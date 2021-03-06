package com.d3vmoon.at.service;

import com.d3vmoon.at.Main;
import com.d3vmoon.at.service.http.NotFoundException;
import com.google.common.primitives.Ints;
import com.google.gson.Gson;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.postgresql.ds.PGPoolingDataSource;
import spark.Request;

import java.util.Optional;

import static com.d3vmoon.at.service.http.Path.PARAM_ID;

public class AbstractService {

    protected final DSLContext ctx;
    protected final Gson gson = Main.gson;

    public AbstractService() {
        PGPoolingDataSource ds = new PGPoolingDataSource();
        ds.setUrl(System.getenv("JDBC_DATABASE_URL"));

        ctx =  DSL.using(ds, SQLDialect.POSTGRES_9_5);
    }

    public static Integer getIdFromPath(Request req) {
        return Optional.ofNullable(Ints.tryParse(req.params(PARAM_ID))).orElseThrow(() -> new NotFoundException(req.pathInfo()));
    }
}
