/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.zhumj.rpc.common;

import java.io.Serializable;

/**
 * @author 朱梦杰
 * @version V1.0
 * @since 2020-12-17 10:57
 */
public class ResponseBody implements Serializable {

    private static final long serialVersionUID = -5762890819836245699L;

    private String className;

    private Object res;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Object getRes() {
        return res;
    }

    public void setRes(Object res) {
        this.res = res;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ResponseBody{");
        sb.append("className='").append(className).append('\'');
        sb.append(", res=").append(res);
        sb.append('}');
        return sb.toString();
    }
}
