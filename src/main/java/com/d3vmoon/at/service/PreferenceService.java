package com.d3vmoon.at.service;

import com.d3vmoon.at.service.pojo.Preferences;
import org.jooq.impl.DSL;
import spark.Request;
import spark.Response;

import static com.d3vmoon.at.db.Tables.AT_PREFERENCES;

public class PreferenceService extends AbstractService {

    public Preferences getPreferences(Request req, Response resp) {
        final Integer userId = getIdFromPath(req);

        return ctx
                .select(AT_PREFERENCES.ID,
                        AT_PREFERENCES.USER,
                        AT_PREFERENCES.DIRECTION,
                        DSL.field("lower({0})", AT_PREFERENCES.START_END).as("start"),
                        DSL.field("upper({0})", AT_PREFERENCES.START_END).as("end"),
                        AT_PREFERENCES.SHOW_WHOLE_TRAIL
                ).from(AT_PREFERENCES)
                .where(AT_PREFERENCES.USER.eq(userId))
                .fetchOneInto(Preferences.class);

    }

    public String setPreferences(Request req, Response resp) {
        final Preferences preferences = gson.fromJson(req.body(), Preferences.class);
        final Integer userId = SecurityService.getUserId(req);

        ctx.update(AT_PREFERENCES)
                .set(AT_PREFERENCES.DIRECTION, preferences.direction)
                .set(AT_PREFERENCES.START_END, (Object) DSL.field("daterange(?, ?)", Object.class, preferences.start, preferences.end))
                .set(AT_PREFERENCES.SHOW_WHOLE_TRAIL, preferences.showFullTrail)
                .where(AT_PREFERENCES.USER.eq(userId))
                .execute();

        resp.status(202);
        return "";
    }

}
