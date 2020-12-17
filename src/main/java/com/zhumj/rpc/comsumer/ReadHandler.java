/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.zhumj.rpc.comsumer;

import com.zhumj.rpc.common.PackageMessage;
import com.zhumj.rpc.common.ResponseBody;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 接收服务端响应的数据
 * @author 朱梦杰
 * @version V1.0
 * @since 2020-12-17 13:37
 */
public class ReadHandler extends ChannelInboundHandlerAdapter {

    private static final ConcurrentHashMap<Long, CompletableFuture> responseCallback = new ConcurrentHashMap<>();

    public static void addCallback(Long requestId, CompletableFuture future) {
        responseCallback.put(requestId, future);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        PackageMessage<ResponseBody> data = (PackageMessage) msg;

        CompletableFuture future = responseCallback.get(data.getRequestId());
        future.complete(data.getContent().getRes());
    }
}
