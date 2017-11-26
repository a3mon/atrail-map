package com.d3vmoon.at.service;

import com.d3vmoon.at.db.enums.AtRole;
import com.d3vmoon.at.service.pojo.Coordinate;
import com.d3vmoon.at.service.pojo.Preferences;
import com.d3vmoon.at.service.pojo.Quota;
import com.d3vmoon.at.service.pojo.errors.Errors;
import com.google.common.collect.ImmutableMap;
import org.jooq.*;
import org.postgresql.geometric.PGpoint;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Optional;

import static com.d3vmoon.at.db.Tables.*;
import static org.jooq.impl.DSL.*;

public class ATService extends AbstractService {

    private final Field<Integer> indexDef = field("row_number() over (order by 1) as index", Integer.class);
    private final Field<Integer> index = field("index", Integer.class);
    private final Table<Record> trail = table("unnest(points) as point");
    private final Field<Object> point = field("point");

    private final SecurityService securityService = new SecurityService(InitUser::init);
    private final PreferenceService preferenceService = new PreferenceService();
    private final QuotaService quotaService = new QuotaService();

    public List<Coordinate> getFullTrail(Request req, Response resp) {
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
        .map(r -> Coordinate.of(r.get(point, PGpoint.class)));
    }

    public Object getUserTrail(Request req, Response resp) {
        final int userId = getIdFromPath(req);
        final Optional<AtRole> userRole = securityService.getRole(userId);

        if ( ! userRole.isPresent() ) {
            return halt(Errors.NOT_FOUND_RESPONSE);
        }

        final Quota.ConsumedQuota quota = quotaService.consumeQuota(userId, userRole.get());
        if (quota.previousQuota <= 0 ) {
            return halt(Errors.NO_QUOTA_RESPNSE);
        }

        final List<Coordinate> fullTrail = getFullTrail(req, resp);
        final Preferences preferences = preferenceService.getPreferences(req, resp);
        final int currentIndex = ctx.select(
                indexDef
        ).from(AT_TRAIL, trail)
        .orderBy(field("{0} <-> ( {1} )",
                point,
                select(AT_LAST_POI.POINT)
                .from(AT_LAST_POI)
                .where(AT_LAST_POI.AT_USER.eq(userId))
        ).asc())
        .limit(1)
        .fetchOne()
        .value1();

        return ImmutableMap.of(
                "trail", fullTrail,
                "direction", preferences.direction,
                "showFullTrail", preferences.showFullTrail,
                "currentIndex", currentIndex,
                "quota", quota.quota);
    }
}
