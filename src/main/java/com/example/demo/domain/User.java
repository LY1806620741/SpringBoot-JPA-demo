package com.example.demo.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.Instant;

@ApiModel("账号")
@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)//缓存机制，使用不严格的读写，更新不频繁时候使用
public class User implements Serializable {
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    @ApiModelProperty("名字")
    private String name;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("地区")
    private String area;

    @ApiModelProperty("添加时间")
    private Instant createtime=Instant.now();

    @ApiModelProperty("最近登陆时间")
    private Instant logintime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Instant getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Instant createtime) {
        this.createtime = createtime;
    }

    public Instant getLogintime() {
        return logintime;
    }

    public void setLogintime(Instant logintime) {
        this.logintime = logintime;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", area='" + area + '\'' +
                ", createtime=" + createtime +
                ", logintime=" + logintime +
                '}';
    }
}
