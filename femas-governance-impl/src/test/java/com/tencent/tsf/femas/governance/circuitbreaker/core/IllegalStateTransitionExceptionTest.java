package com.tencent.tsf.femas.governance.circuitbreaker.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService;
import org.junit.Test;

public class IllegalStateTransitionExceptionTest {

    @Test
    public void shouldReturnCorrectMessage() {
        IllegalStateTransitionException illegalStateTransitionException = new IllegalStateTransitionException(
                "testName", ICircuitBreakerService.State.OPEN,
                ICircuitBreakerService.State.CLOSED);
        assertThat(illegalStateTransitionException.getMessage()).isEqualTo(
                "CircuitBreaker 'testName' tried an illegal state transition from OPEN to CLOSED");
        assertThat(illegalStateTransitionException.getFromState())
                .isEqualTo(ICircuitBreakerService.State.OPEN);
        assertThat(illegalStateTransitionException.getToState())
                .isEqualTo(ICircuitBreakerService.State.CLOSED);
    }
}
