package com.zhumj.rpc.provider;

import com.zhumj.rpc.common.Header;
import com.zhumj.rpc.common.RequestBody;
import com.zhumj.rpc.common.SerializeUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class Server {

    public static void main(String[] args) throws InterruptedException {

        ChannelFuture bind = new ServerBootstrap()
                .group(new NioEventLoopGroup(1))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new RequestHandler());
                    }
                }).bind(9090);

        bind.sync().channel().closeFuture().sync();

    }
}

class RequestHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // header的长度是104
        ByteBuf data = (ByteBuf) msg;
        if (data.readableBytes() >= 104) {
            byte[] headerBytes = new byte[104];
            data.readBytes(headerBytes);

            Header header = SerializeUtil.deserialize(headerBytes, Header.class);
            System.out.println(header);

            if (data.readableBytes() >= header.getDataLength()) {
                byte[] bodyBytes = new byte[data.readableBytes()];
                data.readBytes(bodyBytes);
                RequestBody body = SerializeUtil.deserialize(bodyBytes, RequestBody.class);
                System.out.println(body);
            }
        }





    }
}
