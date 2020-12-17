package com.zhumj.rpc.comsumer;

import com.zhumj.rpc.provider.UserService;

public class Consumer {


    public static void main(String[] args) {

        // 获取到远程服务

        UserService userService = ProxyFactory.getProxy(UserService.class);
        for (int i = 0; i < 20; i++) {

            new Thread(() -> {
                String name = userService.getName(121L);
                System.out.println("我拿到结果啦，哈哈"  + name);

            }).start();
        }

//        String name = userService.getName(121L);
//        System.out.println("我拿到结果啦，哈哈"  + name);

    }

}
