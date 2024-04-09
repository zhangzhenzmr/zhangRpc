package zhang.Rpc.demo.service;


import zhang.Rpc.annotation.RpcService;
import zhang.Rpc.demo.Test2Service;


@RpcService
public class Test2ServiceImpl implements Test2Service {

    @Override
    public void test(String key) {

        System.out.println("服务提供1 test2 测试成功 :" + key);
    }
}
