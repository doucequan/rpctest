package com.zhumj.rpc.provider;

public interface UserService {

    /**
     * 根据用户id获取用户姓名
     * @param id
     * @return
     */
    String getName(Long id);

    Student get(String no, String name);
}
