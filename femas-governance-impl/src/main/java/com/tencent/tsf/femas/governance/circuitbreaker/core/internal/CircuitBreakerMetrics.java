/*
 *
 *  Copyright 2016 Robert Winkler and Bohdan Storozhuk
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package com.tencent.tsf.femas.governance.circuitbreaker.core.internal;


import com.tencent.tsf.femas.common.statistic.FixedSizeSlidingWindowMetrics;
import com.tencent.tsf.femas.common.statistic.Metrics;
import com.tencent.tsf.femas.common.statistic.SecondsSlidingTimeWindowMetrics;
import com.tencent.tsf.femas.common.statistic.Snapshot;
import com.tencent.tsf.femas.governance.circuitbreaker.core.CircuitBreaker;
import com.tencent.tsf.femas.governance.circuitbreaker.core.CircuitBreakerConfig;
import java.time.Clock;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public class CircuitBreakerMetrics implements CircuitBreaker.Metrics {

    private final Metrics metrics;
    private final float failureRateThreshold;
    private final float slowCallRateThreshold;
    private final long slowCallDurationThresholdInNanos;
    private final LongAdder numberOfNotPermittedCalls;
    private int minimumNumberOfCalls;

    private CircuitBreakerMetrics(int slidingWindowSize,
            CircuitBreakerConfig.SlidingWindowType slidingWindowType,
            CircuitBreakerConfig circuitBreakerConfig,
            Clock clock) {
        if (slidingWindowType == CircuitBreakerConfig.SlidingWindowType.COUNT_BASED) {
            this.metrics = new FixedSizeSlidingWindowMetrics(slidingWindowSize);
            this.minimumNumberOfCalls = Math
                    .min(circuitBreakerConfig.getMinimumNumberOfCalls(), slidingWindowSize);
        } else {
            this.metrics = new SecondsSlidingTimeWindowMetrics(slidingWindowSize);
            this.minimumNumberOfCalls = circuitBreakerConfig.getMinimumNumberOfCalls();
        }
        this.failureRateThreshold = circuitBreakerConfig.getFailureRateThreshold();
        this.slowCallRateThreshold = circuitBreakerConfig.getSlowCallRateThreshold();
        this.slowCallDurationThresholdInNanos = circuitBreakerConfig.getSlowCallDurationThreshold()
                .toNanos();
        this.numberOfNotPermittedCalls = new LongAdder();
    }

    private CircuitBreakerMetrics(int slidingWindowSize,
            CircuitBreakerConfig circuitBreakerConfig, Clock clock) {
        this(slidingWindowSize, circuitBreakerConfig.getSlidingWindowType(), circuitBreakerConfig, clock);
    }

    static CircuitBreakerMetrics forClosed(CircuitBreakerConfig circuitBreakerConfig, Clock clock) {
        return new CircuitBreakerMetrics(circuitBreakerConfig.getSlidingWindowSize(),
                circuitBreakerConfig, clock);
    }

    static CircuitBreakerMetrics forHalfOpen(int permittedNumberOfCallsInHalfOpenState,
            CircuitBreakerConfig circuitBreakerConfig, Clock clock) {
        return new CircuitBreakerMetrics(permittedNumberOfCallsInHalfOpenState,
                CircuitBreakerConfig.SlidingWindowType.COUNT_BASED, circuitBreakerConfig, clock);
    }

    static CircuitBreakerMetrics forForcedOpen(CircuitBreakerConfig circuitBreakerConfig, Clock clock) {
        return new CircuitBreakerMetrics(0, CircuitBreakerConfig.SlidingWindowType.COUNT_BASED,
                circuitBreakerConfig, clock);
    }

    static CircuitBreakerMetrics forDisabled(CircuitBreakerConfig circuitBreakerConfig, Clock clock) {
        return new CircuitBreakerMetrics(0, CircuitBreakerConfig.SlidingWindowType.COUNT_BASED,
                circuitBreakerConfig, clock);
    }

    static CircuitBreakerMetrics forMetricsOnly(CircuitBreakerConfig circuitBreakerConfig, Clock clock) {
        return forClosed(circuitBreakerConfig, clock);
    }

    /**
     * Records a call which was not permitted, because the CircuitBreaker state is OPEN.
     */
    void onCallNotPermitted() {
        numberOfNotPermittedCalls.increment();
    }

    /**
     * Records a successful call and checks if the thresholds are exceeded.
     *
     * @return the result of the check
     */
    public Result onSuccess(long duration, TimeUnit durationUnit) {
        Snapshot snapshot;
        if (durationUnit.toNanos(duration) > slowCallDurationThresholdInNanos) {
            snapshot = metrics.record(duration, durationUnit, Metrics.Outcome.SLOW_SUCCESS);
        } else {
            snapshot = metrics.record(duration, durationUnit, Metrics.Outcome.SUCCESS);
        }
        return checkIfThresholdsExceeded(snapshot);
    }

    /**
     * Records a failed call and checks if the thresholds are exceeded.
     *
     * @return the result of the check
     */
    public Result onError(long duration, TimeUnit durationUnit) {
        Snapshot snapshot;
        if (durationUnit.toNanos(duration) > slowCallDurationThresholdInNanos) {
            snapshot = metrics.record(duration, durationUnit, Metrics.Outcome.SLOW_ERROR);
        } else {
            snapshot = metrics.record(duration, durationUnit, Metrics.Outcome.ERROR);
        }
        return checkIfThresholdsExceeded(snapshot);
    }

    /**
     * Checks if the failure rate is above the threshold or if the slow calls percentage is above
     * the threshold.
     *
     * @param snapshot a metrics snapshot
     * @return false, if the thresholds haven't been exceeded.
     */
    private Result checkIfThresholdsExceeded(Snapshot snapshot) {
        float failureRateInPercentage = getFailureRate(snapshot);
        float slowCallsInPercentage = getSlowCallRate(snapshot);

        if (failureRateInPercentage == -1 || slowCallsInPercentage == -1) {
            return Result.BELOW_MINIMUM_CALLS_THRESHOLD;
        }
        if (failureRateInPercentage >= failureRateThreshold
                && slowCallsInPercentage >= slowCallRateThreshold) {
            return Result.ABOVE_THRESHOLDS;
        }
        if (failureRateInPercentage >= failureRateThreshold) {
            return Result.FAILURE_RATE_ABOVE_THRESHOLDS;
        }

        if (slowCallsInPercentage >= slowCallRateThreshold) {
            return Result.SLOW_CALL_RATE_ABOVE_THRESHOLDS;
        }
        return Result.BELOW_THRESHOLDS;
    }

    private float getSlowCallRate(Snapshot snapshot) {
        int bufferedCalls = snapshot.getTotalNumberOfCalls();
        if (bufferedCalls == 0 || bufferedCalls < minimumNumberOfCalls) {
            return -1.0f;
        }
        return snapshot.getSlowCallRate();
    }

    private float getFailureRate(Snapshot snapshot) {
        int bufferedCalls = snapshot.getTotalNumberOfCalls();
        if (bufferedCalls == 0 || bufferedCalls < minimumNumberOfCalls) {
            return -1.0f;
        }
        return snapshot.getFailureRate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getFailureRate() {
        return getFailureRate(metrics.getSnapshot());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getSlowCallRate() {
        return getSlowCallRate(metrics.getSnapshot());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfSuccessfulCalls() {
        return this.metrics.getSnapshot().getNumberOfSuccessfulCalls();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfBufferedCalls() {
        return this.metrics.getSnapshot().getTotalNumberOfCalls();
    }

    @Override
    public int getNumberOfFailedCalls() {
        return this.metrics.getSnapshot().getNumberOfFailedCalls();
    }

    @Override
    public int getNumberOfSlowCalls() {
        return this.metrics.getSnapshot().getTotalNumberOfSlowCalls();
    }

    @Override
    public int getNumberOfSlowSuccessfulCalls() {
        return this.metrics.getSnapshot().getNumberOfSlowSuccessfulCalls();
    }

    @Override
    public int getNumberOfSlowFailedCalls() {
        return this.metrics.getSnapshot().getNumberOfSlowFailedCalls();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getNumberOfNotPermittedCalls() {
        return this.numberOfNotPermittedCalls.sum();
    }

    enum Result {
        BELOW_THRESHOLDS,
        FAILURE_RATE_ABOVE_THRESHOLDS,
        SLOW_CALL_RATE_ABOVE_THRESHOLDS,
        ABOVE_THRESHOLDS,
        BELOW_MINIMUM_CALLS_THRESHOLD;

        public static boolean hasExceededThresholds(Result result) {
            return hasFailureRateExceededThreshold(result) ||
                    hasSlowCallRateExceededThreshold(result);
        }

        public static boolean hasFailureRateExceededThreshold(Result result) {
            return result == ABOVE_THRESHOLDS || result == FAILURE_RATE_ABOVE_THRESHOLDS;
        }

        public static boolean hasSlowCallRateExceededThreshold(Result result) {
            return result == ABOVE_THRESHOLDS || result == SLOW_CALL_RATE_ABOVE_THRESHOLDS;
        }
    }
}
