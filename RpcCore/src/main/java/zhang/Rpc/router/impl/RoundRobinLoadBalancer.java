package zhang.Rpc.router.impl;


import zhang.Rpc.common.entity.ServiceMeta;
import zhang.Rpc.common.entity.ServiceMetaRes;
import zhang.Rpc.config.RpcProperties;
import zhang.Rpc.registry.RegistryService;
import zhang.Rpc.router.LoadBalancer;
import zhang.Rpc.spi.ExtensionLoader;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description: 轮询算法
 */
public class RoundRobinLoadBalancer implements LoadBalancer {

    private static AtomicInteger roundRobinId = new AtomicInteger(0);

    @Override
    public ServiceMetaRes select(Object[] params, String serviceName) {
        // 获取注册中心
        RegistryService registryService = ExtensionLoader.getInstance().get(RpcProperties.getInstance().getRegisterType());

        List<ServiceMeta> discoveries = registryService.discoveries(serviceName);
        // 1.获取所有服务
        int size = discoveries.size();
        // 2.根据当前轮询ID取余服务长度得到具体服务
        roundRobinId.addAndGet(1);
        if (roundRobinId.get() == Integer.MAX_VALUE){
            roundRobinId.set(0);
        }

        return ServiceMetaRes.build(discoveries.get(roundRobinId.get() % size),discoveries);
    }

}
