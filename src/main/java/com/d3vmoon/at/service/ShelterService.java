package com.d3vmoon.at.service;

import com.d3vmoon.at.service.pojo.Coordinate;
import com.d3vmoon.at.service.pojo.Shelter;
import org.jooq.impl.DSL;
import org.postgresql.geometric.PGpoint;
import spark.Request;
import spark.Response;

import java.util.List;

import static com.d3vmoon.at.db.Tables.AT_SHELTER;

public class ShelterService extends AbstractService {

    public List<Shelter> getShelters(Request req, Response resp) {
        return ctx()
                .selectFrom(AT_SHELTER)
                .orderBy(DSL.field("point[0]"))
                .fetch()
                .map(r -> {
                    PGpoint point = r.get(AT_SHELTER.POINT, PGpoint.class);
                    return new Shelter(
                            r.get(AT_SHELTER.ID),
                            r.get(AT_SHELTER.NAME),
                            new Coordinate(point.x, point.y),
                            r.get(AT_SHELTER.COMMENT),
                            r.get(AT_SHELTER.CAMPGROUND)
                    );
                });
    }

}
