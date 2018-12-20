package com.example.demo.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty("说说id")
    private Long id;
    @ManyToOne//多对一关联

    @ApiModelProperty("用户")
    private User user;

    @ApiModelProperty("内容")
    private String value;

    @ApiModelProperty("发布时间")
    private Instant time;

    public Message(){this.time=Instant.now();}//如果有自定义的构造函数，那么就需要写这个默认的构造函数，不然jpa查询的时候会报错 No default constructor for entity

    public Message(User user, String data) {
        this.user=user;
        this.value=data;
        this.time=Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }
}
