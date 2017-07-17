package com.d3vmoon.at.service;

import com.d3vmoon.at.db.enums.AtDirection;
import com.d3vmoon.at.service.http.NotFoundException;
import com.d3vmoon.at.service.pojo.Preferences;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;
import org.jooq.*;
import org.postgresql.geometric.PGpoint;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.d3vmoon.at.db.Tables.*;
import static com.d3vmoon.at.service.http.Path.PARAM_ID;
import static org.jooq.impl.DSL.*;

public class ATService extends AbstractService {

    final private Field<Integer> indexDef = field("row_number() over (order by 1) as index", Integer.class);
    final private Field<Integer> index = field("index", Integer.class);
    final private Table<Record> trail = table("unnest(points) as point");
    final private Field<Object> point = field("point");

    public List<Map<String, Double>> getCurrentTrail(Request req, Response resp) {
        final int userId = Optional.ofNullable(Ints.tryParse(req.params(PARAM_ID))).orElseThrow(() -> new NotFoundException(req.pathInfo()));
        final AtDirection direction = ctx.select(AT_PREFERENCES.DIRECTION).from(AT_PREFERENCES).where(AT_PREFERENCES.USER.eq(userId)).fetchOne().getValue(AT_PREFERENCES.DIRECTION);

        final SelectOffsetStep<Record1<Integer>> nearestPointOnTrail = select(
                indexDef
        ).from(AT_TRAIL, trail)
        .orderBy(field("{0} <-> ( {1} )",
                point,
                select(AT_POI.POINT)
                .from(AT_POI)
                .where(AT_POI.ID.eq(
                        select(AT_LAST_SHELTER.AT_SHELTER)
                        .from(AT_LAST_SHELTER)
                        .where(AT_LAST_SHELTER.AT_USER.eq(userId))
                ))
        ).asc())
        .limit(1);

        final Condition condition;

        switch (direction) {
            case nobo:
                condition = index.le(nearestPointOnTrail);
                break;
            case sobo:
                condition = index.ge(nearestPointOnTrail);
                break;
            default:
                throw new RuntimeException(String.format("Unhandled direction %s", direction.getName()));
        }

        return ctx.select(point)
        .from(
                select(
                        point,
                        indexDef)
                .from(
                        AT_TRAIL,
                        trail)
                .orderBy(index.asc())
        )
        .where(condition)
        .fetch()
        .map(r -> {
            PGpoint p = r.get(point, PGpoint.class);
            return ImmutableMap.of("lat", p.x, "lng", p.y);
        });
    }

    public List<Map<String, Double>> getFullTrail(Request req, Response resp) {
        return ctx.select(point)
        .from(
               select(
                       point,
                       indexDef)
                       .from(
                               AT_TRAIL,
                               trail)
                       .orderBy(index.asc())
        )
        .fetch()
        .map(r -> {
           PGpoint p = r.get(point, PGpoint.class);
           return ImmutableMap.of("lat", p.x, "lng", p.y);
        });
    }

    public Object getUserTrail(Request req, Response resp) {
        final int userId = Optional.ofNullable(Ints.tryParse(req.params(PARAM_ID))).orElseThrow(() -> new NotFoundException(req.pathInfo()));

        final List<Map<String, Double>> fullTrail = getFullTrail(req, resp);
        final Preferences preferences = new PreferenceService().getPreferences(req, resp);
        final Integer currentIndex = ctx.select(
                indexDef
        ).from(AT_TRAIL, trail)
        .orderBy(field("{0} <-> ( {1} )",
                point,
                select(AT_POI.POINT)
                        .from(AT_POI)
                        .where(AT_POI.ID.eq(
                                select(AT_LAST_SHELTER.AT_SHELTER)
                                        .from(AT_LAST_SHELTER)
                                        .where(AT_LAST_SHELTER.AT_USER.eq(userId))
                        ))
        ).asc())
        .limit(1)
        .fetchOne()
        .value1();

        return ImmutableMap.of(
                "trail", fullTrail,
                "direction", preferences.direction,
                "showFullTrail", preferences.showFullTrail,
                "currentIndex", currentIndex);
    }
}
