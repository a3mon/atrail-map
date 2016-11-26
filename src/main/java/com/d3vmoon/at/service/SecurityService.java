package com.d3vmoon.at.service;

import com.d3vmoon.at.service.pojo.Login;
import com.google.common.collect.ImmutableMap;
import org.jooq.Record1;
import org.jooq.Record2;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.Optional;
import java.util.UUID;

import static com.d3vmoon.at.db.Tables.AT_SESSION;
import static com.d3vmoon.at.db.tables.AtUser.AT_USER;
import static com.d3vmoon.at.service.http.Path.SESSIONS;
import static org.jooq.impl.DSL.lower;
import static org.jooq.impl.DSL.select;

public class SecurityService extends AbstractService {

    public static final String USER_ID_PARAM = "user-id";

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityService.class);

    public void authenticate(Request req, Response resp) {
        if ( "GET".equals(req.requestMethod())
             || SESSIONS.equals(req.pathInfo()) && "POST".equals(req.requestMethod())
        ) {
            return;
        }

        final Optional<String> authorization = Optional.ofNullable(req.headers("Authorization"));

        if ( ! authorization.isPresent() ) {
            Spark.halt(401, gson.toJson(new UnauthorizedResponse()));
        }

        final UUID token;
        try {
            token = UUID.fromString(authorization.get());
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Invalid UUID", e);
            Spark.halt(401, gson.toJson(new UnauthorizedResponse()));
            return;
        }

        Optional<Record1<Integer>> userId = ctx.select(AT_SESSION.AT_USER)
                .from(AT_SESSION)
                .where(AT_SESSION.TOKEN.eq(token))
                .fetchOptional();

        if ( ! userId.isPresent() ) {
            Spark.halt(401, gson.toJson(new UnauthorizedResponse()));
        }

        req.attribute(USER_ID_PARAM, userId.get().value1());
    }

    public Object login(Request req, Response resp) {
        final Login login = gson.fromJson(req.body(), Login.class);

        final Optional<Record2<Integer, String>> record = ctx.select(AT_USER.ID, AT_USER.PASSWORD)
                .from(AT_USER)
                .where(lower(AT_USER.EMAIL).eq(lower(login.email)))
                .fetchOptional();

        if ( ! record.isPresent() ) {
            resp.status(401);
            return new CouldNotAuthenticateResponse();
        }

        final boolean isAuthenticated = BCrypt.checkpw(
                login.password,
                record.get().get(AT_USER.PASSWORD)
        );

        if ( ! isAuthenticated ) {
            resp.status(401);
            return new CouldNotAuthenticateResponse();
        }

        final int userId = record.get().get(AT_USER.ID);
        final UUID token = UUID.randomUUID();

        final boolean updateSession = ctx.fetchExists(
                select(AT_SESSION.ID)
                        .from(AT_SESSION)
                        .where(AT_SESSION.AT_USER.eq(userId))
        );

        if ( updateSession ) {
            ctx.update(AT_SESSION)
                    .set(AT_SESSION.TOKEN, token)
                    .where(AT_SESSION.AT_USER.eq(userId))
                    .execute();
        } else {
            ctx.insertInto(AT_SESSION, AT_SESSION.AT_USER, AT_SESSION.TOKEN)
                    .values(userId, token)
                    .execute();
        }

        return ImmutableMap.of("token", token.toString());
    }

    public Object logout(Request req, Response resp) {

        final Integer userId = req.attribute(USER_ID_PARAM);

        LOGGER.debug("Logging out user {}", userId);

        final int count = ctx.deleteFrom(AT_SESSION)
                .where(AT_SESSION.AT_USER.eq(userId))
                .execute();

        if ( count > 1) {
            LOGGER.error("Logged out more then one user. ({})", count);
        } else if (count < 0) {
            LOGGER.warn("Log out attempt failed.");
        }

        resp.status(204);
        return "";
    }

    private static class UnauthorizedResponse {
        final String login = System.getenv("HEROKU_URL") + "/p/login.html";
    }

    private static class CouldNotAuthenticateResponse extends UnauthorizedResponse {
        final String message = "Could not authenticate.";
    }
}