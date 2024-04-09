package zhang.Rpc;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import zhang.Rpc.annotation.EnableConsumerRpc;

@SpringBootApplication
@EnableConsumerRpc
public class RpcClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(RpcClientApplication.class, args);
    }


}
