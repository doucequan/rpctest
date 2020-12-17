/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.zhumj.rpc.provider;

import com.zhumj.rpc.common.Header;
import com.zhumj.rpc.common.PackageMessage;
import com.zhumj.rpc.common.RequestBody;
import com.zhumj.rpc.common.ResponseBody;
import com.zhumj.rpc.common.SerializeUtil;

import java.lang.reflect.Method;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 接收请求，调用具体的实现类，返回结果
 * @author 朱梦杰
 * @version V1.0
 * @since 2020-12-17 13:38
 */
public class RequestHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("发生了异常......" + cause);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // header的长度是104
        PackageMessage<RequestBody> pkg = (PackageMessage) msg;

        RequestBody body = pkg.getContent();
        // 根据body中的信息，进行方法调用
        String interfaceName = body.getInterfaceName();
        String methodName = body.getMethodName();
        Object[] args = body.getArgs();
        Class<?>[] parameterTypes = body.getParameterTypes();

        //  根据interfaceName获取实现类，并通过反射调用方法
        Object service = Dispatcher.getService(interfaceName);

        Method method =  service.getClass().getMethod(methodName, parameterTypes);
        Object result = method.invoke(service, args);

        // 将返回值写回
        ResponseBody responseBody = new ResponseBody();
        responseBody.setClassName(result.getClass().getName());
        responseBody.setRes(result);

        byte[] responseBodyBytes = SerializeUtil.serializeObject(responseBody);
        Header responseHeader = new Header();
        responseHeader.setRequestId(pkg.getRequestId());
        responseHeader.setFlag(0x14141424);
        responseHeader.setDataLength(responseBodyBytes.length);

        byte[] responseHeaderBytes = SerializeUtil.serializeObject(responseHeader);
        ByteBuf buffer = Unpooled.buffer(responseBodyBytes.length + responseHeaderBytes.length);

        buffer.writeBytes(responseHeaderBytes);
        buffer.writeBytes(responseBodyBytes);
        ctx.channel().writeAndFlush(buffer);

    }
}
