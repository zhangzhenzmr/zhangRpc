package zhang.Rpc.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zhang.Rpc.annotation.RpcReference;
import zhang.Rpc.common.constants.FaultTolerantRules;
import zhang.Rpc.common.constants.LoadBalancerRules;
import zhang.Rpc.demo.Test2Service;
import zhang.Rpc.demo.TestService;



@RestController
public class Test {

    @RpcReference(timeout = 10000L,faultTolerant = FaultTolerantRules.Failover,loadBalancer = LoadBalancerRules.RoundRobin)
    TestService testService;

    @RpcReference(loadBalancer = LoadBalancerRules.ConsistentHash)
    Test2Service test2Service;

    /**
     * 轮询
     * 会触发故障转移,提供方模拟异常
     */
    @RequestMapping("test/{key}")
    public String test(@PathVariable String key){
        testService.test(key);
        return "test1 ok";
    }

    /**
     * 一致性哈希
     */
    @RequestMapping("test2/{key}")
    public String test2(@PathVariable String key){

         test2Service.test(key);

         return "ConsistentHash Success";
    }

    /**
     * 轮询,无如何异常
     */
    @RequestMapping("test3/{key}")
    public String test3(@PathVariable String key){
        testService.test2(key);
        return "test2 ok";
    }

}
