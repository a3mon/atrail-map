package com.d3vmoon.at.service;

import com.d3vmoon.at.db.enums.AtDirection;
import com.google.common.collect.ImmutableMap;
import org.jooq.Condition;
import org.jooq.Record1;
import org.jooq.SelectOffsetStep;
import org.postgresql.geometric.PGpoint;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Map;

import static com.d3vmoon.at.db.Tables.*;
import static com.d3vmoon.at.service.SecurityService.PARAM_USER_ID;
import static org.jooq.impl.DSL.*;

public class ATService extends AbstractService {



    public List<Map<String, Double>> getCurrentTrail(Request req, Response resp) {
        final int userId = Integer.parseInt(req.queryParams(PARAM_USER_ID));
        final AtDirection direction = ctx.select(AT_PREFERENCS.DIRECTION).from(AT_PREFERENCS).where(AT_PREFERENCS.USER.eq(userId)).fetchOne().getValue(AT_PREFERENCS.DIRECTION);

        final SelectOffsetStep<Record1<Integer>> nearestPointOnTrail = select(AT_TRAIL.ID)
                .from(AT_TRAIL)
                .orderBy(field("{0} <-> ( {1} )",
                        AT_TRAIL.POINT,
                        select(AT_SHELTER.POINT)
                                .from(AT_SHELTER)
                                .where(AT_SHELTER.ID.eq(
                                        select(AT_LAST_SHELTER.AT_SHELTER)
                                                .from(AT_LAST_SHELTER)
                                                .where(AT_LAST_SHELTER.AT_USER.eq(userId))
                                ))
                ).asc())
                .limit(1);

        final Condition condition;

        switch (direction) {
            case nobo:
                condition = AT_TRAIL.ID.le(nearestPointOnTrail);
                break;
            case sobo:
                condition = AT_TRAIL.ID.ge(nearestPointOnTrail);
                break;
            default:
                throw new RuntimeException(String.format("Unhandled direction %s", direction.getName()));
        }

        return ctx.select(AT_TRAIL.POINT)
            .from(AT_TRAIL)
            .where(condition)
            .orderBy(AT_TRAIL.ID.asc())
            .fetch()
            .map(r -> {
                PGpoint point = r.get(AT_TRAIL.POINT, PGpoint.class);
                return ImmutableMap.of("lat", point.x, "lng", point.y);
            });
    }
}
