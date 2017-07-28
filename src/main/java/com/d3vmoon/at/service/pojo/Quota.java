package com.d3vmoon.at.service.pojo;

public class Quota {

    public final short month;
    public final int quota;

    public Quota(short month, int quota) {
        this.month = month;
        this.quota = quota;
    }

    public static class ConsumedQuota extends Quota {

        public final int previousQuota;

        private ConsumedQuota(short month, int quota, int previousQuota) {
            super(month, quota);
            this.previousQuota = previousQuota;
        }

        public static ConsumedQuota from(Quota oldQuota) {
            return new ConsumedQuota(oldQuota.month, Math.max(oldQuota.quota - 1, 0), oldQuota.quota);
        }
    }
}
