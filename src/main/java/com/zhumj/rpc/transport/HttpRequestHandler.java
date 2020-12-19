package com.zhumj.rpc.transport;

import com.zhumj.rpc.Dispatcher;
import com.zhumj.rpc.protocol.RequestBody;
import com.zhumj.rpc.utils.SerializeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HttpRequestHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        DefaultFullHttpRequest request = (DefaultFullHttpRequest) msg;
        System.out.println(Thread.currentThread().getName() + "\t" + request.toString());

        ByteBuf content = request.content();

        byte[] bytes = new byte[content.readableBytes()];
        content.readBytes(bytes);
        RequestBody body = SerializeUtil.deserialize(bytes, RequestBody.class);

        String interfaceName = body.getInterfaceName();
        String methodName = body.getMethodName();
        Object[] args = body.getArgs();
        Class<?>[] parameterTypes = body.getParameterTypes();

        //  根据interfaceName获取实现类，并通过反射调用方法
        Object service = Dispatcher.getService(interfaceName);

        Method method = null;
        Object result = null;
        try {
            method = service.getClass().getMethod(methodName, parameterTypes);
            result = method.invoke(service, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        byte[] responseBytes = SerializeUtil.serializeObject(result);

        ByteBuf byteBuf = Unpooled.copiedBuffer(responseBytes);

        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.OK, byteBuf);
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, responseBytes.length);

        String requestId = request.headers().get("requestId");
        if (requestId != null && requestId.length() > 0) {
            response.headers().set("requestId", requestId);
        }
        ctx.writeAndFlush(response);


    }
}
