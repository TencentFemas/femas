/*
 *
 *  Copyright 2016 Robert Winkler
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

import static com.tencent.tsf.femas.governance.circuitbreaker.core.internal.CircuitBreakerMetrics.Result;
import static org.assertj.core.api.Assertions.assertThat;

import com.tencent.tsf.femas.governance.circuitbreaker.core.CircuitBreakerConfig;
import java.time.Clock;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class CircuitBreakerMetricsTest {

    @Test
    public void testCircuitBreakerMetrics() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .slidingWindow(10, 10, CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .build();

        CircuitBreakerMetrics circuitBreakerMetrics = CircuitBreakerMetrics
                .forClosed(circuitBreakerConfig, Clock.systemUTC());

        circuitBreakerMetrics.onSuccess(0, TimeUnit.NANOSECONDS);
        circuitBreakerMetrics.onSuccess(0, TimeUnit.NANOSECONDS);
        circuitBreakerMetrics.onError(0, TimeUnit.NANOSECONDS);
        Result result = circuitBreakerMetrics.onError(0, TimeUnit.NANOSECONDS);

        circuitBreakerMetrics.onCallNotPermitted();
        circuitBreakerMetrics.onCallNotPermitted();

        assertThat(circuitBreakerMetrics.getNumberOfBufferedCalls()).isEqualTo(4);
        assertThat(circuitBreakerMetrics.getNumberOfFailedCalls()).isEqualTo(2);
        assertThat(circuitBreakerMetrics.getNumberOfSuccessfulCalls()).isEqualTo(2);
        assertThat(circuitBreakerMetrics.getNumberOfNotPermittedCalls()).isEqualTo(2);

        // The failure rate must be -1, because the number of measured calls is below the buffer size of 10
        assertThat(circuitBreakerMetrics.getFailureRate()).isEqualTo(-1);
        assertThat(result)
                .isEqualTo(CircuitBreakerMetrics.Result.BELOW_MINIMUM_CALLS_THRESHOLD);

        circuitBreakerMetrics.onError(0, TimeUnit.NANOSECONDS);
        circuitBreakerMetrics.onError(0, TimeUnit.NANOSECONDS);
        circuitBreakerMetrics.onError(0, TimeUnit.NANOSECONDS);
        circuitBreakerMetrics.onError(0, TimeUnit.NANOSECONDS);
        circuitBreakerMetrics.onSuccess(0, TimeUnit.NANOSECONDS);
        circuitBreakerMetrics.onSuccess(0, TimeUnit.NANOSECONDS);
        circuitBreakerMetrics.onSuccess(0, TimeUnit.NANOSECONDS);
        result = circuitBreakerMetrics.onSuccess(0, TimeUnit.NANOSECONDS);

        // 12 calls have been recorded, but only 10 are stored in the sliding window. 4 successes and 6 failures.
        // The failure rate must be 60%, because the number of measured calls is above the minimum number of measured calls.
        assertThat(circuitBreakerMetrics.getNumberOfBufferedCalls()).isEqualTo(10);
        assertThat(circuitBreakerMetrics.getNumberOfFailedCalls()).isEqualTo(6);
        assertThat(circuitBreakerMetrics.getNumberOfSuccessfulCalls()).isEqualTo(4);
        assertThat(circuitBreakerMetrics.getFailureRate()).isEqualTo(60);
        assertThat(Result.hasExceededThresholds(result)).isEqualTo(true);

        circuitBreakerMetrics.onSuccess(0, TimeUnit.NANOSECONDS);
        circuitBreakerMetrics.onSuccess(0, TimeUnit.NANOSECONDS);
        result = circuitBreakerMetrics.onSuccess(0, TimeUnit.NANOSECONDS);
        assertThat(result).isEqualTo(CircuitBreakerMetrics.Result.BELOW_THRESHOLDS);
        assertThat(circuitBreakerMetrics.getFailureRate()).isEqualTo(30);


    }
}
