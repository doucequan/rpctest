package com.zhumj.rpc.comsumer;

import com.zhumj.rpc.provider.Server;
import com.zhumj.rpc.provider.Student;
import com.zhumj.rpc.provider.UserService;
import com.zhumj.rpc.proxy.ProxyFactory;

public class Consumer {


    public static void main(String[] args) throws InterruptedException {

        // 获取到远程服务
//        new Thread(() -> {
//            Server.startServer();
//        }).start();
//        Thread.sleep(121);

        UserService userService = ProxyFactory.getProxy(UserService.class);
        for (int i = 0; i < 20; i++) {

            int finalI = i;
            new Thread(() -> {
//                String name = userService.getName(121L);

                Student student = userService.get(finalI + "", "周星星");

                System.out.println(student);

            }).start();
        }

//        String name = userService.getName(121L);
//        System.out.println("我拿到结果啦，哈哈"  + name);

    }

}
