package com.example.demo.repository.query;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;

import java.util.Optional;

public interface QueryUserRepository extends UserRepository {
    Optional<User> findOneByName(String username);
}
