package com.example.demo.tools;

import com.example.demo.domain.User;

/**
 * 独立出来的一个简易模型器，测试直接取用进行修改
 */
public class EntityCreate {

    public static User getnewUser(){
        User user=new User();
        user.setName("test");
        user.setPassword("test");
        user.setArea("test");
        return user;
    }
}
