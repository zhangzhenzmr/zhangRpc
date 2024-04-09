package zhang.Rpc.filter;

import zhang.Rpc.filter.entity.FilterData;

/**
 * @description: 拦截器
 */
public interface Filter {

    void doFilter(FilterData filterData);

}
