package com.zhumj.rpc.common;

import java.io.Serializable;

public class Header implements Serializable {

    private static final long serialVersionUID = 1965841287361799709L;
    /**
     * 请求的唯一标识，服务端返回的时候，要依据此标识知道回调的方法
     */
    private long requestId;

    /**
     * 指定requestBody的大小，服务端依据此来拆包
     */
    private long dataLength;

    private long flag;

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public long getDataLength() {
        return dataLength;
    }

    public void setDataLength(long dataLength) {
        this.dataLength = dataLength;
    }

    public long getFlag() {
        return flag;
    }

    public void setFlag(long flag) {
        this.flag = flag;
    }

    @Override
    public String toString() {
        return "Header{" +
                "requestId=" + requestId +
                ", dataLength=" + dataLength +
                ", flag=" + flag +
                '}';
    }
}
