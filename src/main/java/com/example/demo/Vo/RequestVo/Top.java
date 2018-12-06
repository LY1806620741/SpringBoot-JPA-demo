package com.example.demo.Vo.RequestVo;

public class Top {
    private Integer rank;
    private String name;
    private Integer day;

    public Integer getDay() {
        return day;
    }

    public Top(Integer rank, String name, Integer day) {
        this.rank = rank;
        this.name = name;
        this.day = day;
    }
}
