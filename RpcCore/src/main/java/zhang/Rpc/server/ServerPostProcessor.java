package zhang.Rpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import zhang.Rpc.annotation.RpcService;
import zhang.Rpc.server.handler.RpcRequestHandler;
import zhang.Rpc.common.entity.ServiceMeta;
import zhang.Rpc.config.RpcProperties;
import zhang.Rpc.filter.FilterConfig;
import zhang.Rpc.filter.service.handler.ServiceAfterFilterHandler;
import zhang.Rpc.filter.service.handler.ServiceBeforeFilterHandler;
import zhang.Rpc.poll.ThreadPollFactory;
import zhang.Rpc.protocol.codec.RpcDecoder;
import zhang.Rpc.protocol.codec.RpcEncoder;
import zhang.Rpc.protocol.serialization.SerializationFactory;
import zhang.Rpc.registry.RegistryFactory;
import zhang.Rpc.registry.RegistryService;
import zhang.Rpc.router.LoadBalancerFactory;
import zhang.Rpc.utils.PropertiesUtils;
import zhang.Rpc.utils.RpcServiceNameBuilder;

import java.util.HashMap;
import java.util.Map;


/**
 服务提供方后置处理器
 */
public class ServerPostProcessor implements InitializingBean, BeanPostProcessor, EnvironmentAware {

    private Logger logger = LoggerFactory.getLogger(ServerPostProcessor.class);
    RpcProperties properties;
    private static String serverAddress = "127.0.0.1";
    private final Map<String, Object> localRegisMap = new HashMap<>();

    //读取配置文件
    @Override
    public void setEnvironment(Environment environment) {
        properties=RpcProperties.getInstance();
        PropertiesUtils.init(properties,environment);
        logger.info("读取配置文件成功");
    }

    //开启与客户端的连接,并且初始化工厂
    @Override
    public void afterPropertiesSet() throws Exception {
        Thread connect = new Thread(()->{
            try {
                startRpcServer();
            } catch (Exception e) {
                logger.error("start rpc server error.", e);
            }
        });

        connect.setDaemon(true);
        connect.start();

        SerializationFactory.init();
        RegistryFactory.init();
        LoadBalancerFactory.init();
        FilterConfig.initServiceFilter();

        ThreadPollFactory.setLocalServiceMap(localRegisMap);
    }

    //服务注册到本地和远程的服务中心
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        // 找到bean上带有 RpcService 注解的类
        RpcService rpcService = beanClass.getAnnotation(RpcService.class);
        if (rpcService != null) {
            // 可能会有多个接口,默认选择第一个接口
            String serviceName = beanClass.getInterfaces()[0].getName();
            if (!rpcService.serviceInterface().equals(void.class)){
                serviceName = rpcService.serviceInterface().getName();
            }
            String serviceVersion = rpcService.serviceVersion();
            try {
                int servicePort = properties.getPort();
                // 获取注册中心 ioc
                RegistryService registryService = RegistryFactory.get(properties.getRegisterType());
                ServiceMeta serviceMeta = new ServiceMeta();
                // 服务提供方地址
                serviceMeta.setServiceAddr("127.0.0.1");
                serviceMeta.setServicePort(servicePort);
                serviceMeta.setServiceVersion(serviceVersion);
                serviceMeta.setServiceName(serviceName);
                registryService.register(serviceMeta);
                // 本地缓存
                localRegisMap.put(RpcServiceNameBuilder.buildServiceKey(serviceMeta.getServiceName(),serviceMeta.getServiceVersion()), bean);
                logger.info("register server {} version {}",serviceName,serviceVersion);
            } catch (Exception e) {
                logger.error("failed to register service {}",  serviceVersion, e);
            }
        }
        return bean;
    }
    private void startRpcServer() throws InterruptedException {
        int serverPort = properties.getPort();

        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();

        bootstrap.group(boss,worker)
                .option(ChannelOption.SO_KEEPALIVE,true)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new RpcEncoder())
                                .addLast(new RpcDecoder())
                                .addLast(new ServiceBeforeFilterHandler())
                                //处理请求，将结果回复
                                .addLast(new RpcRequestHandler())
                                .addLast(new ServiceAfterFilterHandler());
                    }
                })
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        ChannelFuture channelFuture = bootstrap.bind(this.serverAddress, serverPort).sync();
        logger.info("server addr {} started on port {}", this.serverAddress, serverPort);
        channelFuture.channel().closeFuture().sync();
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            logger.info("ShutdownHook execute start...");
            logger.info("Netty NioEventLoopGroup shutdownGracefully...");
            logger.info("Netty NioEventLoopGroup shutdownGracefully2...");
            boss.shutdownGracefully();
            worker.shutdownGracefully();
            logger.info("ShutdownHook execute end...");
        }, "Allen-thread"));


    }



}