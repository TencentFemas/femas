/*
 *
 *  Copyright 2019 Robert Winkler and Bohdan Storozhuk
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

package com.tencent.tsf.femas.common.statistic;

import static org.assertj.core.api.Assertions.assertThat;

import com.tencent.tsf.femas.common.util.TimeUtil;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class SlidingTimeWindowMetricsTest {

    @Test
    public void checkInitialBucketCreation() {
        SecondsSlidingTimeWindowMetrics metrics = new SecondsSlidingTimeWindowMetrics(5);

        PartialAggregation[] buckets = metrics.partialAggregations;

        long epochSecond = TimeUtil.currentTimeMillis() / 1000;
        for (int i = 0; i < buckets.length; i++) {
            PartialAggregation bucket = buckets[i];
            assertThat(bucket.getEpochMillis() / 1000).isEqualTo(epochSecond + i);
        }

        Snapshot snapshot = metrics.getSnapshot();

        assertThat(snapshot.getTotalNumberOfCalls()).isEqualTo(0);
        assertThat(snapshot.getNumberOfSuccessfulCalls()).isEqualTo(0);
        assertThat(snapshot.getNumberOfFailedCalls()).isEqualTo(0);
        assertThat(snapshot.getTotalDuration().toMillis()).isEqualTo(0);
    }

    @Test
    public void testRecordSuccess() {
        Metrics metrics = new SecondsSlidingTimeWindowMetrics(5);

        metrics.record(100, TimeUnit.MILLISECONDS, Metrics.Outcome.SUCCESS);

        Snapshot snapshot = metrics.getSnapshot();
        assertThat(snapshot.getTotalNumberOfCalls()).isEqualTo(1);
        assertThat(snapshot.getNumberOfSuccessfulCalls()).isEqualTo(1);
        assertThat(snapshot.getNumberOfFailedCalls()).isEqualTo(0);
        assertThat(snapshot.getTotalNumberOfSlowCalls()).isEqualTo(0);
        assertThat(snapshot.getNumberOfSlowSuccessfulCalls()).isEqualTo(0);
        assertThat(snapshot.getNumberOfSlowFailedCalls()).isEqualTo(0);
        assertThat(snapshot.getTotalDuration().toMillis()).isEqualTo(100);
        assertThat(snapshot.getAverageDuration().toMillis()).isEqualTo(100);
        assertThat(snapshot.getFailureRate()).isEqualTo(0);
    }

    @Test
    public void testRecordError() {
        Metrics metrics = new SecondsSlidingTimeWindowMetrics(5);

        metrics.record(100, TimeUnit.MILLISECONDS, Metrics.Outcome.ERROR);
        Snapshot snapshot = metrics.getSnapshot();
        assertThat(snapshot.getTotalNumberOfCalls()).isEqualTo(1);
        assertThat(snapshot.getNumberOfSuccessfulCalls()).isEqualTo(0);
        assertThat(snapshot.getNumberOfFailedCalls()).isEqualTo(1);
        assertThat(snapshot.getTotalNumberOfSlowCalls()).isEqualTo(0);
        assertThat(snapshot.getNumberOfSlowSuccessfulCalls()).isEqualTo(0);
        assertThat(snapshot.getNumberOfSlowFailedCalls()).isEqualTo(0);
        assertThat(snapshot.getTotalDuration().toMillis()).isEqualTo(100);
        assertThat(snapshot.getAverageDuration().toMillis()).isEqualTo(100);
        assertThat(snapshot.getFailureRate()).isEqualTo(100);
    }

    @Test
    public void testRecordSlowSuccess() {
        Metrics metrics = new SecondsSlidingTimeWindowMetrics(5);

        metrics.record(100, TimeUnit.MILLISECONDS, Metrics.Outcome.SLOW_SUCCESS);

        Snapshot snapshot = metrics.getSnapshot();
        assertThat(snapshot.getTotalNumberOfCalls()).isEqualTo(1);
        assertThat(snapshot.getNumberOfSuccessfulCalls()).isEqualTo(1);
        assertThat(snapshot.getNumberOfFailedCalls()).isEqualTo(0);
        assertThat(snapshot.getTotalNumberOfSlowCalls()).isEqualTo(1);
        assertThat(snapshot.getNumberOfSlowSuccessfulCalls()).isEqualTo(1);
        assertThat(snapshot.getNumberOfSlowFailedCalls()).isEqualTo(0);
        assertThat(snapshot.getTotalDuration().toMillis()).isEqualTo(100);
        assertThat(snapshot.getAverageDuration().toMillis()).isEqualTo(100);
        assertThat(snapshot.getFailureRate()).isEqualTo(0);
    }

    @Test
    public void testSlowCallsPercentage() {
        Metrics metrics = new SecondsSlidingTimeWindowMetrics(5);

        metrics.record(10000, TimeUnit.MILLISECONDS, Metrics.Outcome.SLOW_SUCCESS);
        metrics.record(10000, TimeUnit.MILLISECONDS, Metrics.Outcome.SLOW_ERROR);
        metrics.record(100, TimeUnit.MILLISECONDS, Metrics.Outcome.SUCCESS);
        metrics.record(100, TimeUnit.MILLISECONDS, Metrics.Outcome.SUCCESS);
        metrics.record(100, TimeUnit.MILLISECONDS, Metrics.Outcome.SUCCESS);

        Snapshot snapshot = metrics.getSnapshot();
        assertThat(snapshot.getTotalNumberOfCalls()).isEqualTo(5);
        assertThat(snapshot.getNumberOfSuccessfulCalls()).isEqualTo(4);
        assertThat(snapshot.getTotalNumberOfSlowCalls()).isEqualTo(2);
        assertThat(snapshot.getNumberOfSlowSuccessfulCalls()).isEqualTo(1);
        assertThat(snapshot.getNumberOfSlowFailedCalls()).isEqualTo(1);
        assertThat(snapshot.getTotalDuration().toMillis()).isEqualTo(20300);
        assertThat(snapshot.getAverageDuration().toMillis()).isEqualTo(4060);
        assertThat(snapshot.getSlowCallRate()).isEqualTo(40f);
    }

    @Test
    public void testMoveHeadIndexByOne() {
        SlidingTimeWindowMetrics metrics = new SecondsSlidingTimeWindowMetrics(3);

        assertThat(metrics.headIndex).isEqualTo(0);

        metrics.moveHeadIndexByOne();

        assertThat(metrics.headIndex).isEqualTo(1);

        metrics.moveHeadIndexByOne();

        assertThat(metrics.headIndex).isEqualTo(2);

        metrics.moveHeadIndexByOne();

        assertThat(metrics.headIndex).isEqualTo(0);

        metrics.moveHeadIndexByOne();

        assertThat(metrics.headIndex).isEqualTo(1);

    }

    @Test
    public void shouldClearSlidingTimeWindowMetrics() {
        Metrics metrics = new SecondsSlidingTimeWindowMetrics(5);

        metrics.record(100, TimeUnit.MILLISECONDS, Metrics.Outcome.ERROR);

        Snapshot snapshot = metrics.getSnapshot();
        assertThat(snapshot.getTotalNumberOfCalls()).isEqualTo(1);
        assertThat(snapshot.getNumberOfSuccessfulCalls()).isEqualTo(0);
        assertThat(snapshot.getNumberOfFailedCalls()).isEqualTo(1);
        assertThat(snapshot.getTotalDuration().toMillis()).isEqualTo(100);
        assertThat(snapshot.getAverageDuration().toMillis()).isEqualTo(100);
        assertThat(snapshot.getFailureRate()).isEqualTo(100);

        TimeUtil.silentlySleep(100);

        metrics.record(100, TimeUnit.MILLISECONDS, Metrics.Outcome.SUCCESS);
        snapshot = metrics.getSnapshot();
        assertThat(snapshot.getTotalNumberOfCalls()).isEqualTo(2);
        assertThat(snapshot.getNumberOfSuccessfulCalls()).isEqualTo(1);
        assertThat(snapshot.getNumberOfFailedCalls()).isEqualTo(1);
        assertThat(snapshot.getTotalDuration().toMillis()).isEqualTo(200);
        assertThat(snapshot.getAverageDuration().toMillis()).isEqualTo(100);
        assertThat(snapshot.getFailureRate()).isEqualTo(50);

        TimeUtil.silentlySleep(700);

        metrics.record(100, TimeUnit.MILLISECONDS, Metrics.Outcome.SUCCESS);
        snapshot = metrics.getSnapshot();
        assertThat(snapshot.getTotalNumberOfCalls()).isEqualTo(3);
        assertThat(snapshot.getNumberOfSuccessfulCalls()).isEqualTo(2);
        assertThat(snapshot.getNumberOfFailedCalls()).isEqualTo(1);
        assertThat(snapshot.getTotalDuration().toMillis()).isEqualTo(300);
        assertThat(snapshot.getAverageDuration().toMillis()).isEqualTo(100);

        TimeUtil.silentlySleep(4000);

        snapshot = metrics.getSnapshot();
        assertThat(snapshot.getTotalNumberOfCalls()).isEqualTo(3);
        assertThat(snapshot.getNumberOfSuccessfulCalls()).isEqualTo(2);
        assertThat(snapshot.getNumberOfFailedCalls()).isEqualTo(1);
        assertThat(snapshot.getTotalDuration().toMillis()).isEqualTo(300);
        assertThat(snapshot.getAverageDuration().toMillis()).isEqualTo(100);

        TimeUtil.silentlySleep(1000);

        snapshot = metrics.getSnapshot();

        assertThat(snapshot.getTotalNumberOfCalls()).isEqualTo(0);
        assertThat(snapshot.getNumberOfSuccessfulCalls()).isEqualTo(0);
        assertThat(snapshot.getNumberOfFailedCalls()).isEqualTo(0);
        assertThat(snapshot.getTotalDuration().toMillis()).isEqualTo(0);
        assertThat(snapshot.getAverageDuration().toMillis()).isEqualTo(0);
        assertThat(snapshot.getFailureRate()).isEqualTo(0);

        TimeUtil.silentlySleep(100);

        metrics.record(100, TimeUnit.MILLISECONDS, Metrics.Outcome.SUCCESS);
        snapshot = metrics.getSnapshot();
        assertThat(snapshot.getTotalNumberOfCalls()).isEqualTo(1);
        assertThat(snapshot.getNumberOfSuccessfulCalls()).isEqualTo(1);
        assertThat(snapshot.getNumberOfFailedCalls()).isEqualTo(0);
        assertThat(snapshot.getTotalDuration().toMillis()).isEqualTo(100);
        assertThat(snapshot.getAverageDuration().toMillis()).isEqualTo(100);
        assertThat(snapshot.getFailureRate()).isEqualTo(0);
    }

    @Test
    public void testSlidingTimeWindowMetrics() {
        Metrics metrics = new SecondsSlidingTimeWindowMetrics(5);

        metrics.record(100, TimeUnit.MILLISECONDS, Metrics.Outcome.ERROR);
        Snapshot result = metrics.getSnapshot();
        assertThat(result.getTotalNumberOfCalls()).isEqualTo(1);
        assertThat(result.getNumberOfSuccessfulCalls()).isEqualTo(0);
        assertThat(result.getNumberOfFailedCalls()).isEqualTo(1);
        assertThat(result.getTotalDuration().toMillis()).isEqualTo(100);

        TimeUtil.silentlySleep(100);

        metrics.record(100, TimeUnit.MILLISECONDS, Metrics.Outcome.SUCCESS);
        result = metrics.getSnapshot();
        assertThat(result.getTotalNumberOfCalls()).isEqualTo(2);
        assertThat(result.getNumberOfSuccessfulCalls()).isEqualTo(1);
        assertThat(result.getNumberOfFailedCalls()).isEqualTo(1);
        assertThat(result.getTotalDuration().toMillis()).isEqualTo(200);

        TimeUtil.silentlySleep(100);

        metrics.record(100, TimeUnit.MILLISECONDS, Metrics.Outcome.SUCCESS);
        result = metrics.getSnapshot();
        assertThat(result.getTotalNumberOfCalls()).isEqualTo(3);
        assertThat(result.getNumberOfSuccessfulCalls()).isEqualTo(2);
        assertThat(result.getNumberOfFailedCalls()).isEqualTo(1);
        assertThat(result.getTotalDuration().toMillis()).isEqualTo(300);

        TimeUtil.silentlySleep(1000);

        metrics.record(100, TimeUnit.MILLISECONDS, Metrics.Outcome.SUCCESS);
        result = metrics.getSnapshot();
        assertThat(result.getTotalNumberOfCalls()).isEqualTo(4);
        assertThat(result.getNumberOfSuccessfulCalls()).isEqualTo(3);
        assertThat(result.getNumberOfFailedCalls()).isEqualTo(1);
        assertThat(result.getTotalDuration().toMillis()).isEqualTo(400);

        TimeUtil.silentlySleep(1000);

        metrics.record(100, TimeUnit.MILLISECONDS, Metrics.Outcome.SUCCESS);
        result = metrics.getSnapshot();
        assertThat(result.getTotalNumberOfCalls()).isEqualTo(5);
        assertThat(result.getNumberOfSuccessfulCalls()).isEqualTo(4);
        assertThat(result.getNumberOfFailedCalls()).isEqualTo(1);
        assertThat(result.getTotalDuration().toMillis()).isEqualTo(500);

        TimeUtil.silentlySleep(1000);

        metrics.record(100, TimeUnit.MILLISECONDS, Metrics.Outcome.ERROR);
        result = metrics.getSnapshot();
        assertThat(result.getTotalNumberOfCalls()).isEqualTo(6);
        assertThat(result.getNumberOfSuccessfulCalls()).isEqualTo(4);
        assertThat(result.getNumberOfFailedCalls()).isEqualTo(2);
        assertThat(result.getTotalDuration().toMillis()).isEqualTo(600);

        TimeUtil.silentlySleep(1000);

        metrics.record(100, TimeUnit.MILLISECONDS, Metrics.Outcome.SUCCESS);
        result = metrics.getSnapshot();
        assertThat(result.getTotalNumberOfCalls()).isEqualTo(7);
        assertThat(result.getNumberOfSuccessfulCalls()).isEqualTo(5);
        assertThat(result.getNumberOfFailedCalls()).isEqualTo(2);
        assertThat(result.getTotalDuration().toMillis()).isEqualTo(700);

        TimeUtil.silentlySleep(1000);

        metrics.record(100, TimeUnit.MILLISECONDS, Metrics.Outcome.SUCCESS);
        result = metrics.getSnapshot();
        assertThat(result.getTotalNumberOfCalls()).isEqualTo(5);
        assertThat(result.getNumberOfSuccessfulCalls()).isEqualTo(4);
        assertThat(result.getNumberOfFailedCalls()).isEqualTo(1);
        assertThat(result.getTotalDuration().toMillis()).isEqualTo(500);

        TimeUtil.silentlySleep(1000);

        metrics.record(100, TimeUnit.MILLISECONDS, Metrics.Outcome.SUCCESS);
        result = metrics.getSnapshot();
        assertThat(result.getTotalNumberOfCalls()).isEqualTo(5);
        assertThat(result.getNumberOfSuccessfulCalls()).isEqualTo(4);
        assertThat(result.getNumberOfFailedCalls()).isEqualTo(1);
        assertThat(result.getTotalDuration().toMillis()).isEqualTo(500);

        TimeUtil.silentlySleep(5000);

        metrics.record(100, TimeUnit.MILLISECONDS, Metrics.Outcome.SUCCESS);
        result = metrics.getSnapshot();
        assertThat(result.getTotalNumberOfCalls()).isEqualTo(1);
        assertThat(result.getNumberOfSuccessfulCalls()).isEqualTo(1);
        assertThat(result.getNumberOfFailedCalls()).isEqualTo(0);
        assertThat(result.getTotalDuration().toMillis()).isEqualTo(100);
    }
}
