package com.tencent.tsf.femas.governance.ratelimit.impl;

import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.common.util.TimeUtil;
import com.tencent.tsf.femas.plugin.context.ConfigContext;
import com.tencent.tsf.femas.governance.ratelimit.RateLimiter;
import com.tencent.tsf.femas.plugin.impl.config.rule.ratelimit.RateLimiterRule;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 漏桶算法限流器
 *
 * 以恒定的速度放行请求
 * 适合需要削峰的应用，或者处理请求时间一定的情况
 */
public class LeakBucketRateLimiter implements RateLimiter<RateLimiterRule> {

    private final long interval;
    private final AtomicLong latestPassedTime = new AtomicLong(-1);

    public LeakBucketRateLimiter(int duration, int limit) {
        this.interval = duration / limit;
        this.latestPassedTime.set(TimeUtil.currentTimeMillis());
    }

    @Override
    public boolean acquire() {
        return acquire(1);
    }

    @Override
    public boolean acquire(int permits) {
        // Pass when acquire count is less or equal than 0.
        if (permits <= 0) {
            return true;
        }

        long currentTime = TimeUtil.currentTimeMillis();
        // Calculate the interval between every two requests.
        long costTime = Math.round(permits * interval);

        // Expected pass time of this request.
        long expectedTime = costTime + latestPassedTime.get();

        if (expectedTime <= currentTime) {
            // Contention may exist here, but it's okay.
            latestPassedTime.set(currentTime);
            return true;
        }

        return false;
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
