package com.tencent.tsf.femas.common.statistic;

public class PartialAggregation extends AbstractAggregation {

    private long epochMillis;

    PartialAggregation(long epochMillis) {
        this.epochMillis = epochMillis;
    }

    void reset(long epochMillis) {
        this.epochMillis = epochMillis;
        this.totalDurationInMillis = 0;
        this.numberOfSlowCalls.reset();
        this.numberOfFailedCalls.reset();
        this.numberOfCalls.reset();
        this.numberOfBlockCalls.reset();
    }

    public long getEpochMillis() {
        return epochMillis;
    }
}