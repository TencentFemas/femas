package com.tencent.tsf.femas.common.statistic;

import com.tencent.tsf.femas.common.util.backport.LongAdder;
import java.util.concurrent.TimeUnit;

/**
 * @author zhixinzxliu
 */
class AbstractAggregation {

    long totalDurationInMillis = 0;

    LongAdder numberOfSlowCalls = new LongAdder();
    LongAdder numberOfSlowFailedCalls = new LongAdder();
    LongAdder numberOfFailedCalls = new LongAdder();
    LongAdder numberOfBlockCalls = new LongAdder();
    LongAdder numberOfCalls = new LongAdder();

    void record(Metrics.Outcome outcome) {
        record(0, null, outcome);
    }

    void record(long duration, TimeUnit durationUnit, Metrics.Outcome outcome) {
        this.numberOfCalls.increment();

        if (duration > 0 && durationUnit != null) {
            this.totalDurationInMillis += durationUnit.toMillis(duration);
        }

        switch (outcome) {
            case BLOCK:
                numberOfBlockCalls.increment();
                break;

            case SLOW_SUCCESS:
                numberOfSlowCalls.increment();
                break;

            case SLOW_ERROR:
                numberOfSlowCalls.increment();
                numberOfFailedCalls.increment();
                numberOfSlowFailedCalls.increment();
                break;

            case ERROR:
                numberOfFailedCalls.increment();
                break;
        }
    }
}
