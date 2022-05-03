/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tencent.tsf.femas.governance.ratelimit.impl;

import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.common.statistic.Metrics;
import com.tencent.tsf.femas.common.statistic.SlidingTimeWindowMetrics;
import com.tencent.tsf.femas.common.util.TimeUtil;
import com.tencent.tsf.femas.governance.plugin.context.ConfigContext;
import com.tencent.tsf.femas.governance.ratelimit.RateLimiter;
import com.tencent.tsf.femas.governance.ratelimit.entity.RateLimiterRule;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 根据Sentinel的QPS warmup改造而来，后续如果是采用QPS的话，还是基于QPS更合理
 */
public class WarmUpSlidingWindowRateLimiter implements RateLimiter<RateLimiterRule> {

    protected int limit;
    protected int duration;
    protected int warningToken = 0;
    protected double slope;
    protected AtomicLong storedTokens = new AtomicLong(0);
    protected AtomicLong lastFilledTime = new AtomicLong(0);
    SlidingTimeWindowMetrics slidingTimeWindowMetrics;
    private int coldFactor;
    private int maxToken;

    public WarmUpSlidingWindowRateLimiter(int duration, int limit, int warmUpTimes, int coldFactor) {
        if (coldFactor <= 1) {
            throw new IllegalArgumentException("Cold factor should be larger than 1");
        }

        this.limit = limit;
        this.duration = duration;
        this.coldFactor = coldFactor;

        warningToken = (warmUpTimes * limit) / (coldFactor - 1);
        maxToken = warningToken + (int) (2 * warmUpTimes * limit / (1.0 + coldFactor));

        // slope
        slope = (coldFactor - 1.0) / limit / (maxToken - warningToken);

        this.slidingTimeWindowMetrics = new SlidingTimeWindowMetrics(5, duration);
    }

    @Override
    public boolean acquire() {
        return acquire(1);
    }

    @Override
    public boolean acquire(int permits) {
        int passQps = slidingTimeWindowMetrics.getSnapshot().getTotalNumberOfCalls();
        syncToken(passQps);

        // 开始计算它的斜率
        // 如果进入了警戒线，开始调整他的qps
        long restToken = storedTokens.get();
        if (restToken >= warningToken) {
            long aboveToken = restToken - warningToken;
            // 消耗的速度要比warning快，但是要比慢
            // current interval = restToken*slope+1/count
            double warningQps = Math.nextUp(1.0 / (aboveToken * slope + 1.0 / limit));
            if (passQps + permits <= warningQps) {
                slidingTimeWindowMetrics.record(Metrics.Outcome.SUCCESS);
                return true;
            }
        } else {
            if (passQps + permits <= limit) {
                slidingTimeWindowMetrics.record(Metrics.Outcome.SUCCESS);
                return true;
            }
        }

        slidingTimeWindowMetrics.record(Metrics.Outcome.BLOCK);
        return false;
    }

    protected void syncToken(long passQps) {
        long currentTime = TimeUtil.currentTimeMillis();
        long oldLastFillTime = lastFilledTime.get();
        if (currentTime <= oldLastFillTime) {
            return;
        }

        long oldValue = storedTokens.get();
        long newValue = coolDownTokens(currentTime, passQps);

        if (storedTokens.compareAndSet(oldValue, newValue)) {
            long currentValue = storedTokens.addAndGet(0 - passQps);
            if (currentValue < 0) {
                storedTokens.set(0L);
            }
            lastFilledTime.set(currentTime);
        }

    }

    private long coolDownTokens(long currentTime, long passQps) {
        long oldValue = storedTokens.get();
        long newValue = oldValue;

        // 添加令牌的判断前提条件:
        // 当令牌的消耗程度远远低于警戒线的时候
        if (oldValue < warningToken) {
            newValue = oldValue + (currentTime - lastFilledTime.get()) * limit / duration;
        } else if (oldValue > warningToken) {
            if (passQps < limit / coldFactor) {
                newValue = oldValue + (currentTime - lastFilledTime.get()) * limit / duration;
            }
        }
        return Math.min(newValue, maxToken);
    }

    @Override
    public void update(RateLimiterRule rule) {

    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public void init(ConfigContext conf) throws FemasRuntimeException {

    }

    @Override
    public void buildCollector(Service service) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void setInsId(String insId) {

    }
}
