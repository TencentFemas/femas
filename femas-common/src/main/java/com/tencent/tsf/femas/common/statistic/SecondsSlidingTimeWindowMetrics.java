package com.tencent.tsf.femas.common.statistic;

public class SecondsSlidingTimeWindowMetrics extends SlidingTimeWindowMetrics {

    /**
     * Creates a new {@link SecondsSlidingTimeWindowMetrics} with the given clock and window of time.
     *
     * @param timeWindowSizeInSeconds the window time size in seconds
     */
    public SecondsSlidingTimeWindowMetrics(int timeWindowSizeInSeconds) {
        super(timeWindowSizeInSeconds, timeWindowSizeInSeconds * 1000);
    }
}