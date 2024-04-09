package zhang.Rpc.annotation;

import org.springframework.context.annotation.Import;
import zhang.Rpc.server.ServerPostProcessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description: 开启服务提供方自动装配
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(ServerPostProcessor.class)
public @interface EnableProviderRpc { }
