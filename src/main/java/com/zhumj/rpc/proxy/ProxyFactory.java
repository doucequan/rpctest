package com.zhumj.rpc.proxy;

import com.zhumj.rpc.protocol.RequestBody;

import com.zhumj.rpc.transport.ClientFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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
                CompletableFuture future = ClientFactory.transport(requestBody);
                // 阻塞，等待服务端返回，此处需要一个回调，服务端返回时，也需要相应header中的requestId，依据此requestId进行回调
                return future.get();
            }
        });

        return (T) o;

    }


}
