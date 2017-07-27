package com.d3vmoon.at.service;

import com.d3vmoon.at.db.tables.records.AtPoiRecord;
import com.d3vmoon.at.service.pojo.Shelter;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.postgresql.geometric.PGpoint;
import spark.Request;
import spark.Response;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.d3vmoon.at.db.Tables.AT_LAST_POI;
import static com.d3vmoon.at.db.Tables.AT_POI;
import static com.d3vmoon.at.db.Tables.AT_TIMELINE;
import static com.d3vmoon.at.service.SecurityService.PARAM_USER_ID;
import static com.d3vmoon.at.service.SecurityService.getUserId;
import static com.d3vmoon.at.service.http.Path.PARAM_ID;
import static javax.servlet.http.HttpServletResponse.SC_ACCEPTED;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

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
                            .select(AT_LAST_POI.AT_POI)
                            .from(AT_LAST_POI)
                            .where(AT_LAST_POI.AT_USER.eq(userId)))
                    ).fetch()
                    .map(RECORD_MAPPER).get(0);
        } else {
            final Integer id = getIdFromPath(req);
            return ctx
                    .selectFrom(AT_POI)
                    .where(AT_POI.ID.eq(id)).fetch()
                    .map(RECORD_MAPPER).get(0);
        }
    }

}
