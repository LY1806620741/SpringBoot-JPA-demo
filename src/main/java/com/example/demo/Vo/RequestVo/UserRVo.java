package com.example.demo.Vo.RequestVo;

import com.example.demo.domain.enumeration.TopRankTime;
import io.swagger.annotations.ApiModelProperty;

public class UserRVo {
    @ApiModelProperty("名字")
    private String name;
    @ApiModelProperty("地区")
    private String area;
    @ApiModelProperty("添加时间")
    private TopRankTime createtime;
    @ApiModelProperty("最近登陆时间")
    private TopRankTime logintime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public TopRankTime getCreatetime() {
        return createtime;
    }

    public void setCreatetime(TopRankTime createtime) {
        this.createtime = createtime;
    }

    public TopRankTime getLogintime() {
        return logintime;
    }

    public void setLogintime(TopRankTime logintime) {
        this.logintime = logintime;
    }
}
