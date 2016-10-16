package com.d3vmoon.at.service;

import com.google.common.collect.ImmutableMap;
import org.postgresql.geometric.PGpoint;
import spark.Request;
import spark.Response;
import java.util.List;
import java.util.Map;

import static com.d3vmoon.at.db.Tables.AT_TRAIL;

public class ATService extends AbstractService {

    public List<Map<String, Double>> getCurrentTrail(Request req, Response resp) {
        return ctx().fetch(
                "  select point " +
                "    from at_trail " +
                "   where id <= ( " +
                "          select id " +
                "            from ( " +
                "                  select id, point, point <-> ( " +
                "                          select point " +
                "                            from at_shelter  " +
                "                           where id = ( " +
                "                                  select at_shelter " +
                "                                    from at_last_shelter " +
                "                                ) " +
                "                        ) as dist " +
                "                    from at_trail " +
                "                order by dist asc  " +
                "                   limit 1 " +
                "                ) t " +
                "        ) " +
                "order by id asc"
        )
        .map(r -> {
            PGpoint pGpoint = r.get(AT_TRAIL.POINT, PGpoint.class);
            return ImmutableMap.of("lat", pGpoint.x, "lng", pGpoint.y);
        });
    }
}
