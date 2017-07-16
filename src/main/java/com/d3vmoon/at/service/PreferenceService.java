package com.d3vmoon.at.service;

import com.d3vmoon.at.service.http.NotFoundException;
import com.d3vmoon.at.service.pojo.Preferences;
import com.google.common.primitives.Ints;
import org.jooq.impl.DSL;
import spark.Request;
import spark.Response;

import java.util.Optional;

import static com.d3vmoon.at.db.Tables.AT_PREFERENCS;
import static com.d3vmoon.at.service.SecurityService.PARAM_USER_ID;
import static com.d3vmoon.at.service.http.Path.PARAM_ID;

public class PreferenceService extends AbstractService {

    public Preferences getPreferences(Request req, Response resp) {
        final String idParam = req.params(PARAM_ID);
        final Integer id = Optional.ofNullable(Ints.tryParse(idParam)).orElseThrow(() -> new NotFoundException(req.pathInfo()));

        return ctx
                .select(AT_PREFERENCS.ID,
                        AT_PREFERENCS.USER,
                        AT_PREFERENCS.DIRECTION,
                        DSL.field("lower({0})", AT_PREFERENCS.START_END).as("start"),
                        DSL.field("upper({0})", AT_PREFERENCS.START_END).as("end")
                ).from(AT_PREFERENCS)
                .where(AT_PREFERENCS.USER.eq(id))
                .fetchOneInto(Preferences.class);

    }

    public String setPreferences(Request req, Response resp) {
        final Preferences preferences = gson.fromJson(req.body(), Preferences.class);
        final Integer userId = req.attribute(PARAM_USER_ID);

        ctx.update(AT_PREFERENCS)
                .set(AT_PREFERENCS.DIRECTION, preferences.direction)
                .set(AT_PREFERENCS.START_END, (Object) DSL.field("daterange(?, ?)", Object.class, preferences.start, preferences.end))
                .where(AT_PREFERENCS.USER.eq(userId))
                .execute();

        resp.status(202);
        return "";
    }
}
