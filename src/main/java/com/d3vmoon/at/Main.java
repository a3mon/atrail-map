package com.d3vmoon.at;

import com.d3vmoon.at.service.ATService;
import com.d3vmoon.at.service.SecurityService;
import com.d3vmoon.at.service.ShelterService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static com.d3vmoon.at.service.http.Path.*;
import static spark.Spark.*;

public class Main {

    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final ShelterService shelterService = new ShelterService();
    private final SecurityService securityService = new SecurityService();

    public static void main(String[] args)  {

        new Main().init();
    }

    public void init() {
        port(Integer.valueOf(System.getenv("PORT")));
        staticFileLocation("/public");

        before("/api/*", securityService::authenticate);

        get(CURRENT_TRAIL, new ATService()::getCurrentTrail, gson::toJson);

        get(SHELTERS, shelterService::getShelters, gson::toJson);
        get(SHELTERS + PARAM_ID, shelterService::getShelter, gson::toJson);
        post(SHELTERS + PARAM_ID, shelterService::setShelter);

        redirect.any("/", "/u/manage_trail.html");

    }

}
