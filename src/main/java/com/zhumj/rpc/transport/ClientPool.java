/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.zhumj.rpc.transport;

import java.net.InetSocketAddress;


import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import com.zhumj.rpc.protocol.ProtocolEnum;
import com.zhumj.rpc.utils.SerializeUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;

/**
 * 连接池，该连接池是基于某个provider的
 * @author 朱梦杰
 * @version V1.0
 * @since 2020-12-17 11:19
 */
public class ClientPool {

    /**
     * 服务提供方的名字，会基于此服务名称拉取服务的ip:port
     */
    private String providerName;

    private InetSocketAddress[] providerAddresses;

    private final int poolSize = 2;

    private boolean init;

    private Bootstrap bootstrap;

    private Channel[] clients;

    private final AtomicInteger connectCount = new AtomicInteger(0);

    public ClientPool(String providerName) {
        this.providerName = providerName;
    }

    public synchronized Channel getClient() {
        if (!init) {
            init();
        }
        int currentConnectCount = connectCount.getAndIncrement();
        int index = currentConnectCount % poolSize;
        Channel client = clients[index];
        if (client == null || !client.isActive()) {
            try {
                client = bootstrap.connect(providerAddresses[0]).sync().channel();
                System.out.println("创建连接：" + client);
                clients[index] = client;
                return client;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return client;
    }

    private void init() {
        if (init) {
            return;
        }
        clients = new NioSocketChannel[poolSize];

        initBootstrap();
        // todo 调用注册中心，根据providerName获取服务提供方的ip：port，还是根据调用的接口名称获取？？？
        providerAddresses = new InetSocketAddress[]{new InetSocketAddress("127.0.0.1", 9090)};
        init = true;
    }

    private void initBootstrap() {
        // http协议使用netty自身封装好的解码器（将byteBuf转为response），然后假如自定义的HttpResponseHandler，取出结果
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        String protocol = System.getProperties().getProperty(ProtocolEnum.protocol_key, ProtocolEnum.rpc.protocolName);

        ProtocolEnum protocolEnum = ProtocolEnum.fromCode(protocol);
        if (Objects.isNull(protocolEnum)) {
            throw new RuntimeException("不支持的传输协议");
        }
        switch (protocolEnum) {
            case rpc:
                initRpcBootstrap();
                break;
            case stateful_http:
                initStatefulHttpBootstrap();
                break;
            case stateless_http:
                initStatelessHttpBootstrap();
                break;
        }
    }

    private void initRpcBootstrap() {
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        bootstrap = new Bootstrap().group(group).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new DecodeHandler()).addLast(new CustomRpcResponseHandler());
                    }
                });
    }

    private void initStatefulHttpBootstrap() {
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        bootstrap = new Bootstrap().group(group).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new HttpClientCodec())
                                .addLast(new HttpObjectAggregator(1024 * 512))
                                .addLast(new HttpStatefulResponseHandler());
                    }
                });
    }

    private void initStatelessHttpBootstrap() {
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        bootstrap = new Bootstrap().group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new HttpClientCodec())
                                .addLast(new HttpObjectAggregator(1024 * 512));
                    }
                });
    }
}

