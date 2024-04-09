package zhang.Rpc.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import zhang.Rpc.annotation.RpcReference;
import zhang.Rpc.config.RpcProperties;
import zhang.Rpc.filter.FilterConfig;
import zhang.Rpc.filter.client.impl.ClientLogFilter;
import zhang.Rpc.protocol.serialization.SerializationFactory;
import zhang.Rpc.registry.RegistryFactory;
import zhang.Rpc.router.LoadBalancerFactory;
import zhang.Rpc.utils.PropertiesUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;


/**
 * 客户端后置处理器
 */
public class ClientPostProcessor implements InitializingBean, BeanPostProcessor, EnvironmentAware {

    private Logger logger = LoggerFactory.getLogger(ClientLogFilter.class);

    RpcProperties rpcProperties;

    //读取配置文件
    @Override
    public void setEnvironment(Environment environment) {
        rpcProperties=RpcProperties.getInstance();
        PropertiesUtils.init(rpcProperties,environment);
        logger.info("读取配置文件成功");
    }

    //初始化序列化、服务发现、负载均衡、过滤器工厂
    @Override
    public void afterPropertiesSet() throws Exception {
        SerializationFactory.init();
        RegistryFactory.init();
        LoadBalancerFactory.init();
        FilterConfig.initClientFilter();
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        Field[] fields = bean.getClass().getDeclaredFields();

        for (Field field:fields)
        {
            //将标记RpcReference注解生成代理对象
            if (field.isAnnotationPresent(RpcReference.class)) {
              final RpcReference rpcReference = field.getAnnotation(RpcReference.class);
                Class<?> target = field.getType();
                field.setAccessible(true);
                Object targetProxy = null;
                //创建代理对象
                targetProxy=Proxy.newProxyInstance(target.getClassLoader(),
                        new Class<?>[]{target},
                        new RpcInvokerProxy(rpcReference.serviceVersion(),rpcReference.timeout(),
                                rpcReference.faultTolerant(),rpcReference.loadBalancer(),rpcReference.retryCount()));

                try {
                    field.set(bean,targetProxy);
                    field.setAccessible(false);
                    logger.info(beanName + " field:" + field.getName() + "注入成功");
                } catch (IllegalAccessException e){
                    e.printStackTrace();
                    logger.info(beanName + " field:" + field.getName() + "注入失败");
                }

            }
        }

        return bean;
    }
}
