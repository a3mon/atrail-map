package com.d3vmoon.at;

import com.d3vmoon.at.service.*;
import com.d3vmoon.at.service.pojo.TimelineDate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.d3vmoon.at.service.http.Path.*;
import static spark.Spark.*;

public class Main {

    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private final ATService atService = new ATService();
    private final ShelterService shelterService = new ShelterService();
    private final SecurityService securityService = new SecurityService();
    private final PreferenceService preferenceService = new PreferenceService();
    private final TimelineService timelineService = new TimelineService();

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

        get(TRAIL, atService::getFullTrail, gson::toJson);
        get(TRAIL + PARAM_ID, atService::getUserTrail, gson::toJson);

        get(SHELTERS, shelterService::getShelters, gson::toJson);
        get(SHELTERS + PARAM_ID, shelterService::getShelter, gson::toJson);

        get(PREFERENCES + PARAM_ID, preferenceService::getPreferences, gson::toJson);
        post(PREFERENCES + PARAM_ID, preferenceService::setPreferences);

        get(TIMELINE + PARAM_ID, timelineService::getTimeline, gson::toJson);
        post(TIMELINE + PARAM_ID, timelineService::addTimelineMoment);
        delete(TIMELINE + PARAM_ID + "/" + PARAM_LAST, timelineService::deleteLastTimelineMoment);

        redirect.any("/", "/u/manage_trail.html");

        exception(Exception.class, (exception, request, response) -> LOGGER.error("uuups...", exception));
    }

}
