/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.zhumj.rpc.transport;


import com.zhumj.rpc.protocol.Header;
import com.zhumj.rpc.protocol.ProtocolEnum;
import com.zhumj.rpc.protocol.RequestBody;
import com.zhumj.rpc.utils.SerializeUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

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
        String protocol = System.getProperties().getProperty(ProtocolEnum.protocol_key, ProtocolEnum.rpc.name());
        ProtocolEnum protocolEnum = ProtocolEnum.fromCode(protocol);

        if (Objects.isNull(protocolEnum)) {
            throw new RuntimeException("不支持的协议" + protocol);
        }
        switch (protocolEnum) {
            case rpc:
                return customRpcTransport(requestBody);
            case stateful_http:
                return nettyStatefulHttpTransport(requestBody);
            case stateless_http:
                return nettyStatelessHttpTransport(requestBody);
            case http_url:
                return httpUrlTransport(requestBody);
            default:
                throw new RuntimeException("不支持的协议" + protocol);
        }
    }

    private static CompletableFuture nettyStatefulHttpTransport(RequestBody requestBody) {
        Channel channel = getClient("user-service");
        byte[] bytes = SerializeUtil.serializeObject(requestBody);
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0,
                HttpMethod.POST, "/",
                Unpooled.copiedBuffer(bytes)
        );
        request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);

        long requestId = UUID.randomUUID().getLeastSignificantBits();
        CompletableFuture future = new CompletableFuture();
        ResponseMappingCallback.registerCallback(requestId, future);

        request.headers().set("requestId", requestId);
        channel.writeAndFlush(request);
        return future;
    }

    private static CompletableFuture customRpcTransport(RequestBody requestBody) {
        byte[] requestBodyBytes = SerializeUtil.serializeObject(requestBody);

        Header header = Header.createRequestHeader(requestBodyBytes.length);
        byte[] headerBytes = SerializeUtil.serializeObject(header);

        Channel channel = getClient("user-service");
        ByteBuf buffer = Unpooled.buffer(headerBytes.length + requestBodyBytes.length);

        buffer.writeBytes(headerBytes);
        buffer.writeBytes(requestBodyBytes);
        CompletableFuture future = new CompletableFuture();
        ResponseMappingCallback.registerCallback(header.getRequestId(), future);
        channel.writeAndFlush(buffer);
        return future;
    }



    private static CompletableFuture nettyStatelessHttpTransport(RequestBody requestBody) {
        Channel client = getClient("user-service");
        // 处理相应后，客户端的回调
        CompletableFuture future = new CompletableFuture();
        // todo 这个骚操作有问题的，client是可以复用的，并发的情况下会添加很多处理相应的handler。
        // 需要在handler当中处理完成之后，把自己remove掉。

        client.pipeline().addLast(new HttpStatelessResponseHandler(future));

        byte[] bytes = SerializeUtil.serializeObject(requestBody);
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0,
                HttpMethod.POST, "/",
                Unpooled.copiedBuffer(bytes)
        );
        request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
        client.writeAndFlush(request);
        return future;
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
