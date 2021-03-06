package com.d3vmoon.at.service.pojo;

import org.postgresql.geometric.PGpoint;

public class Shelter {
    public final int id;
    public final String name;
    public final Coordinate point;
    public final String comment;
    public final boolean campground;

    public Shelter(int id, String name, Coordinate point, String comment, boolean campground) {
        this.id = id;
        this.name = name;
        this.point = point;
        this.comment = comment;
        this.campground = campground;
    }

    public Shelter(int id, String name, PGpoint point, String comment, boolean campground) {
        this.id = id;
        this.name = name;
        this.point = new Coordinate(point.x, point.y);
        this.comment = comment;
        this.campground = campground;
    }
}
