package zhang.Rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zhang.Rpc.common.entity.RpcRequest;
import zhang.Rpc.common.entity.ServiceMeta;
import zhang.Rpc.protocol.RpcProtocol;
import zhang.Rpc.client.handler.RpcResponseHandler;
import zhang.Rpc.protocol.codec.RpcDecoder;
import zhang.Rpc.protocol.codec.RpcEncoder;

public class RpcConnect
{
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;
    private Logger logger = LoggerFactory.getLogger(RpcConnect.class);

    //建立连接
    public RpcConnect() {
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(4);
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new RpcEncoder())
                                .addLast(new RpcDecoder())
                                .addLast(new RpcResponseHandler());
                    }
                });
    }

    //发送请求
    public void sendRequest(RpcProtocol<RpcRequest> protocol, ServiceMeta serviceMetadata) throws Exception {
        if (serviceMetadata != null) {
            ChannelFuture future = bootstrap.connect(serviceMetadata.getServiceAddr(), serviceMetadata.getServicePort()).sync();
            future.addListener((ChannelFutureListener) arg0 -> {
                if (future.isSuccess()) {
                    logger.info("连接 rpc server {} 端口 {} 成功.", serviceMetadata.getServiceAddr(), serviceMetadata.getServicePort());
                } else {
                    logger.error("连接 rpc server {} 端口 {} 失败.", serviceMetadata.getServiceAddr(), serviceMetadata.getServicePort());
                    future.cause().printStackTrace();
                    eventLoopGroup.shutdownGracefully();
                }
            });
            // 写入数据
            future.channel().writeAndFlush(protocol);
        }
    }
}
