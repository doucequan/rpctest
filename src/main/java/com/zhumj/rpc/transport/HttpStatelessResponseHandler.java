package com.zhumj.rpc.transport;

import com.zhumj.rpc.utils.SerializeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;

import java.util.concurrent.CompletableFuture;

public class HttpStatelessResponseHandler extends ChannelInboundHandlerAdapter {



//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        throw new RuntimeException(cause);
//    }
//
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        DefaultFullHttpResponse response = (DefaultFullHttpResponse) msg;
//        ByteBuf content = response.content();
//        byte[] bytes = new byte[content.readableBytes()];
//
//        content.readBytes(bytes);
//        Object res = SerializeUtil.deserialize(bytes, Object.class);
//
//        CompletableFuture future = ResponseMappingCallback.getCallback(Long.parseLong(requestId));
//
//        future.complete(res);
//    }
//
}
