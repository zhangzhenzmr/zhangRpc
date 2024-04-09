package zhang.Rpc.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import zhang.Rpc.common.entity.RpcRequest;
import zhang.Rpc.poll.ThreadPollFactory;
import zhang.Rpc.protocol.RpcProtocol;

public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) throws Exception {
        ThreadPollFactory.submitRequest(ctx,protocol);
    }

    public RpcRequestHandler() {}
}
