package com.d3vmoon.at.service.pojo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuotaTest {

    @Test
    void testQuotaReduction() {
        final Quota quota = new Quota((short)3, 27);
        final Quota.ConsumedQuota actual = Quota.ConsumedQuota.from(quota);

        assertEquals(26, actual.quota, "quota");

        assertEquals(27, actual.previousQuota, "previous quota");
        assertEquals((short)3, actual.month, "month");
    }

    @Test
    void testQuotaLowerBound() {
        final Quota quota = new Quota((short)7, 0);
        final Quota.ConsumedQuota actual = Quota.ConsumedQuota.from(quota);

        assertEquals(0, actual.quota, "quota");

        assertEquals(0, actual.previousQuota, "previous quota");
        assertEquals((short)7, actual.month, "month");
    }

}