package com.d3vmoon.at.service.pojo;

import com.d3vmoon.at.db.enums.AtDirection;

public class Hiker {

    public final int id;
    public final String trailName;
    public final String realName;
    public final AtDirection direction;
    public final int year;

    public Hiker(int id, String trailName, String realName, AtDirection direction, int year) {
        this.id = id;
        this.trailName = trailName;
        this.realName = realName;
        this.direction = direction;
        this.year = year;
    }
}
