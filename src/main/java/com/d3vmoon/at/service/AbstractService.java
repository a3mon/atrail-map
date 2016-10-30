package com.d3vmoon.at.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.postgresql.ds.PGPoolingDataSource;

public class AbstractService {

    protected final DSLContext ctx;
    protected final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public AbstractService() {
        PGPoolingDataSource ds = new PGPoolingDataSource();
        ds.setUrl(System.getenv("JDBC_DATABASE_URL"));

        ctx =  DSL.using(ds, SQLDialect.POSTGRES_9_5);
    }
}
