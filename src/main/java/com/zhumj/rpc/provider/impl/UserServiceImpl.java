package com.zhumj.rpc.provider.impl;

import com.zhumj.rpc.provider.Student;
import com.zhumj.rpc.provider.UserService;

public class UserServiceImpl implements UserService {

    @Override
    public String getName(Long id) {
        return "hehehehehe";
    }

    @Override
    public Student get(String no, String name) {
        Student student = new Student();
        student.setName(name);
        student.setNo(no);
        return student;
    }
}
