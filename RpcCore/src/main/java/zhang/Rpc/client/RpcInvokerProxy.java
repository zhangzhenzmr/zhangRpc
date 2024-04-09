package zhang.Rpc.client;

import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import zhang.Rpc.common.constants.MsgType;
import zhang.Rpc.common.constants.ProtocolConstants;
import zhang.Rpc.common.entity.*;
import zhang.Rpc.config.RpcProperties;
import zhang.Rpc.filter.FilterConfig;
import zhang.Rpc.filter.entity.FilterData;
import zhang.Rpc.protocol.MsgHeader;
import zhang.Rpc.protocol.RpcProtocol;
import zhang.Rpc.router.LoadBalancer;
import zhang.Rpc.router.LoadBalancerFactory;
import zhang.Rpc.utils.RpcServiceNameBuilder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.TimeUnit;


/**
 * JDK动态代理
 */
@Slf4j
public class RpcInvokerProxy implements InvocationHandler
{

    private String serviceVersion;
    private long timeout;
    private String loadBalancerType;
    private String faultTolerantType;
    private long retryCount;

    public RpcInvokerProxy(String serviceVersion, long timeout,String faultTolerantType,String loadBalancerType,long retryCount)  {
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.loadBalancerType = loadBalancerType;
        this.faultTolerantType = faultTolerantType;
        this.retryCount = retryCount;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        //消息：消息头+消息体
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();

        //请求ID
        long requestId = RpcRequestHolder.REQUEST_ID_GEN.incrementAndGet();

        //消息头
        MsgHeader header = new MsgHeader();
        header.setMagic(ProtocolConstants.MAGIC);
        header.setVersion(ProtocolConstants.VERSION);
        header.setRequestId(requestId);
        //序列化的相关信息
        final byte[] serialization = RpcProperties.getInstance().getSerialization().getBytes();
        header.setSerializationLen(serialization.length);
        header.setSerializations(serialization);
        header.setMsgType((byte) MsgType.REQUEST.ordinal());
        header.setStatus((byte) 0x1);
        protocol.setHeader(header);

        //消息体
        RpcRequest requestBody =  new RpcRequest();
        requestBody.setServiceVersion(this.serviceVersion);
        requestBody.setClassName(method.getDeclaringClass().getName());
        requestBody.setMethodName(method.getName());
        requestBody.setParameterTypes(method.getParameterTypes());
        requestBody.setData(ObjectUtils.isEmpty(args) ? new Object[0] : args);
        requestBody.setDataClass(ObjectUtils.isEmpty(args) ? null : args[0].getClass());

        //设置token
        requestBody.setServiceAttachments(RpcProperties.getInstance().getServiceAttachments());
        requestBody.setClientAttachments(RpcProperties.getInstance().getClientAttachments());
        protocol.setBody(requestBody);

        // 拦截器的上下文
        final FilterData filterData = new FilterData(requestBody);
        try {
            FilterConfig.getClientBeforeFilterChain().doFilter(filterData);
        }catch (Throwable e){
            throw e;
        }

        //构建服务名
        String serviceName = RpcServiceNameBuilder.buildServiceKey(requestBody.getClassName(),requestBody.getServiceVersion());

        Object[] params = {requestBody.getData()};

        //1、获取负载均衡策略
        LoadBalancer loadBalancer = LoadBalancerFactory.get(loadBalancerType);
        ServiceMetaRes serviceMeta = loadBalancer.select(params, serviceName);

        //当前服务
        ServiceMeta curServiceMeta = serviceMeta.getCurServiceMeta();

        //剩下的可用服务
        Collection<ServiceMeta> otherServiceMeta = serviceMeta.getOtherServiceMeta();

        RpcConnect rpcConnect = new RpcConnect();

        long count =1;

        RpcResponse rpcResponse =null;

        while (count<retryCount)
        {
            RpcFuture<RpcResponse> future = new RpcFuture<>(new DefaultPromise<>(new DefaultEventLoop()), timeout);

            RpcRequestHolder.REQUEST_MAP.put(requestId, future);

            try {
                //向服务端发送请求
                rpcConnect.sendRequest(protocol,curServiceMeta);

                //得到响应
                rpcResponse=future.getPromise().get(future.getTimeout(), TimeUnit.MILLISECONDS);

                if((rpcResponse.getException()!=null && otherServiceMeta.size() == 0) || rpcResponse.getException()!=null ){
                    throw rpcResponse.getException();
                }

                log.info("rpc 调用成功, serviceName: {}",serviceName);

                return rpcResponse.getData();
            }catch (Throwable e) {
                log.info("error");
                e.printStackTrace();
                return null;
            }

        }

        throw new RuntimeException("rpc 调用失败，超过最大重试次数: {}" + retryCount);
    }
}
