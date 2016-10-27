package com.d3vmoon.at;

import com.d3vmoon.at.service.ATService;
import com.d3vmoon.at.service.ShelterService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static spark.Spark.*;

public class Main {

    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args)  {

        new Main().init();
    }

    public void init() {
        port(Integer.valueOf(System.getenv("PORT")));
        staticFileLocation("/public");

        get("/currentTrail", new ATService()::getCurrentTrail, gson::toJson);

        get("/shelters", new ShelterService()::getShelters, gson::toJson);

        get("/", (req, res) -> "Hello Worldd");
    }
}
