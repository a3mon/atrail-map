package com.d3vmoon.at.service;

import com.d3vmoon.at.Main;
import com.d3vmoon.at.service.http.NotFoundException;
import com.d3vmoon.at.service.pojo.errors.BaseError;
import com.google.common.primitives.Ints;
import com.google.gson.Gson;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.postgresql.ds.PGPoolingDataSource;
import spark.Request;
import spark.Spark;

import java.util.Optional;

import static com.d3vmoon.at.service.http.Path.PARAM_ID;

class AbstractService {

    final DSLContext ctx;
    final Gson gson = Main.gson;

    AbstractService() {
        PGPoolingDataSource ds = new PGPoolingDataSource();
        ds.setUrl(System.getenv("JDBC_DATABASE_URL"));

        ctx =  DSL.using(ds, SQLDialect.POSTGRES_9_5);
    }

    <T> T halt(BaseError error) {
        Spark.halt(error.status, gson.toJson(error));
        /* Should never reach the return. Only cast to make compiler happy. */
        return (T) error;
    }

    <T> T  halt(int status) {
        Spark.halt(status);
        return null;
    }


    static Integer getIdFromPath(Request req) {
        return Optional.ofNullable(Ints.tryParse(req.params(PARAM_ID))).orElseThrow(() -> new NotFoundException(req.pathInfo()));
    }

}
