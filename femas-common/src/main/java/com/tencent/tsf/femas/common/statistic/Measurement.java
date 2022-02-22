package com.tencent.tsf.femas.common.statistic;

class Measurement extends AbstractAggregation {

    void reset() {
        this.totalDurationInMillis = 0;
        this.numberOfSlowCalls.reset();
        this.numberOfFailedCalls.reset();
        this.numberOfCalls.reset();
    }

}
