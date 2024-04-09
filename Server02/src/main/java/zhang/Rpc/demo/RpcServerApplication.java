package zhang.Rpc.demo;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import zhang.Rpc.annotation.EnableProviderRpc;

@SpringBootApplication
@EnableProviderRpc
public class RpcServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RpcServerApplication.class, args);
    }
}
