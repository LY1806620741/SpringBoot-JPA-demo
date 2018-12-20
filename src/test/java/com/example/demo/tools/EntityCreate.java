package com.example.demo.tools;

import com.example.demo.domain.Message;
import com.example.demo.domain.User;
import com.example.demo.repository.query.QueryMessageRepository;
import com.example.demo.repository.query.QueryUserRepository;

/**
 * 独立出来的一个简易模型器，测试直接取用进行修改
 */

public class EntityCreate {

    /**
     * user
     * @return
     */

    public static User getnewUser(){
        return getnewUser("test");
    }

    public static User savenewUser(QueryUserRepository queryUserRepository,String name){
        User user=getnewUser(name);
        queryUserRepository.save(user);
        return user;
    }

    public static User getnewUser(String name) {
        User user=new User();
        user.setName(name);
        user.setPassword("test");
        user.setArea("test");
        return user;
    }

    /**
     * message
     * @param user
     * @param data
     * @return
     */

    public static Message getnewMessage(User user,String data){
        Message message = getnewMessage(user);
        message.setValue(data);
        return message;
    }

    public static Message savenewMessage(QueryMessageRepository queryMessageRepository,User user,String data){
        Message message = getnewMessage(user,data);
        queryMessageRepository.save(message);
        return message;
    }

    public static Message getnewMessage(User user){
        Message message = new Message();
        message.setUser(user);
        message.setValue("test");
        return message;
    }

    public static Message savenewMessage(QueryMessageRepository queryMessageRepository,User user){
        Message message = getnewMessage(user);
        queryMessageRepository.save(message);
        return message;
    }


}
