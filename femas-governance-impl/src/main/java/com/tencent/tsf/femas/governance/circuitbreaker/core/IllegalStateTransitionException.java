package com.tencent.tsf.femas.governance.circuitbreaker.core;

import com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService;

/**
 * A {@link IllegalStateTransitionException} signals that someone tried to trigger an illegal state
 * transition..
 */
public class IllegalStateTransitionException extends RuntimeException {

    private final String name;
    private final ICircuitBreakerService.State fromState;
    private final ICircuitBreakerService.State toState;

    IllegalStateTransitionException(String name, ICircuitBreakerService.State fromState,
            ICircuitBreakerService.State toState) {
        super(String
                .format("CircuitBreaker '%s' tried an illegal state transition from %s to %s", name,
                        fromState.toString(), toState.toString()));
        this.name = name;
        this.fromState = fromState;
        this.toState = toState;
    }

    public ICircuitBreakerService.State getFromState() {
        return fromState;
    }

    public ICircuitBreakerService.State getToState() {
        return toState;
    }

    public String getName() {
        return name;
    }
}
