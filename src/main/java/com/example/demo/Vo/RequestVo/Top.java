package com.example.demo.Vo.RequestVo;

import io.swagger.annotations.ApiModelProperty;

public class Top {
    @ApiModelProperty("排名（解决同排名问题）")
    private final Integer rank;
    @ApiModelProperty("用户名")
    private final String name;
    @ApiModelProperty("元老天数(登陆时间减去创建时间)")
    private final Integer day;

    public Integer getDay() {
        return day;
    }

    public Top(Integer rank, String name, Integer day) {
        this.rank = rank;
        this.name = name;
        this.day = day;
    }

    public Integer getRank() {
        return rank;
    }

    public String getName() {
        return name;
    }
}
