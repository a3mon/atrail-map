package com.d3vmoon.at.service.pojo;

import org.postgresql.geometric.PGpoint;

public class Coordinate {
    public final double lat;
    public final double lng;

    public Coordinate(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public static Coordinate of(PGpoint point) {
        return new Coordinate(point.x, point.y);
    }
}
