package com.zhumj.rpc.protocol;

import java.util.HashMap;

public enum ProtocolEnum {
    rpc("rpc"),
    stateful_http("statefulHttp"),
    stateless_http("statelessHttp"),
    http_url("httpUrl");
    private String protocolName;

    public static String protocol_key = "protocol";

    ProtocolEnum(String protocolName) {
        this.protocolName = protocolName;
    }

    private static HashMap<String, ProtocolEnum> code_map = new HashMap<>(16);

    static {
        for (ProtocolEnum value : ProtocolEnum.values()) {
            code_map.put(value.protocolName, value);
        }
    }

    public static ProtocolEnum fromCode(String code) {
        return code_map.get(code);
    }


}
