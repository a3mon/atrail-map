package com.d3vmoon.at;

import com.d3vmoon.at.service.ATService;
import com.d3vmoon.at.service.PreferenceService;
import com.d3vmoon.at.service.SecurityService;
import com.d3vmoon.at.service.ShelterService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.d3vmoon.at.service.http.Path.*;
import static spark.Spark.*;

public class Main {

    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private final ShelterService shelterService = new ShelterService();
    private final SecurityService securityService = new SecurityService();
    private final PreferenceService preferenceService = new PreferenceService();

    public static void main(String[] args)  {
        new Main().init();
    }

    private void init() {
        port(Integer.valueOf(System.getenv("PORT")));
        staticFileLocation("/public");

        before("/api/*", securityService::authenticate);

        post(SESSIONS, securityService::login, gson::toJson);
        delete(SESSIONS + PARAM_ID, securityService::logout, gson::toJson);

        post(USERS, securityService::signup, gson::toJson);

        post(CONFIRMATIONS, securityService::confirm, gson::toJson);

        get(CURRENT_TRAIL, new ATService()::getCurrentTrail, gson::toJson);

        get(SHELTERS, shelterService::getShelters, gson::toJson);
        get(SHELTERS + PARAM_ID, shelterService::getShelter, gson::toJson);
        post(SHELTERS + PARAM_ID, shelterService::setShelter);

        get(PREFERENCES + PARAM_ID, preferenceService::getPreferences, gson::toJson);
        post(PREFERENCES + PARAM_ID, preferenceService::setPreferences);

        redirect.any("/", "/u/manage_trail.html");

        exception(Exception.class, (exception, request, response) -> LOGGER.error("uuups...", exception));
    }

}
