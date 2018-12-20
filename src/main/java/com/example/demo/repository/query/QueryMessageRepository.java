package com.example.demo.repository.query;

import com.example.demo.repository.MessageRepository;

public interface QueryMessageRepository extends MessageRepository {
    Long countByUser_Id(Long id);
}
