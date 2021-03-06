/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.zhumj.rpc.transport;

import com.zhumj.rpc.utils.PackageMessage;

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
public class CustomRpcResponseHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        PackageMessage data = (PackageMessage) msg;

        CompletableFuture future = ResponseMappingCallback.getCallback(data.getRequestId());
        future.complete(data.getContent());
    }
}
