package zhang.Rpc.filter.service.impl;


import zhang.Rpc.config.RpcProperties;
import zhang.Rpc.filter.service.ServiceBeforeFilter;
import zhang.Rpc.filter.entity.FilterData;

import java.util.Map;

/**
    token拦截器
 */
public class ServiceTokenFilter implements ServiceBeforeFilter {

    @Override
    public void doFilter(FilterData filterData) {
        final Map<String, Object> attachments = filterData.getClientAttachments();
        final Map<String, Object> serviceAttachments = RpcProperties.getInstance().getServiceAttachments();

        //验证token
        if (!attachments.getOrDefault("token","").equals(serviceAttachments.getOrDefault("token",""))){
            throw new IllegalArgumentException("token不正确");
        }
    }

}
