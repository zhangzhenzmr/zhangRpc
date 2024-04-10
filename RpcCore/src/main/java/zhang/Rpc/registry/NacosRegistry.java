package zhang.Rpc.registry;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import zhang.Rpc.common.entity.ServiceMeta;
import zhang.Rpc.config.RpcProperties;
import zhang.Rpc.utils.RpcServiceNameBuilder;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class NacosRegistry implements RegistryService {

    private final NamingService namingService;

    public NacosRegistry()   {
        RpcProperties properties = RpcProperties.getInstance();
        // 创建Nacos配置
        Properties nacosProperties = new Properties();
        nacosProperties.put("serverAddr", properties.getRegisterAddr());
        // 创建NamingService实例
        try {
            namingService = NacosFactory.createNamingService(nacosProperties);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void register(ServiceMeta serviceMeta)   {
        String key = RpcServiceNameBuilder.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion());

        Instance instance = new Instance();
        instance.setIp(serviceMeta.getServiceAddr());
        instance.setPort(serviceMeta.getServicePort());
        instance.setServiceName(key);
        // 可以添加更多服务的元数据
        instance.addMetadata("version", serviceMeta.getServiceVersion());
        try {
            namingService.registerInstance(key, instance);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unRegister(ServiceMeta serviceMeta)   {
        try {
            namingService.deregisterInstance(serviceMeta.getServiceName(), serviceMeta.getServiceAddr(), serviceMeta.getServicePort());
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ServiceMeta> discoveries(String serviceName)   {


        List<Instance> instances = null;
        try {
            instances = namingService.getAllInstances(serviceName);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
        List<ServiceMeta> serviceMetas = new ArrayList<>();
        for (Instance instance : instances) {
            ServiceMeta serviceMeta = new ServiceMeta();
            serviceMeta.setServiceAddr(instance.getIp());
            serviceMeta.setServicePort(instance.getPort());
            serviceMeta.setServiceName(instance.getServiceName());
            serviceMeta.setServiceVersion(instance.getMetadata().get("version"));
            serviceMetas.add(serviceMeta);
        }
        return serviceMetas;
    }

    @Override
    public void destroy() throws IOException {
        // Nacos SDK does not require explicit destruction. Cleanup if necessary.
    }
}
