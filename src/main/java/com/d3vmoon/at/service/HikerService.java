package com.d3vmoon.at.service;

import com.d3vmoon.at.service.pojo.Hiker;
import spark.Request;
import spark.Response;

import java.util.List;

import static com.d3vmoon.at.db.Tables.AT_PREFERENCES;
import static org.jooq.impl.DSL.field;

public class HikerService extends AbstractService {

    public List<Hiker> getHikers(Request request, Response response) {
        return ctx.select(
                    AT_PREFERENCES.USER,
                    AT_PREFERENCES.TRAILNAME,
                    AT_PREFERENCES.REALNAME,
                    AT_PREFERENCES.DIRECTION,
                    field("extract(year from lower({0}))", Integer.class, AT_PREFERENCES.START_END).as("year")
                )
               .from(AT_PREFERENCES)
               .where(AT_PREFERENCES.USER.gt(1))
               .orderBy(field("year").desc(), AT_PREFERENCES.USER.asc())
               .fetchInto(Hiker.class);
    }
}
