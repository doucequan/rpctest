package com.zhumj.rpc.common;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 通过这三个参数，服务端能够找到本地的方法进行调用
 */
public class RequestBody implements Serializable {

    private static final long serialVersionUID = -7332056307918446670L;
    /**
     * 参数
     */
    private Object[] args;

    /**
     * 接口全限定名称
     */
    private String interfaceName;

    /**
     * 方法名称
     */
    private String methodName;

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public String toString() {
        return "RequestBody{" +
                "args=" + Arrays.toString(args) +
                ", interfaceName='" + interfaceName + '\'' +
                ", methodName='" + methodName + '\'' +
                '}';
    }
}
