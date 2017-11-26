package com.d3vmoon.at.service;

import com.d3vmoon.at.db.enums.AtRole;
import com.d3vmoon.at.service.pojo.Quota;
import com.d3vmoon.at.service.pojo.Quota.ConsumedQuota;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.jooq.Record2;
import spark.Request;
import spark.Response;

import java.time.LocalDate;
import java.util.Optional;

import static com.d3vmoon.at.db.Tables.AT_QUOTA;

public class QuotaService extends AbstractService {

    private final static ImmutableMap<AtRole, Integer> ROLE_TO_QUOTA = Maps.immutableEnumMap(ImmutableMap.of(
            AtRole.unconfirmed, 0,
            AtRole.user, 500,
            AtRole.user_payed, 100_000
    ));

    private SecurityService securityService = new SecurityService(InitUser::init);

    public Quota getQuota(Request req, Response resp) {
        final int userId = getIdFromPath(req);
        final Optional<AtRole> userRole = securityService.getRole(userId);

        if ( ! userRole.isPresent() ) {
            return null;
        }

        return getQuota(userId, userRole.get());
    }

    private Quota getQuota(int userId, AtRole userRole) {
        final Optional<Record2<Short, Integer>> quotaRecord = ctx.select(AT_QUOTA.MONTH, AT_QUOTA.QUOTA)
                                                                              .from(AT_QUOTA)
                                                                              .where(AT_QUOTA.AT_USER.eq(userId))
                                                                              .fetchOptional();

        final short currentMonth = (short) LocalDate.now().getMonth().getValue();
        final int quota;

        if ( ! quotaRecord.isPresent() ) {
            quota = ROLE_TO_QUOTA.get(userRole);
            ctx.insertInto(AT_QUOTA, AT_QUOTA.AT_USER, AT_QUOTA.MONTH, AT_QUOTA.QUOTA)
                    .values(userId, currentMonth, quota)
                    .execute();
        } else if ( quotaRecord.get().get(AT_QUOTA.MONTH) != currentMonth ) {
            quota = ROLE_TO_QUOTA.get(userRole);
            ctx.update(AT_QUOTA)
                    .set(AT_QUOTA.QUOTA, quota)
                    .set(AT_QUOTA.MONTH, currentMonth)
                    .where(AT_QUOTA.AT_USER.eq(userId))
                    .execute();
        } else {
            quota = quotaRecord.get().get(AT_QUOTA.QUOTA);
        }
        return new Quota(currentMonth, quota);
    }

    ConsumedQuota consumeQuota(int userId, AtRole userRole) {
        final ConsumedQuota quota = ConsumedQuota.from(getQuota(userId, userRole));

        ctx.update(AT_QUOTA)
                .set(AT_QUOTA.QUOTA, quota.quota)
                .set(AT_QUOTA.MONTH, quota.month)
                .where(AT_QUOTA.AT_USER.eq(userId))
                .execute();

        return quota;
    }

}
