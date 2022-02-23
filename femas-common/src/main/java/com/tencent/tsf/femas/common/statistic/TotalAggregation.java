package com.tencent.tsf.femas.common.statistic;

class TotalAggregation extends AbstractAggregation {

    void removeBucket(AbstractAggregation bucket) {
        this.totalDurationInMillis -= bucket.totalDurationInMillis;
        this.numberOfSlowCalls.add(-bucket.numberOfSlowCalls.intValue());
        this.numberOfSlowFailedCalls.add(-bucket.numberOfSlowFailedCalls.intValue());
        this.numberOfFailedCalls.add(-bucket.numberOfFailedCalls.intValue());
        this.numberOfBlockCalls.add(-bucket.numberOfBlockCalls.intValue());
        this.numberOfCalls.add(-bucket.numberOfCalls.intValue());
    }
}
