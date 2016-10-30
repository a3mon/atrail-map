package com.d3vmoon.at.service.http;

public class Path {

    private static final String API_VERSION = "v1/";
    private static final String API_BASE = "/api/" + API_VERSION;

    public static final String CURRENT_TRAIL = API_BASE + "currentTrail";
    public static final String SHELTERS = API_BASE + "shelters/";

    public static final String PARAM_ID = ":id";
}
