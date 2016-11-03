package com.d3vmoon.at.service;

import com.google.common.collect.ImmutableMap;
import org.postgresql.geometric.PGpoint;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Map;

import static com.d3vmoon.at.db.Tables.AT_LAST_SHELTER;
import static com.d3vmoon.at.db.Tables.AT_SHELTER;
import static com.d3vmoon.at.db.Tables.AT_TRAIL;
import static org.jooq.impl.DSL.*;

public class ATService extends AbstractService {

    public List<Map<String, Double>> getCurrentTrail(Request req, Response resp) {
    return ctx.select(AT_TRAIL.POINT)
            .from(AT_TRAIL)
            .where(AT_TRAIL.ID.le(
                    select(AT_TRAIL.ID)
                    .from(AT_TRAIL)
                    .orderBy(field("{0} <-> ( {1} )",
                            AT_TRAIL.POINT,
                            select(AT_SHELTER.POINT)
                            .from(AT_SHELTER)
                            .where(AT_SHELTER.ID.eq(
                                    select(AT_LAST_SHELTER.AT_SHELTER)
                                    .from(AT_LAST_SHELTER)
                            ))
                    ).asc())
                    .limit(1)
            ))
            .orderBy(AT_TRAIL.ID.asc())
            .fetch()
            .map(r -> {
                PGpoint point = r.get(AT_TRAIL.POINT, PGpoint.class);
                return ImmutableMap.of("lat", point.x, "lng", point.y);
            });
    }
}
