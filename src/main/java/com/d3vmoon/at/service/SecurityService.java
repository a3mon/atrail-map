package com.d3vmoon.at.service;

import com.d3vmoon.at.service.pojo.Credentials;
import com.google.common.collect.ImmutableMap;
import com.sendgrid.*;
import org.jooq.Record1;
import org.jooq.Record2;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static com.d3vmoon.at.db.Tables.AT_CONFIRMATION;
import static com.d3vmoon.at.db.Tables.AT_SESSION;
import static com.d3vmoon.at.db.tables.AtUser.AT_USER;
import static com.d3vmoon.at.service.http.Path.SESSIONS;
import static com.d3vmoon.at.service.http.Path.USERS;
import static org.jooq.impl.DSL.lower;
import static org.jooq.impl.DSL.select;

public class SecurityService extends AbstractService {


    public static final String USER_ID_PARAM = "user-id";
    public static final String HEROKU_URL = System.getenv("HEROKU_URL");

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityService.class);

    public void authenticate(Request req, Response resp) {
        if ( "GET".equals(req.requestMethod())
            || SESSIONS.equals(req.pathInfo()) && "POST".equals(req.requestMethod())
            || USERS.equals(req.pathInfo()) && "POST".equals(req.requestMethod())
        ) {
            return;
        }

        final Optional<String> authorization = Optional.ofNullable(req.headers("Authorization"));

        if ( ! authorization.isPresent() ) {
            Spark.halt(401, gson.toJson(new UnauthorizedResponse()));
            return;
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
            return;
        }

        req.attribute(USER_ID_PARAM, userId.get().value1());
    }

    public Object login(Request req, Response resp) {
        final Credentials credentials = gson.fromJson(req.body(), Credentials.class);

        final Optional<Record2<Integer, String>> record = ctx.select(AT_USER.ID, AT_USER.PASSWORD)
                .from(AT_USER)
                .where(lower(AT_USER.EMAIL).eq(lower(credentials.email)))
                .fetchOptional();

        if ( ! record.isPresent() ) {
            resp.status(401);
            return new CouldNotAuthenticateResponse();
        }

        final boolean isAuthenticated = BCrypt.checkpw(
                credentials.password,
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

    public Object signup(Request req, Response resp) {
        final Credentials credentials = gson.fromJson(req.body(), Credentials.class);

        final String hash = BCrypt.hashpw(credentials.password, BCrypt.gensalt());

        Integer userId = ctx.insertInto(AT_USER, AT_USER.EMAIL, AT_USER.PASSWORD)
                .values(credentials.email, hash)
                .returning(AT_USER.ID)
                .fetchOne()
                .get(AT_USER.ID);

        final UUID token = UUID.randomUUID();

        if ( sendConfirmationEmail(credentials.email, token) ) {
            ctx.insertInto(AT_CONFIRMATION, AT_CONFIRMATION.AT_USER, AT_CONFIRMATION.TOKEN)
                    .values(userId, token)
                    .execute();

            sendConfirmationEmail(credentials.email, token);

            resp.status(204);
            return "";
        } else {
            resp.status(400);
            return "";
        }
    }

    private boolean sendConfirmationEmail(String email, UUID token) {
        final Email from = new Email("doNotReply@atrail-map.herokuapp.com");
        final String subject = "Please confirm your Email address!";
        final Email to = new Email(email);
        final Content content = new Content("text/plain",
                "Hello and thanks for your registration.\n\n" +
                "To complete your registration, please follow this link: " +
                HEROKU_URL + "p/confirm.html?token=" + token.toString() + "\n\n" +
                "Thanks and have a good hike!"
        );

        final Mail mail = new Mail(from, subject, to, content);

        final SendGrid sg = new SendGrid(System.getenv("SENDGRID_API_KEY"));
        final com.sendgrid.Request request = new com.sendgrid.Request();
        request.method = Method.POST;
        request.endpoint = "mail/send";
        try {
            request.body = mail.build();
            final com.sendgrid.Response response = sg.api(request);

            LOGGER.info("Sent mail with status {}", response.statusCode);
            return true;
        } catch (IOException ex) {
            LOGGER.error("Could not send Email.", ex);
            return false;
        }
    }

    private static class UnauthorizedResponse {
        final String login = HEROKU_URL + "/p/login.html";
    }

    private static class CouldNotAuthenticateResponse extends UnauthorizedResponse {
        final String message = "Could not authenticate.";
    }
}