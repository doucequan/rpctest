/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.zhumj.rpc.comsumer;


import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;

/**
 * @author 朱梦杰
 * @version V1.0
 * @since 2020-12-17 13:59
 */
public class ClientFactory {

    /**
     * 消费者对每个服务提供方都有配置连接池
     */
    private static final ConcurrentHashMap<String, ClientPool> PROVIDER_CLIENT_POOL = new ConcurrentHashMap<>();

    /**
     * 根据服务名称获取连接
     * @param providerName
     * @return
     */
    public static Channel getClient(String providerName) {
        ClientPool clientPool = PROVIDER_CLIENT_POOL.get(providerName);

        if (Objects.nonNull(clientPool)) {
            return clientPool.getClient();
        }
        clientPool = new ClientPool(providerName);
        PROVIDER_CLIENT_POOL.putIfAbsent(providerName, clientPool);
        return PROVIDER_CLIENT_POOL.get(providerName).getClient();
    }

}
