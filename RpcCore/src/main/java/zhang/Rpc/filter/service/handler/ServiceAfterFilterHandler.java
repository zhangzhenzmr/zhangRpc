package zhang.Rpc.filter.service.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zhang.Rpc.common.constants.MsgStatus;
import zhang.Rpc.common.entity.RpcResponse;
import zhang.Rpc.filter.FilterConfig;
import zhang.Rpc.filter.client.impl.ClientLogFilter;
import zhang.Rpc.filter.entity.FilterData;
import zhang.Rpc.protocol.MsgHeader;
import zhang.Rpc.protocol.RpcProtocol;

public class ServiceAfterFilterHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {

    private Logger logger = LoggerFactory.getLogger(ClientLogFilter.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcResponse> responseProtocol) throws Exception {

        FilterData filterData = new FilterData();

        filterData.setData(responseProtocol.getBody());

        RpcResponse response = new RpcResponse();
        MsgHeader header = responseProtocol.getHeader();
        try {
            FilterConfig.getServiceAfterFilterChain().doFilter(filterData);
        } catch (Exception e) {
            header.setStatus((byte) MsgStatus.FAILED.ordinal());
            response.setException(e);
            logger.error("after process request {} error", header.getRequestId(), e);
        }
        ctx.writeAndFlush(responseProtocol);

    }
}
