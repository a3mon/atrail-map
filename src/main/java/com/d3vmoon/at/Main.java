package com.d3vmoon.at;

import com.d3vmoon.at.service.ATService;
import com.d3vmoon.at.service.ShelterService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static spark.Spark.*;

public class Main {

    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private ShelterService shelterService = new ShelterService();

    public static void main(String[] args)  {

        new Main().init();
    }

    public void init() {
        port(Integer.valueOf(System.getenv("PORT")));
        staticFileLocation("/public");

        get("/currentTrail", new ATService()::getCurrentTrail, gson::toJson);

        get("/shelters", shelterService::getShelters, gson::toJson);
        get("/shelters/:id", shelterService::getShelter, gson::toJson);
        post("/shelters/:id", shelterService::setShelter);

        get("/", (req, res) -> "Hello Worldd");
    }

}
