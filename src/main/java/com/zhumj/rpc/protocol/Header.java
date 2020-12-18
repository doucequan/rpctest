package com.zhumj.rpc.protocol;

import com.zhumj.rpc.utils.SerializeUtil;

import java.io.Serializable;
import java.util.UUID;

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

    public static transient int headerLength = 106;

    public static void main(String[] args) {
        System.out.println(SerializeUtil.serializeObject(new Header()).length);
    }

    public static Header createRequestHeader(long bodyLength) {
        Header header = new Header();
        header.setRequestId(UUID.randomUUID().getLeastSignificantBits());
        header.setFlag(0x14141414);
        header.setDataLength(bodyLength);
        return header;
    }

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
