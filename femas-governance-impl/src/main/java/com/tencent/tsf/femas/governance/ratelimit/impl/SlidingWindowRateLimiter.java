package com.tencent.tsf.femas.governance.ratelimit.impl;

import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.common.statistic.Metrics;
import com.tencent.tsf.femas.common.statistic.SlidingTimeWindowMetrics;
import com.tencent.tsf.femas.governance.ratelimit.RateLimiter;
import com.tencent.tsf.femas.plugin.context.ConfigContext;
import com.tencent.tsf.femas.plugin.impl.config.rule.ratelimit.RateLimiterRule;

public class SlidingWindowRateLimiter implements RateLimiter<RateLimiterRule> {

    SlidingTimeWindowMetrics slidingTimeWindowMetrics;
    int limit;

    /**
     * @param duration 单位ms，整个window的时间大小
     * @param limit
     */
    public SlidingWindowRateLimiter(int duration, int limit) {
        this.limit = limit;
        this.slidingTimeWindowMetrics = new SlidingTimeWindowMetrics(5, duration);
    }

    public SlidingWindowRateLimiter() {
        this(1000, 1000);
    }

    @Override
    public boolean acquire() {
        return acquire(1);
    }

    @Override
    public boolean acquire(int permits) {
        int curCount = slidingTimeWindowMetrics.getSnapshot().getNumberOfSuccessfulCalls();
        if (curCount + permits > limit) {
            slidingTimeWindowMetrics.record(Metrics.Outcome.BLOCK);
            return false;
        }

        slidingTimeWindowMetrics.record(Metrics.Outcome.SUCCESS);
        return true;
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
    public String getName() {
        return "slideWindow";
    }

    @Override
    public void destroy() {

    }

    @Override
    public void setInsId(String insId) {

    }
}
