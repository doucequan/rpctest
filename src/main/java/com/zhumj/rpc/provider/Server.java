package com.zhumj.rpc.provider;

import com.zhumj.rpc.common.ClassUtils;
import com.zhumj.rpc.common.Header;
import com.zhumj.rpc.common.RequestBody;
import com.zhumj.rpc.common.ResponseBody;
import com.zhumj.rpc.common.SerializeUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.lang.reflect.Method;

public class Server {

    public static void main(String[] args) throws InterruptedException {

        ChannelFuture bind = new ServerBootstrap()
                .group(new NioEventLoopGroup(1))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new RequestHandler());
                    }
                }).bind(2222);

        bind.sync().channel().closeFuture().sync();

    }
}

class RequestHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // header的长度是104
        ByteBuf data = (ByteBuf) msg;
        if (data.readableBytes() >= 104) {
            byte[] headerBytes = new byte[104];
            data.readBytes(headerBytes);

            Header header = SerializeUtil.deserialize(headerBytes, Header.class);
            System.out.println(header);

            if (data.readableBytes() >= header.getDataLength()) {
                byte[] bodyBytes = new byte[data.readableBytes()];
                data.readBytes(bodyBytes);
                RequestBody body = SerializeUtil.deserialize(bodyBytes, RequestBody.class);
                System.out.println(body);
                // 根据body中的信息，进行方法调用
                String interfaceName = body.getInterfaceName();
                String methodName = body.getMethodName();
                Object[] args = body.getArgs();
                Class<?>[] parameterTypes = body.getParameterTypes();

                //  根据interfaceName获取实现类，并通过反射调用方法
                Object instanceByInterfaceName = ClassUtils.getInstanceByInterfaceName(interfaceName);
                Class<?> aClass = instanceByInterfaceName.getClass();
                Method method = aClass.getMethod(methodName, parameterTypes);
                Object result = method.invoke(instanceByInterfaceName, args);

                // 将返回值写回
                ResponseBody responseBody = new ResponseBody();
                responseBody.setClassName(result.getClass().getName());
                responseBody.setRes(result);

                byte[] responseBodyBytes = SerializeUtil.serializeObject(responseBody);
                Header responseHeader = new Header();
                responseHeader.setRequestId(header.getRequestId());
                responseHeader.setFlag(0x14141424);
                responseHeader.setDataLength(responseBodyBytes.length);

                byte[] responseHeaderBytes = SerializeUtil.serializeObject(responseHeader);
                ByteBuf buffer = Unpooled.buffer(responseBodyBytes.length + responseHeaderBytes.length);

                buffer.writeBytes(responseHeaderBytes);
                buffer.writeBytes(responseBodyBytes);
                ctx.channel().writeAndFlush(buffer);
            }
        }

    }
}
