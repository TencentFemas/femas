package com.tencent.tsf.femas.governance.ratelimit.impl;

import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.governance.plugin.context.ConfigContext;
import com.tencent.tsf.femas.governance.ratelimit.RateLimiter;
import com.tencent.tsf.femas.governance.ratelimit.entity.RateLimiterRule;

public class TokenBucketRateLimiter implements RateLimiter<RateLimiterRule> {

    double permitsPerSecond;

    public TokenBucketRateLimiter(double permitsPerSecond) {
        this.permitsPerSecond = permitsPerSecond;
    }

    @Override
    public boolean acquire() {
        return false;
    }

    @Override
    public boolean acquire(int permits) {
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
