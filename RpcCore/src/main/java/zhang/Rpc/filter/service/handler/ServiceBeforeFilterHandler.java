package zhang.Rpc.filter.service.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zhang.Rpc.common.constants.MsgStatus;
import zhang.Rpc.common.entity.RpcRequest;
import zhang.Rpc.common.entity.RpcResponse;
import zhang.Rpc.filter.FilterConfig;
import zhang.Rpc.filter.entity.FilterData;
import zhang.Rpc.protocol.MsgHeader;
import zhang.Rpc.protocol.RpcProtocol;

public class ServiceBeforeFilterHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {


    private Logger logger = LoggerFactory.getLogger(ServiceBeforeFilterHandler.class);


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) throws Exception {

        logger.info("test-----------------test");

        RpcRequest body = protocol.getBody();

        FilterData filterData = new FilterData(body);

        try {
            FilterConfig.getClientBeforeFilterChain().doFilter(filterData);
        }catch (Exception e) {
            RpcResponse response = new RpcResponse();
            MsgHeader header = protocol.getHeader();

            RpcProtocol<RpcResponse> resProtocol = new RpcProtocol<>();
            header.setStatus((byte) MsgStatus.FAILED.ordinal());
            response.setException(e);
            logger.error("before process request {} error", header.getRequestId(), e);
            resProtocol.setHeader(header);
            resProtocol.setBody(response);
            ctx.writeAndFlush(resProtocol);
            return;
        }

        ctx.fireChannelRead(protocol);
    }
}
