package com.tencent.tsf.femas.governance.circuitbreaker.core;

import com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService;
import com.tencent.tsf.femas.governance.circuitbreaker.core.internal.CircuitBreakerMetrics;

public abstract class StateTransitionCallback {

    public abstract void onTransition(ICircuitBreakerService.State from, ICircuitBreakerService.State to,
            Object circuitBreakerObject, CircuitBreakerMetrics metrics,
            CircuitBreaker circuitBreaker);
}
