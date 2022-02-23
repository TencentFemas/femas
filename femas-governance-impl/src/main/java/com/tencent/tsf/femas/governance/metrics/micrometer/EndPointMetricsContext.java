/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.tsf.femas.governance.metrics.micrometer;

import com.tencent.tsf.femas.common.monitor.Endpoint;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Timer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author leoziltong
 * @Date: 2021/7/28 14:58
 */
public class EndPointMetricsContext {

    public static final Map<Endpoint, EndPointMetrics> endPointMetricsContextCache = new ConcurrentHashMap<>();

    public static void register(EndPointMetrics metrics) {
        endPointMetricsContextCache.put(metrics.getEndpoint(), metrics);
    }

    public static void remove(Endpoint endpoint) {
        endPointMetricsContextCache.remove(endpoint);
    }

    public static Map<Endpoint, EndPointMetrics> getEndPointMetricsContextCache() {
        return endPointMetricsContextCache;
    }

    public static class EndPointMetrics {

        private Endpoint endpoint;

        private Timer timer;

        private Counter authBlockedCounter;
        private Counter rateLimitBlockedCounter;
        private Counter circuitBreakerBlockedCounter;

        private AtomicInteger authBlockedDurationCount;
        private AtomicInteger rateLimitBlockedDurationCount;
        private AtomicInteger circuitBreakerBlockedDurationCount;

        private AtomicInteger authBlockedGauge;
        private AtomicInteger rateLimitBlockedGauge;
        private AtomicInteger circuitBreakerBlockedGauge;

        private DistributionSummary authBlockedSummary;
        private DistributionSummary rateLimitBlockedSummary;
        private DistributionSummary circuitBreakerBlockedSummary;

        public EndPointMetrics(Endpoint endpoint) {
            this.endpoint = endpoint;
        }

        public EndPointMetrics() {
        }

        public Endpoint getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(Endpoint endpoint) {
            this.endpoint = endpoint;
        }

        public Timer getTimer() {
            return timer;
        }

        public void setTimer(Timer timer) {
            this.timer = timer;
        }

        public Counter getAuthBlockedCounter() {
            return authBlockedCounter;
        }

        public void setAuthBlockedCounter(Counter authBlockedCounter) {
            this.authBlockedCounter = authBlockedCounter;
        }

        public Counter getRateLimitBlockedCounter() {
            return rateLimitBlockedCounter;
        }

        public void setRateLimitBlockedCounter(Counter rateLimitBlockedCounter) {
            this.rateLimitBlockedCounter = rateLimitBlockedCounter;
        }

        public Counter getCircuitBreakerBlockedCounter() {
            return circuitBreakerBlockedCounter;
        }

        public void setCircuitBreakerBlockedCounter(Counter circuitBreakerBlockedCounter) {
            this.circuitBreakerBlockedCounter = circuitBreakerBlockedCounter;
        }

        public AtomicInteger getAuthBlockedDurationCount() {
            return authBlockedDurationCount;
        }

        public void setAuthBlockedDurationCount(AtomicInteger authBlockedDurationCount) {
            this.authBlockedDurationCount = authBlockedDurationCount;
        }

        public AtomicInteger getRateLimitBlockedDurationCount() {
            return rateLimitBlockedDurationCount;
        }

        public void setRateLimitBlockedDurationCount(AtomicInteger rateLimitBlockedDurationCount) {
            this.rateLimitBlockedDurationCount = rateLimitBlockedDurationCount;
        }

        public AtomicInteger getCircuitBreakerBlockedDurationCount() {
            return circuitBreakerBlockedDurationCount;
        }

        public void setCircuitBreakerBlockedDurationCount(AtomicInteger circuitBreakerBlockedDurationCount) {
            this.circuitBreakerBlockedDurationCount = circuitBreakerBlockedDurationCount;
        }

        public AtomicInteger getAuthBlockedGauge() {
            return authBlockedGauge;
        }

        public void setAuthBlockedGauge(AtomicInteger authBlockedGauge) {
            this.authBlockedGauge = authBlockedGauge;
        }

        public AtomicInteger getRateLimitBlockedGauge() {
            return rateLimitBlockedGauge;
        }

        public void setRateLimitBlockedGauge(AtomicInteger rateLimitBlockedGauge) {
            this.rateLimitBlockedGauge = rateLimitBlockedGauge;
        }

        public AtomicInteger getCircuitBreakerBlockedGauge() {
            return circuitBreakerBlockedGauge;
        }

        public void setCircuitBreakerBlockedGauge(AtomicInteger circuitBreakerBlockedGauge) {
            this.circuitBreakerBlockedGauge = circuitBreakerBlockedGauge;
        }

        public DistributionSummary getAuthBlockedSummary() {
            return authBlockedSummary;
        }

        public void setAuthBlockedSummary(DistributionSummary authBlockedSummary) {
            this.authBlockedSummary = authBlockedSummary;
        }

        public DistributionSummary getRateLimitBlockedSummary() {
            return rateLimitBlockedSummary;
        }

        public void setRateLimitBlockedSummary(DistributionSummary rateLimitBlockedSummary) {
            this.rateLimitBlockedSummary = rateLimitBlockedSummary;
        }

        public DistributionSummary getCircuitBreakerBlockedSummary() {
            return circuitBreakerBlockedSummary;
        }

        public void setCircuitBreakerBlockedSummary(DistributionSummary circuitBreakerBlockedSummary) {
            this.circuitBreakerBlockedSummary = circuitBreakerBlockedSummary;
        }
    }
}
