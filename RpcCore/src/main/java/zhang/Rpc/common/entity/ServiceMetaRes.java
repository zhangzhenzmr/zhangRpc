package zhang.Rpc.common.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;

@Data
public class ServiceMetaRes {

    // 当前服务节点
    private ServiceMeta curServiceMeta;
    // 剩余服务节点
    private Collection<ServiceMeta> otherServiceMeta;

    public static ServiceMetaRes build(ServiceMeta curServiceMeta, Collection<ServiceMeta> otherServiceMeta){
        final ServiceMetaRes serviceMetaRes = new ServiceMetaRes();
        serviceMetaRes.curServiceMeta = curServiceMeta;
        // 如果只有一个服务
        if(otherServiceMeta.size() == 1){
            otherServiceMeta = new ArrayList<>();
        }else{
            otherServiceMeta.remove(curServiceMeta);
        }
        serviceMetaRes.otherServiceMeta = otherServiceMeta;
        return serviceMetaRes;
    }


}
