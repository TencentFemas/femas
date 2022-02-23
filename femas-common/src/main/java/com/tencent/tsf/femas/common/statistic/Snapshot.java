package com.tencent.tsf.femas.common.statistic;

import java.time.Duration;

public interface Snapshot {

    /**
     * Returns the current total duration of all calls.
     *
     * @return the current total duration of all calls
     */
    Duration getTotalDuration();

    /**
     * Returns the current average duration of all calls.
     *
     * @return the current average duration of all calls
     */
    Duration getAverageDuration();

    /**
     * Returns the current number of calls which were slower than a certain threshold.
     *
     * @return the current number of calls which were slower than a certain threshold
     */
    int getTotalNumberOfSlowCalls();

    /**
     * Returns the current number of successful calls which were slower than a certain threshold.
     *
     * @return the current number of successful calls which were slower than a certain threshold
     */
    int getNumberOfSlowSuccessfulCalls();

    /**
     * Returns the current number of failed calls which were slower than a certain threshold.
     *
     * @return the current number of failed calls which were slower than a certain threshold
     */
    int getNumberOfSlowFailedCalls();

    /**
     * Returns the current percentage of calls which were slower than a certain threshold.
     *
     * @return the current percentage of calls which were slower than a certain threshold
     */
    float getSlowCallRate();

    /**
     * Returns the current number of successful calls.
     *
     * @return the current number of successful calls
     */
    int getNumberOfSuccessfulCalls();

    /**
     * Returns the current number of failed calls.
     *
     * @return the current number of failed calls
     */
    int getNumberOfFailedCalls();

    /**
     * Returns the current total number of all calls.
     *
     * @return the current total number of all calls
     */
    int getTotalNumberOfCalls();

    /**
     * Returns the current failure rate in percentage.
     *
     * @return the current  failure rate in percentage
     */
    float getFailureRate();
}
