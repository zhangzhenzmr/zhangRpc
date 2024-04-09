package zhang.Rpc.filter.client.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zhang.Rpc.filter.client.ClientBeforeFilter;
import zhang.Rpc.filter.entity.FilterData;


/**
    日志
 */
public class ClientLogFilter implements ClientBeforeFilter {

    private Logger logger = LoggerFactory.getLogger(ClientLogFilter.class);
    @Override
    public void doFilter(FilterData filterData) {
        logger.info(filterData.toString());
    }
}
