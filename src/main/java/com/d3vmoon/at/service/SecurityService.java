package com.d3vmoon.at.service;

import com.google.common.base.Optional;
import spark.Request;
import spark.Response;
import spark.Spark;

public class SecurityService extends AbstractService {

    public void authenticate(Request req, Response resp) {
        if ( "GET".equals(req.requestMethod()) ) {
            return;
        }

        Optional<String> authorization = Optional.fromNullable(req.headers("Authorization"));

        if ( ! authorization.isPresent() ) {
            Spark.halt(401, gson.toJson(new UnauthorizedResponse()));
        }

    }

    private static class UnauthorizedResponse {
        final String login = System.getenv("HEROKU_URL") + "/p/login.html";
    }
}