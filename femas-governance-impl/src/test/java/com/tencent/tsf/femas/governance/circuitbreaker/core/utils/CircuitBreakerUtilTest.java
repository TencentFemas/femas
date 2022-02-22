package com.tencent.tsf.femas.governance.circuitbreaker.core.utils;


import static com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService.State.CLOSED;
import static com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService.State.DISABLED;
import static com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService.State.FORCED_OPEN;
import static com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService.State.HALF_OPEN;
import static com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService.State.METRICS_ONLY;
import static com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService.State.OPEN;
import static com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService.State.UNREGISTERED;
import static org.assertj.core.api.Assertions.assertThat;

import com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService;
import org.junit.Test;

public class CircuitBreakerUtilTest {

    @Test
    public void shouldConsiderAllKnownStatusesUsingIsCallPermitted() {
        assertThat(ICircuitBreakerService.State.values())
                .describedAs("List of statuses changed." +
                        "Please consider updating CircuitBreakerUtil#isCallPermitted to handle" +
                        "new status properly.")
                .containsOnly(DISABLED, CLOSED, OPEN, FORCED_OPEN, HALF_OPEN, METRICS_ONLY, UNREGISTERED);
    }
}
