package zhang.Rpc.filter;


import zhang.Rpc.filter.client.ClientAfterFilter;
import zhang.Rpc.filter.entity.FilterData;


public class SystemClientFilter implements ClientAfterFilter {

    @Override
    public void doFilter(FilterData filterData) {
        System.out.println("客户端后置处理器启动咯");
    }
}
