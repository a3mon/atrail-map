package com.d3vmoon.at.service.pojo;

import com.d3vmoon.at.db.enums.AtDirection;

import java.time.LocalDate;

public class Preferences {

    public final int id;
    public final int user;
    public final AtDirection direction;
    public final LocalDate start;
    public final LocalDate end;

    public Preferences(int id, int user, AtDirection direction, LocalDate start, LocalDate end) {
        this.id = id;
        this.user = user;
        this.direction = direction;
        this.start = start;
        this.end = end;
    }
}
