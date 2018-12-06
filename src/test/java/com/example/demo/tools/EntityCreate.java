package com.example.demo.tools;

import com.example.demo.domain.User;

public class EntityCreate {

    public static User getnewUser(){
        User user=new User();
        user.setName("test");
        user.setPassword("test");
        user.setArea("test");
        user.setPassword("test");
        return user;
    }
}
