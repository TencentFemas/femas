package com.tencent.tsf.femas.governance.circuitbreaker.core.utils;

import static com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService.State.CLOSED;
import static com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService.State.DISABLED;
import static com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService.State.HALF_OPEN;
import static com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService.State.METRICS_ONLY;

import com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService;
import com.tencent.tsf.femas.governance.circuitbreaker.core.CircuitBreaker;
import org.apache.commons.lang3.StringUtils;


public final class CircuitBreakerUtil {

    public static final String CIRCUIT_BREAKER_SEPARATOR = "_";

    public static String append(String... keys) {
        return StringUtils.join(keys, CIRCUIT_BREAKER_SEPARATOR);
    }

    /**
     * Indicates whether Circuit Breaker allows any calls or not.
     *
     * @param circuitBreaker to test
     * @return call is permitted
     */
    public static boolean isCallPermitted(CircuitBreaker circuitBreaker) {
        ICircuitBreakerService.State state = circuitBreaker.getState();
        return state == CLOSED || state == HALF_OPEN || state == DISABLED || state == METRICS_ONLY;
    }
}
