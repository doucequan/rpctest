/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.zhumj.rpc.common;

import com.zhumj.rpc.common.Header;
import com.zhumj.rpc.common.RequestBody;

/**
 * 每个完整的数据包包含的信息
 * @author 朱梦杰
 * @version V1.0
 * @since 2020-12-17 15:06
 */
public class PackageMessage<T> {

    private Header header;

    private T content;

    public long getRequestId() {
        return header.getRequestId();
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }
}
