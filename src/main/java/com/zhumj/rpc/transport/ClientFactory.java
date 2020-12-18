/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.zhumj.rpc.transport;


import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import com.zhumj.rpc.protocol.RequestBody;
import com.zhumj.rpc.utils.SerializeUtil;
import com.zhumj.rpc.protocol.Header;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

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
        byte[] requestBodyBytes = SerializeUtil.serializeObject(requestBody);

        Header header = Header.createRequestHeader(requestBodyBytes.length);
        byte[] headerBytes = SerializeUtil.serializeObject(header);

        // 建立与远程的连接，进行数据的传输
        Channel channel = ClientFactory.getClient("user-service");
        ByteBuf buffer = Unpooled.buffer(headerBytes.length + requestBodyBytes.length);

        buffer.writeBytes(headerBytes);
        buffer.writeBytes(requestBodyBytes);
        CompletableFuture future = new CompletableFuture();
        ReadHandler.addCallback(header.getRequestId(), future);
        channel.writeAndFlush(buffer);
        return future;
    }

}
