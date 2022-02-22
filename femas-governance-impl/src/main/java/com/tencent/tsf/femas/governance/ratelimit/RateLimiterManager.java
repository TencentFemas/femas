package com.tencent.tsf.femas.governance.ratelimit;

import com.tencent.tsf.femas.common.entity.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO 后续所有治理相关模块的使用可能会从静态方法变成类方法，以实现用户自己在需要的地方进行治理的能力
 */
public class RateLimiterManager {

    public static Map<Service, RateLimiter> RATE_LIMITERS = new ConcurrentHashMap<>();

    public static boolean acquire(Service service) {
        if (service == null) {
            return true;
        }

        RateLimiter rateLimiter = RATE_LIMITERS.get(service);
        // RATE_LIMITER 不为空，且Tag规则命中，且限流不通过
        if (rateLimiter != null && !rateLimiter.acquire()) {
            return false;
        }

        return true;
    }

    public static void refreshRateLimiter(Service service, RateLimiter rateLimiter) {
        RATE_LIMITERS.put(service, rateLimiter);
    }

    public static RateLimiter getRateLimiter(Service service) {
        return RATE_LIMITERS.get(service);
    }
}
