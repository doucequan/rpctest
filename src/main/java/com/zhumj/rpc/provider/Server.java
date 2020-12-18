package com.zhumj.rpc.provider;

import com.zhumj.rpc.Dispatcher;
import com.zhumj.rpc.transport.DecodeHandler;
import com.zhumj.rpc.provider.impl.UserServiceImpl;

import com.zhumj.rpc.transport.RequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class Server {

    public static void main(String[] args) throws InterruptedException {

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

        bind.sync().channel().closeFuture().sync();

    }
}

