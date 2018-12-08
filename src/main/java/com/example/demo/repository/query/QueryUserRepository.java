package com.example.demo.repository.query;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;

import java.time.Instant;
import java.util.Optional;

public interface QueryUserRepository extends UserRepository {
    /**
     * 因为这种用法不灵活，所以不常使用
     */
    //登陆查询
    Optional<User> findOneByName(String username);
    //根据地区统计用户总数
    Long countByArea(String area);
    //查询在一个时间段内注册的用户，且以时间先后排序
    Long findAllByCreatetimeGreaterThanEqualAndCreatetimeLessThanEqualOrderByCreatetimeDesc(Instant min,Instant max);
}
