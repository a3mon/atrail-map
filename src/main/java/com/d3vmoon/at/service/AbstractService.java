package com.d3vmoon.at.service;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.postgresql.ds.PGPoolingDataSource;

public class AbstractService {

    private final DSLContext ctx;

    public AbstractService() {
        PGPoolingDataSource ds = new PGPoolingDataSource();
        ds.setUrl(System.getenv("JDBC_DATABASE_URL"));

        ctx =  DSL.using(ds, SQLDialect.POSTGRES_9_5);
    }

    protected DSLContext ctx() {
        return ctx;
    }
}
