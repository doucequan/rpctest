package com.zhumj.rpc.provider;

import java.io.Serializable;

public class Student implements Serializable {

    private static final long serialVersionUID = 5482099109741139278L;

    private String name;

    private String no;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", no='" + no + '\'' +
                '}';
    }
}
