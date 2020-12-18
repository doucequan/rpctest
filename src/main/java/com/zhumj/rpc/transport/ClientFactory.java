/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.zhumj.rpc.transport;


import java.io.*;
import java.net.*;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import com.zhumj.rpc.protocol.RequestBody;
import com.zhumj.rpc.utils.SerializeUtil;
import com.zhumj.rpc.protocol.Header;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;

/**
 * @author 朱梦杰
 * @version V1.0
 * @since 2020-12-17 13:59
 */
public class ClientFactory {

    /**
     * 消费者对每个服务提供方都有配置连接池
     */
    private static final ConcurrentHashMap<String, ClientPool> PROVIDER_CLIENT_POOL = new ConcurrentHashMap<>();

    /**
     * 根据服务名称获取连接
     * @param providerName
     * @return
     */
    public static Channel getClient(String providerName) {
        ClientPool clientPool = PROVIDER_CLIENT_POOL.get(providerName);

        if (Objects.nonNull(clientPool)) {
            return clientPool.getClient();
        }
        clientPool = new ClientPool(providerName);
        PROVIDER_CLIENT_POOL.putIfAbsent(providerName, clientPool);
        return PROVIDER_CLIENT_POOL.get(providerName).getClient();
    }

    public static CompletableFuture transport(RequestBody requestBody) {
        // 可以根据协议走不同的分支
        String protocol = "http";
        if (protocol.equals("http")) {
//            return httpUrlTransport(requestBody);
            CompletableFuture<Object> objectCompletableFuture = new CompletableFuture<>();
            nettyHttpTransport(requestBody,objectCompletableFuture);
            return objectCompletableFuture;
        }


        byte[] requestBodyBytes = SerializeUtil.serializeObject(requestBody);

        Header header = Header.createRequestHeader(requestBodyBytes.length);
        byte[] headerBytes = SerializeUtil.serializeObject(header);

        // 建立与远程的连接，进行数据的传输
        Channel channel = getClient("user-service");
        ByteBuf buffer = Unpooled.buffer(headerBytes.length + requestBodyBytes.length);

        buffer.writeBytes(headerBytes);
        buffer.writeBytes(requestBodyBytes);
        CompletableFuture future = new CompletableFuture();
        ReadHandler.addCallback(header.getRequestId(), future);
        channel.writeAndFlush(buffer);
        return future;
    }

    private static void nettyHttpTransport(RequestBody requestBody, CompletableFuture future) {
        Bootstrap handler = new Bootstrap().group(new NioEventLoopGroup(1))
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new HttpClientCodec())
                                .addLast(new HttpObjectAggregator(1024 * 512))
                                .addLast(new ChannelInboundHandlerAdapter() {

                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        DefaultFullHttpResponse response = (DefaultFullHttpResponse) msg;


                                        ByteBuf content = response.content();

                                        byte[] bytes = new byte[content.readableBytes()];

                                        content.readBytes(bytes);
                                        Object res = SerializeUtil.deserialize(bytes, Object.class);

                                        future.complete(res);
                                    }
                                });
                    }
                });

        try {
            Channel client = handler.connect("localhost", 9090).sync().channel();
            byte[] bytes = SerializeUtil.serializeObject(requestBody);
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0,
                    HttpMethod.POST, "/",
                    Unpooled.copiedBuffer(bytes)
            );
            request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
            client.writeAndFlush(request);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static CompletableFuture httpUrlTransport(RequestBody requestBody) {
        RuntimeException runtimeException = new RuntimeException("unknown error....");
        try {
            URL url = new URL("http", "localhost", 9090, "/");
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            OutputStream outputStream = urlConnection.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(requestBody);

            if (urlConnection.getResponseCode() == 200) {
                InputStream inputStream = urlConnection.getInputStream();

                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

                Object o = objectInputStream.readObject();
                CompletableFuture future = new CompletableFuture();
                future.complete(o);
                return future;
            }
        } catch (MalformedURLException e) {
            runtimeException = new RuntimeException(e.getCause());
            e.printStackTrace();
        } catch (IOException e) {
            runtimeException = new RuntimeException(e.getCause());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            runtimeException = new RuntimeException(e.getCause());
            e.printStackTrace();
        }
        throw runtimeException;
    }

}
