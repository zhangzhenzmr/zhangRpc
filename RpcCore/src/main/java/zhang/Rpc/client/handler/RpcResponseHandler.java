package zhang.Rpc.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import zhang.Rpc.common.entity.RpcFuture;
import zhang.Rpc.common.entity.RpcRequestHolder;
import zhang.Rpc.common.entity.RpcResponse;
import zhang.Rpc.protocol.MsgHeader;
import zhang.Rpc.protocol.RpcProtocol;

public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>>
{

    //对服务端的响应进行处理
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcResponse> msg) throws Exception
    {
        long requestId = msg.getHeader().getRequestId();

        //将从服务端得到的结果进行封装
        RpcFuture<RpcResponse> future = RpcRequestHolder.REQUEST_MAP.remove(requestId);

        future.getPromise().setSuccess(msg.getBody());
    }
}
