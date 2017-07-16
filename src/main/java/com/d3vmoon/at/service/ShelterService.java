package com.d3vmoon.at.service;

import com.d3vmoon.at.db.tables.records.AtPoiRecord;
import com.d3vmoon.at.service.http.NotFoundException;
import com.d3vmoon.at.service.pojo.Shelter;
import com.google.common.primitives.Ints;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.postgresql.geometric.PGpoint;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Optional;

import static com.d3vmoon.at.db.Tables.AT_LAST_SHELTER;
import static com.d3vmoon.at.db.Tables.AT_POI;
import static com.d3vmoon.at.service.SecurityService.PARAM_USER_ID;
import static com.d3vmoon.at.service.http.Path.PARAM_ID;

public class ShelterService extends AbstractService {

    private static final RecordMapper<AtPoiRecord, Shelter> RECORD_MAPPER = r -> new Shelter(
            r.getId(),
            r.getName(),
            (PGpoint) r.getPoint(),
            r.getComment(),
            Optional.ofNullable(r.getCampground()).orElse(Boolean.FALSE)
    );

    public List<Shelter> getShelters(Request req, Response resp) {
        return ctx
                .selectFrom(AT_POI)
                .orderBy(DSL.field("point[0]"))
                .fetch()
                .map(RECORD_MAPPER);
    }

    public Shelter getShelter(Request req, Response resp) {
        final String idParam = req.params(PARAM_ID);
        if ("last".equalsIgnoreCase(idParam)) {
            final int userId = Integer.parseInt(req.queryParams(PARAM_USER_ID));
            return ctx
                    .selectFrom(AT_POI)
                    .where(AT_POI.ID.eq(ctx
                            .select(AT_LAST_SHELTER.AT_SHELTER)
                            .from(AT_LAST_SHELTER)
                            .where(AT_LAST_SHELTER.AT_USER.eq(userId)))
                    ).fetch()
                    .map(RECORD_MAPPER).get(0);
        } else {
            final Integer id = Optional.ofNullable(Ints.tryParse(idParam)).orElseThrow(() -> new NotFoundException(req.pathInfo()));
            return ctx
                    .selectFrom(AT_POI)
                    .where(AT_POI.ID.eq(id)).fetch()
                    .map(RECORD_MAPPER).get(0);
        }
    }

    public String setShelter(Request req, Response resp) {
        final Shelter shelter = gson.fromJson(req.body(), Shelter.class);
        final Integer userId = req.attribute(PARAM_USER_ID);
        final String idParam = req.params(PARAM_ID);
        if ("last".equalsIgnoreCase(idParam)) {
            ctx.update(AT_LAST_SHELTER)
                    .set(AT_LAST_SHELTER.AT_SHELTER, shelter.id)
                    .where(AT_LAST_SHELTER.AT_USER.eq(userId))
                    .execute();

            resp.status(202);
            return "";
        } else {
            resp.status(403);
            return "";
        }
    }

}
