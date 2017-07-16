package com.d3vmoon.at.service.http;

public class Path {

    private static final String API_VERSION = "v1/";
    private static final String API_BASE = "/api/" + API_VERSION;

    public static final String CURRENT_TRAIL = API_BASE + "currentTrail";
    public static final String SHELTERS = API_BASE + "shelters/";

    public static final String PREFERENCES = API_BASE + "preferences/";

    public static final String SESSIONS = API_BASE + "sessions/";
    public static final String USERS = API_BASE + "users/";

    public static final String CONFIRMATIONS = API_BASE + "confirmations/";

    public static final String PARAM_ID = ":id";
}
