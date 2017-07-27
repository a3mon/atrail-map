package com.d3vmoon.at.service;

import com.d3vmoon.at.service.pojo.TimelineDate;
import spark.Request;
import spark.Response;

import java.time.LocalDate;
import java.util.List;

import static com.d3vmoon.at.db.Tables.AT_POI;
import static com.d3vmoon.at.db.tables.AtTimeline.AT_TIMELINE;
import static com.d3vmoon.at.service.SecurityService.getUserId;
import static com.d3vmoon.at.service.http.Path.PARAM_LAST;
import static javax.servlet.http.HttpServletResponse.*;

public class TimelineService extends AbstractService {

    public List<TimelineDate> getTimeline(Request req, Response resp) {
        int userId = getIdFromPath(req);

        return ctx.select(AT_TIMELINE.ID, AT_POI.ID, AT_POI.NAME, AT_TIMELINE.DATE, AT_TIMELINE.COMMENT)
                       .from(AT_TIMELINE)
                       .join(AT_POI).on(AT_TIMELINE.AT_POI.eq(AT_POI.ID))
                       .where(AT_TIMELINE.AT_USER.eq(userId))
                       .orderBy(AT_TIMELINE.DATE.desc())
                       .fetch()
                       .into(TimelineDate.class);
    }

    public String addTimelineMoment(Request req, Response resp) {
        final int userId = getUserId(req);
        final TimelineDate timelineDate = gson.fromJson(req.body(), TimelineDate.class);

        final LocalDate latestDate = ctx.selectFrom(AT_TIMELINE)
                .where(AT_TIMELINE.AT_USER.eq(userId))
                .orderBy(AT_TIMELINE.DATE.desc())
                .limit(1)
                .fetchOne(AT_TIMELINE.DATE)
                .toLocalDate();

        if (latestDate != null && timelineDate.date.isBefore(latestDate)) {
            resp.status(SC_CONFLICT);
            return "";
        }

        ctx.insertInto(AT_TIMELINE, AT_TIMELINE.AT_USER, AT_TIMELINE.AT_POI, AT_TIMELINE.COMMENT, AT_TIMELINE.DATE)
                .values(userId, timelineDate.poiId, timelineDate.comment, java.sql.Date.valueOf(timelineDate.date))
                .execute();

        resp.status(SC_ACCEPTED);
        return "";
    }

    public String deleteLastTimelineMoment(Request req, Response resp) {
        final int pathUserId = getIdFromPath(req);
        final int userId = getUserId(req);
        final String idParam = req.params(PARAM_LAST);

        if ("last".equalsIgnoreCase(idParam) && pathUserId == userId) {
            ctx.deleteFrom(AT_TIMELINE)
            .where(AT_TIMELINE.ID.eq(
                    ctx.select(AT_TIMELINE.ID).from(AT_TIMELINE)
                    .where(AT_TIMELINE.AT_USER.eq(userId))
                    .orderBy(AT_TIMELINE.DATE.desc())
                    .limit(1)
            ))
            .execute();

            resp.status(SC_ACCEPTED);
        } else {
            resp.status(SC_FORBIDDEN);
        }
        return "";
    }
}
