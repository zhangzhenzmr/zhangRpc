package zhang.Rpc.annotation;

import org.springframework.context.annotation.Import;
import zhang.Rpc.client.ClientPostProcessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
    开启调用方自动装配
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(ClientPostProcessor.class)
public @interface EnableConsumerRpc { }
