package com.d3vmoon.at.service;

import org.jooq.Configuration;
import org.jooq.impl.DSL;

import static com.d3vmoon.at.db.Tables.AT_PREFERENCES;

public class InitUser {

    public static void init(Configuration config, int userId) {
        DSL.using(config).insertInto(AT_PREFERENCES)
                .set(AT_PREFERENCES.USER, userId)
                .execute();
    }
}
