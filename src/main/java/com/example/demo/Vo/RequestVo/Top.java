package com.example.demo.Vo.RequestVo;

public class Top {
    private final Integer rank;
    private final String name;
    private final Integer day;

    public Integer getDay() {
        return day;
    }

    public Top(Integer rank, String name, Integer day) {
        this.rank = rank;
        this.name = name;
        this.day = day;
    }
}
