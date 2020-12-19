package com.zhumj.rpc.provider;

import com.zhumj.rpc.Dispatcher;
import com.zhumj.rpc.protocol.ProtocolEnum;
import com.zhumj.rpc.provider.impl.UserServiceImpl;
import com.zhumj.rpc.transport.DecodeHandler;
import com.zhumj.rpc.transport.HttpRequestHandler;
import com.zhumj.rpc.transport.RequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

public class Server {

    public static void main(String[] args)  {

        String property = System.getProperties().getProperty(ProtocolEnum.protocol_key, ProtocolEnum.rpc.name());
        if (ProtocolEnum.rpc.name().equals(property)) {
            System.out.println("服务端使用rpc协议");
            startServer();
        } else {
            startHttpServer();
        }
    }

    public static void startHttpServer() {
        UserService userService = new UserServiceImpl();
        Dispatcher.addService(UserService.class.getName(), userService);

        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup(2);
        ChannelFuture bind = new ServerBootstrap()
                .group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new HttpServerCodec())
                        .addLast(new HttpObjectAggregator(1024*512))
                                .addLast(new HttpRequestHandler());
                    }
                }).bind(9090);

        try {
            bind.sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void startServer() {
        UserService userService = new UserServiceImpl();
        Dispatcher.addService(UserService.class.getName(), userService);

        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup(3);
        ChannelFuture bind = new ServerBootstrap()
                .group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new DecodeHandler())
                                .addLast(new RequestHandler());
                    }
                }).bind(9090);

        try {
            bind.sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

