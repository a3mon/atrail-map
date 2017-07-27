package com.d3vmoon.at.service.pojo;

import java.time.LocalDate;

public class TimelineDate {

    public final int id;
    public final int poiId;
    public final String poiName;
    public final LocalDate date;
    public final String comment;

    public TimelineDate(int id, int poiId, String poiName, LocalDate date, String comment) {
        this.id = id;
        this.poiId = poiId;
        this.poiName = poiName;
        this.date = date;
        this.comment = comment;
    }
}
