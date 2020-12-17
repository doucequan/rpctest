package com.zhumj.rpc.comsumer;

import com.zhumj.rpc.common.Header;
import com.zhumj.rpc.common.RequestBody;
import com.zhumj.rpc.common.SerializeUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ProxyFactory {

    /**
     * 基于接口获取动态代理
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T> T getProxy(final Class<T> tClass) {

        Object o = Proxy.newProxyInstance(tClass.getClassLoader(), new Class[]{tClass}, new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                // 生成请求头和请求体
                RequestBody requestBody = new RequestBody();
                requestBody.setArgs(args);
                requestBody.setMethodName(method.getName());
                requestBody.setInterfaceName(tClass.getName());
                requestBody.setParameterTypes(method.getParameterTypes());
                byte[] requestBodyBytes = SerializeUtil.serializeObject(requestBody);

                Header header = new Header();
                header.setRequestId(UUID.randomUUID().getLeastSignificantBits());
                header.setFlag(0x14141414);
                header.setDataLength(requestBodyBytes.length);
                byte[] headerBytes = SerializeUtil.serializeObject(header);

                // 建立与远程的连接，进行数据的传输
                Channel channel = ClientFactory.getClient("user-service");
                ByteBuf buffer = Unpooled.buffer(headerBytes.length + requestBodyBytes.length);

                buffer.writeBytes(headerBytes);
                buffer.writeBytes(requestBodyBytes);
                CompletableFuture future = new CompletableFuture();
                ReadHandler.addCallback(header.getRequestId(), future);
                channel.writeAndFlush(buffer);
                // 阻塞，等待服务端返回，此处需要一个回调，服务端返回时，也需要相应header中的requestId，依据此requestId进行回调
                return future.get();
            }
        });

        return (T) o;

    }


}
