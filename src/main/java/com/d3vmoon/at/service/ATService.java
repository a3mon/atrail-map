package com.d3vmoon.at.service;

import com.google.common.collect.ImmutableMap;
import org.postgresql.geometric.PGpoint;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Map;

import static com.d3vmoon.at.db.Tables.AT_TRAIL;
import static org.jooq.impl.DSL.*;

public class ATService extends AbstractService {

    public List<Map<String, Double>> getCurrentTrail(Request req, Response resp) {
    return ctx.select(AT_TRAIL.POINT)
            .from(AT_TRAIL)
            .where(AT_TRAIL.ID.le(
                    ctx.select(field("id", Integer.class))
                    .from(
                            ctx.select(AT_TRAIL.ID, AT_TRAIL.POINT, field(
                                    "point <-> ( " +
                                    "  select point " +
                                    "    from at_shelter  " +
                                    "   where id = ( " +
                                    "          select at_shelter " +
                                    "            from at_last_shelter " +
                                    "        ) "+
                                    ")"
                            ).as("dist"))
                            .from(AT_TRAIL)
                            .orderBy(field("dist").asc())
                            .limit(1)
                    )
            ))
            .orderBy(AT_TRAIL.ID.asc())
            .fetch()
            .map(r -> {
                PGpoint point = r.get(AT_TRAIL.POINT, PGpoint.class);
                return ImmutableMap.of("lat", point.x, "lng", point.y);
            });
    }
}
