package com.d3vmoon.at.service.pojo;

import com.d3vmoon.at.db.enums.AtDirection;

import java.time.LocalDate;

public class Preferences {

    public final int id;
    public final int user;
    public final AtDirection direction;
    public final LocalDate start;
    public final LocalDate end;
    public final boolean showFullTrail;

    public Preferences(int id, int user, AtDirection direction, LocalDate start, LocalDate end, boolean showFullTrail) {
        this.id = id;
        this.user = user;
        this.direction = direction;
        this.start = start;
        this.end = end;
        this.showFullTrail = showFullTrail;
    }
}
