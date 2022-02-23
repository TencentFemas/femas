package com.tencent.tsf.femas.governance.circuitbreaker.core.utils;

import static com.tencent.tsf.femas.governance.circuitbreaker.core.utils.CircuitBreakerUtil.isCallPermitted;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService;
import com.tencent.tsf.femas.governance.circuitbreaker.core.CircuitBreaker;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CircuitBreakerUtilIsCallPermittedTest {

    private static final boolean CALL_NOT_PERMITTED = false;
    private static final boolean CALL_PERMITTED = true;

    @Parameter
    public ICircuitBreakerService.State state;

    @Parameter(1)
    public boolean expectedPermission;

    @Parameters(name = "isCallPermitted should be {1} for circuit breaker state {0}")
    public static Collection<Object[]> cases() {
        return asList(new Object[][]{
                {ICircuitBreakerService.State.DISABLED, CALL_PERMITTED},
                {ICircuitBreakerService.State.CLOSED, CALL_PERMITTED},
                {ICircuitBreakerService.State.OPEN, CALL_NOT_PERMITTED},
                {ICircuitBreakerService.State.FORCED_OPEN, CALL_NOT_PERMITTED},
                {ICircuitBreakerService.State.HALF_OPEN, CALL_PERMITTED},
        });
    }

    private static CircuitBreaker givenCircuitBreakerAtState(ICircuitBreakerService.State state) {
        CircuitBreaker circuitBreaker = mock(CircuitBreaker.class);
        when(circuitBreaker.getState()).thenReturn(state);
        return circuitBreaker;
    }

    @Test
    public void shouldIndicateCallPermittedForGivenStatus() {
        CircuitBreaker circuitBreaker = givenCircuitBreakerAtState(state);

        boolean isPermitted = isCallPermitted(circuitBreaker);

        assertThat(isPermitted).isEqualTo(expectedPermission);
    }
}
