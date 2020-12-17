/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.zhumj.rpc.provider;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 朱梦杰
 * @version V1.0
 * @since 2020-12-17 18:55
 */
public class Dispatcher {

    private static final ConcurrentHashMap<String, Object> INTERFACE_NAME_SERVICE = new ConcurrentHashMap<>();


    public static void addService(String serviceName, Object serviceBean) {
        INTERFACE_NAME_SERVICE.putIfAbsent(serviceName, serviceBean);
    }

    public static Object getService(String serviceName) {
        return INTERFACE_NAME_SERVICE.get(serviceName);

    }

}
