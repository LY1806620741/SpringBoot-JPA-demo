package com.example.demo.domain;

import io.swagger.annotations.ApiModel;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.Instant;

@ApiModel("说说")
@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Message {

    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private User user;
    private String value;
    private Instant time;

    public Message(User user, String data) {
        this.user=user;
        this.value=data;
        this.time=Instant.now();
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setTime(Instant time) {
        this.time = time;
    }
}