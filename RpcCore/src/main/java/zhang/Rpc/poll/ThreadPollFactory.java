package zhang.Rpc.poll;

import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;
import zhang.Rpc.common.constants.MsgStatus;
import zhang.Rpc.common.constants.MsgType;
import zhang.Rpc.common.entity.RpcRequest;
import zhang.Rpc.common.entity.RpcResponse;
import zhang.Rpc.utils.RpcServiceNameBuilder;
import zhang.Rpc.protocol.MsgHeader;
import zhang.Rpc.protocol.RpcProtocol;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class ThreadPollFactory
{
    private static Logger logger = LoggerFactory.getLogger(ThreadPollFactory.class);
    private static int corSize = Runtime.getRuntime().availableProcessors();
    //本地服务注册中心
    private static Map<String,Object> localServiceMap;
    //慢任务线程池
    private static ThreadPoolExecutor slowPoll;
    //快任务线程池
    private static ThreadPoolExecutor fastPoll;
    //key为服务名，value为被标记慢服务的次数。
    private static volatile ConcurrentHashMap<String, AtomicInteger> slowTaskMap =  new ConcurrentHashMap<>();


    static {
        slowPoll=new ThreadPoolExecutor(corSize/2,corSize,
                60L, TimeUnit.SECONDS, new LinkedBlockingDeque<>(2000), r-> {
            Thread thread = new Thread(r);
            thread.setName("slow poll-"+r.hashCode());
            thread.setDaemon(true);
           return thread;
        });

        fastPoll=new ThreadPoolExecutor(corSize/2,corSize,
                60L,TimeUnit.SECONDS,new LinkedBlockingDeque<>(1000),r->{
            Thread thread = new Thread(r);
            thread.setName("fast poll-"+r.hashCode());
            thread.setDaemon(true);
            return thread;
        });
    }

    private ThreadPollFactory(){};

    public static void setLocalServiceMap(Map<String,Object> map) {
        localServiceMap=map;
    }

    //接收客户端的消息，对其进行处理
    public static void submitRequest(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> requestProtocol) {

        RpcRequest body = requestProtocol.getBody();

        String key = body.getClassName()+body.getMethodName()+body.getServiceVersion();

        ThreadPoolExecutor poll = fastPoll;

        //判断当前的服务是不是慢服务
        if (slowTaskMap.contains(key) && slowTaskMap.get(key).intValue()>=10) {
            poll=slowPoll;
        }

        poll.submit(()->{

            long startTime = System.currentTimeMillis();

            RpcProtocol<RpcResponse> responseProtocol = new RpcProtocol<>();

            MsgHeader responseHeader = requestProtocol.getHeader();

            RpcResponse responseBody = new RpcResponse();

            try {
                Object  responseData = submit(ctx, requestProtocol);

                responseBody.setData(responseData);
                responseBody.setDataClass(responseData==null ? null: responseData.getClass());
                responseHeader.setStatus((byte) MsgStatus.SUCCESS.ordinal());
            } catch (Exception e) {
                responseHeader.setStatus((byte) MsgStatus.FAILED.ordinal());
                responseBody.setException(e);
                logger.error("process request {} error", responseHeader.getRequestId(), e);
            }finally {
                long cost = System.currentTimeMillis()-startTime;
                logger.info("cost time:" + cost);
                if (cost>1000) {
                    //标记为慢服务
                    AtomicInteger timeOutCount = slowTaskMap.putIfAbsent(key, new AtomicInteger(1));
                    if (timeOutCount!=null) {
                        timeOutCount.incrementAndGet();
                    }
                }
            }
            responseProtocol.setBody(responseBody);
            responseProtocol.setHeader(responseHeader);
            logger.info("执行成功: {},{},{},{}",Thread.currentThread().getName(),body.getClassName(),body.getMethodName(),body.getServiceVersion());
            ctx.fireChannelRead(responseProtocol);
        });

    }

    //修改响应头的MsgType
    private static Object submit(ChannelHandlerContext ctx,RpcProtocol<RpcRequest> requestProtocol) throws InvocationTargetException {

        MsgHeader header = requestProtocol.getHeader();

        header.setMsgType((byte) MsgType.RESPONSE.ordinal());

        RpcRequest requestBody = requestProtocol.getBody();

        return handle(requestBody);
    }

    //处理请求，返回结果
    private static Object handle(RpcRequest request) throws InvocationTargetException {

        String serviceKey = RpcServiceNameBuilder.buildServiceKey(request.getClassName(), request.getServiceVersion());

        Object serviceBean = localServiceMap.get(serviceKey);

        if (serviceBean==null) {
            throw new RuntimeException(String.format("service not exist: %s:%s", request.getClassName(), request.getMethodName()));
        }

        Class<?> serviceClass = serviceBean.getClass();

        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object [] parameters={request.getData()};

        FastClass fastClass = FastClass.create(serviceClass);
        int methodIndex = fastClass.getIndex(methodName, parameterTypes);

        // 调用方法并返回结果
        return fastClass.invoke(methodIndex, serviceBean, parameters);
    }

}
