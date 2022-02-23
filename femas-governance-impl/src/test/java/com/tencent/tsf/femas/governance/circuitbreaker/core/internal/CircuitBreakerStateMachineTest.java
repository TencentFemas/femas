/*
 *
 *  Copyright 2016 Robert Winkler
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package com.tencent.tsf.femas.governance.circuitbreaker.core.internal;

import static com.tencent.tsf.femas.governance.circuitbreaker.core.CircuitBreakerConfig.SlidingWindowType;
import static com.tencent.tsf.femas.governance.circuitbreaker.core.CircuitBreakerConfig.custom;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService;
import com.tencent.tsf.femas.governance.circuitbreaker.core.CallNotPermittedException;
import com.tencent.tsf.femas.governance.circuitbreaker.core.CircuitBreaker;
import com.tencent.tsf.femas.governance.circuitbreaker.core.IllegalStateTransitionException;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

public class CircuitBreakerStateMachineTest {

    private CircuitBreaker circuitBreaker;
    private Clock mockClock;
//    private EventConsumer<CircuitBreakerOnSuccessEvent> mockOnSuccessEventConsumer;
//    private EventConsumer<CircuitBreakerOnErrorEvent> mockOnErrorEventConsumer;
//    private EventConsumer<CircuitBreakerOnStateTransitionEvent> mockOnStateTransitionEventConsumer;
//    private EventConsumer<CircuitBreakerOnFailureRateExceededEvent> mockOnFailureRateExceededEventConsumer;
//    private EventConsumer<CircuitBreakerOnSlowCallRateExceededEvent> mockOnSlowCallRateExceededEventConsumer;

    @Before
    public void setUp() {
//        mockOnSuccessEventConsumer = (EventConsumer<CircuitBreakerOnSuccessEvent>) mock(EventConsumer.class);
//        mockOnErrorEventConsumer = (EventConsumer<CircuitBreakerOnErrorEvent>) mock(EventConsumer.class);
//        mockOnStateTransitionEventConsumer = (EventConsumer<CircuitBreakerOnStateTransitionEvent>) mock(EventConsumer.class);
//        mockOnFailureRateExceededEventConsumer = (EventConsumer<CircuitBreakerOnFailureRateExceededEvent>) mock(EventConsumer.class);
//        mockOnSlowCallRateExceededEventConsumer = (EventConsumer<CircuitBreakerOnSlowCallRateExceededEvent>) mock(EventConsumer.class);
        mockClock = Clock.systemUTC();
        circuitBreaker = new CircuitBreakerStateMachine("testName", custom()
                .failureRateThreshold(50)
                .permittedNumberOfCallsInHalfOpenState(4)
                .slowCallDurationThreshold(Duration.ofSeconds(4))
                .slowCallRateThreshold(50)
                .slidingWindow(5, 5, SlidingWindowType.TIME_BASED)
                .waitDurationInOpenState(Duration.ofSeconds(5))
                .ignoreExceptions(NumberFormatException.class)
                .build(), mockClock);
    }

    @Test
    public void shouldReturnTheCorrectName() {
        Assertions.assertThat(circuitBreaker.getName()).isEqualTo("testName");
    }

    @Test()
    public void shouldThrowCallNotPermittedExceptionWhenStateIsOpen() {
        circuitBreaker.transitionToOpenState();
        assertThatThrownBy(circuitBreaker::acquirePermission)
                .isInstanceOf(CallNotPermittedException.class);
        Assertions.assertThat(circuitBreaker.getMetrics().getNumberOfNotPermittedCalls()).isEqualTo(1);
    }

    @Test()
    public void shouldThrowCallNotPermittedExceptionWhenStateIsForcedOpen() {
        circuitBreaker.transitionToForcedOpenState();
        assertThatThrownBy(circuitBreaker::acquirePermission)
                .isInstanceOf(CallNotPermittedException.class);
        Assertions.assertThat(circuitBreaker.getMetrics().getNumberOfNotPermittedCalls()).isEqualTo(1);
    }

    @Test()
    public void shouldIncreaseCounterOnReleasePermission() {
        circuitBreaker.transitionToOpenState();
        circuitBreaker.transitionToHalfOpenState();
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(false);
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(false);

        circuitBreaker.releasePermission();
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
    }

    @Test
    public void shouldThrowCallNotPermittedExceptionWhenNotFurtherTestCallsArePermitted() {
        circuitBreaker.transitionToOpenState();
        circuitBreaker.transitionToHalfOpenState();
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        assertThatThrownBy(circuitBreaker::acquirePermission)
                .isInstanceOf(CallNotPermittedException.class);
        Assertions.assertThat(circuitBreaker.getMetrics().getNumberOfNotPermittedCalls()).isEqualTo(1);
    }

    @Test
    public void shouldOnlyPermitFourCallsInHalfOpenState() {
        assertThatMetricsAreReset();
        circuitBreaker.transitionToOpenState();
        circuitBreaker.transitionToHalfOpenState();
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.HALF_OPEN);
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(false);
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(false);
        circuitBreaker.transitionToOpenState();
        circuitBreaker.transitionToHalfOpenState();
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(false);
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(false);
    }

    @Test
    public void shouldOpenAfterFailureRateThresholdExceeded() {
        // A ring buffer with size 5 is used in closed state
        // Initially the CircuitBreaker is closed
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.CLOSED);
        assertThatMetricsAreReset();
//        circuitBreaker.getEventPublisher().onFailureRateExceeded(mockOnFailureRateExceededEventConsumer);

        // Call 1 is a failure
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker.onError(0, TimeUnit.NANOSECONDS,
                new RuntimeException()); // Should create a CircuitBreakerOnErrorEvent
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.CLOSED);
        assertCircuitBreakerMetricsEqualTo(-1f, 0, 1, 1, 0L);

        // Call 2 is a failure
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker.onError(0, TimeUnit.NANOSECONDS,
                new RuntimeException()); // Should create a CircuitBreakerOnErrorEvent
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.CLOSED);
        assertCircuitBreakerMetricsEqualTo(-1f, 0, 2, 2, 0L);

        // Call 3 is a failure
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker.onError(0, TimeUnit.NANOSECONDS,
                new RuntimeException()); // Should create a CircuitBreakerOnErrorEvent
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.CLOSED);
        assertCircuitBreakerMetricsEqualTo(-1f, 0, 3, 3, 0L);

        // Call 4 is a success
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker
                .onSuccess(0, TimeUnit.NANOSECONDS); // Should create a CircuitBreakerOnSuccessEvent
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.CLOSED);
        assertCircuitBreakerMetricsEqualTo(-1f, 1, 4, 3, 0L);

        // Call 5 is a success
        circuitBreaker
                .onSuccess(0, TimeUnit.NANOSECONDS); // Should create a CircuitBreakerOnSuccessEvent

        // The ring buffer is filled and the failure rate is above 50%
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(
                ICircuitBreakerService.State.OPEN); // Should create a CircuitBreakerOnStateTransitionEvent (6)
        Assertions.assertThat(circuitBreaker.getMetrics().getNumberOfBufferedCalls()).isEqualTo(5);
        Assertions.assertThat(circuitBreaker.getMetrics().getNumberOfFailedCalls()).isEqualTo(3);
        Assertions.assertThat(circuitBreaker.getMetrics().getFailureRate()).isEqualTo(60.0f);
        assertCircuitBreakerMetricsEqualTo(60.0f, 2, 5, 3, 0L);
//        verify(mockOnFailureRateExceededEventConsumer, times(1)).consumeEvent(any(CircuitBreakerOnFailureRateExceededEvent.class));

        // Call to tryAcquirePermission records a notPermittedCall
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(false);
        assertCircuitBreakerMetricsEqualTo(60.0f, 2, 5, 3, 1L);
    }

    @Test
    public void shouldOpenAfterSlowCallRateThresholdExceeded() {
        // A ring buffer with size 5 is used in closed state
        // Initially the CircuitBreaker is closed
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.CLOSED);
        assertThatMetricsAreReset();

        // Call 1 is slow
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker.onError(5, TimeUnit.SECONDS,
                new RuntimeException()); // Should create a CircuitBreakerOnErrorEvent
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.CLOSED);

        // Call 2 is slow
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker.onSuccess(5, TimeUnit.SECONDS); // Should create a CircuitBreakerOnErrorEvent
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.CLOSED);

        // Call 3 is fast
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker.onError(100, TimeUnit.MILLISECONDS,
                new RuntimeException()); // Should create a CircuitBreakerOnErrorEvent
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.CLOSED);

        // Call 4 is fast
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker
                .onSuccess(100, TimeUnit.MILLISECONDS); // Should create a CircuitBreakerOnSuccessEvent
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.CLOSED);

        // Call 5 is slow
        circuitBreaker
                .onSuccess(5, TimeUnit.SECONDS); // Should create a CircuitBreakerOnSuccessEvent

        // The ring buffer is filled and the slow call rate is above 50%
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(
                ICircuitBreakerService.State.OPEN); // Should create a CircuitBreakerOnStateTransitionEvent (6)
        Assertions.assertThat(circuitBreaker.getMetrics().getNumberOfBufferedCalls()).isEqualTo(5);
        Assertions.assertThat(circuitBreaker.getMetrics().getNumberOfSlowCalls()).isEqualTo(3);
        Assertions.assertThat(circuitBreaker.getMetrics().getSlowCallRate()).isEqualTo(60.0f);

    }

    @Test
    public void shouldTransitionBackToOpenStateWhenFailureRateIsAboveThreshold() {
        // Initially the CircuitBreaker is half_open
        circuitBreaker.transitionToOpenState();
        circuitBreaker.transitionToHalfOpenState();
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.HALF_OPEN);
        assertCircuitBreakerMetricsEqualTo(-1f, 0, 0, 0, 0L);

        // A ring buffer with size 3 is used in half open state
        // Call 1 is a failure
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker.onError(0, TimeUnit.NANOSECONDS,
                new RuntimeException()); // Should create a CircuitBreakerOnErrorEvent

        // Call 2 is a failure
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker.onError(0, TimeUnit.NANOSECONDS,
                new RuntimeException()); // Should create a CircuitBreakerOnErrorEvent
        // Call 3 is a success
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker.onSuccess(0,
                TimeUnit.NANOSECONDS); // Should create a CircuitBreakerOnSuccessEvent (12)
        // Call 2 is a failure
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker.onError(0, TimeUnit.NANOSECONDS,
                new RuntimeException()); // Should create a CircuitBreakerOnErrorEvent

        // The ring buffer is filled and the failure rate is above 50%
        // The state machine transitions back to OPEN state
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(
                ICircuitBreakerService.State.OPEN); // Should create a CircuitBreakerOnStateTransitionEvent (13)
        assertCircuitBreakerMetricsEqualTo(75f, 1, 4, 3, 0L);
    }

    @Test
    public void shouldTransitionBackToOpenStateWhenSlowCallRateIsAboveThreshold() {
        // Initially the CircuitBreaker is half_open
        circuitBreaker.transitionToOpenState();
        circuitBreaker.transitionToHalfOpenState();
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.HALF_OPEN);

        // Call 1 is slow
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker.onSuccess(5, TimeUnit.SECONDS);

        // Call 2 is slow
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker.onSuccess(5, TimeUnit.SECONDS);

        // Call 3 is slow
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker.onSuccess(5, TimeUnit.SECONDS);

        // Call 4 is fast
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker.onSuccess(1, TimeUnit.SECONDS);

        // The failure rate is blow 50%, but slow call rate is above 50%
        // The state machine transitions back to OPEN state
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.OPEN);
        Assertions.assertThat(circuitBreaker.getMetrics().getNumberOfSlowCalls()).isEqualTo(3);
        Assertions.assertThat(circuitBreaker.getMetrics().getSlowCallRate()).isEqualTo(75.0f);
        Assertions.assertThat(circuitBreaker.getMetrics().getFailureRate()).isEqualTo(0f);
    }


    @Test
    public void shouldTransitionBackToClosedStateWhenFailureIsBelowThreshold() {
        // Initially the CircuitBreaker is half_open
        circuitBreaker.transitionToOpenState();
        circuitBreaker.transitionToHalfOpenState();
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.HALF_OPEN);
        assertCircuitBreakerMetricsEqualTo(-1f, 0, 0, 0, 0L);

        // Call 1 is a failure
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker.onError(0, TimeUnit.NANOSECONDS,
                new RuntimeException()); // Should create a CircuitBreakerOnErrorEvent

        // Call 2 is a success
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker
                .onSuccess(0, TimeUnit.NANOSECONDS); // Should create a CircuitBreakerOnSuccessEvent

        // Call 3 is a success
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker
                .onSuccess(0, TimeUnit.NANOSECONDS); // Should create a CircuitBreakerOnSuccessEvent

        // Call 4 is a success
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker
                .onSuccess(0, TimeUnit.NANOSECONDS); // Should create a CircuitBreakerOnSuccessEvent

        // The ring buffer is filled and the failure rate is below 50%
        // The state machine transitions back to CLOSED state
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(
                ICircuitBreakerService.State.CLOSED); // Should create a CircuitBreakerOnStateTransitionEvent
        assertCircuitBreakerMetricsEqualTo(-1f, 0, 0, 0, 0L);

        // // Call 5 is a success and fills the buffer in closed state
        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker
                .onSuccess(0, TimeUnit.NANOSECONDS); // Should create a CircuitBreakerOnSuccessEvent
        assertCircuitBreakerMetricsEqualTo(-1f, 1, 1, 0, 0L);

    }

    @Test
    public void shouldResetMetrics() {
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(
                ICircuitBreakerService.State.CLOSED); // Should create a CircuitBreakerOnStateTransitionEvent (21)
        assertThatMetricsAreReset();

        circuitBreaker.onSuccess(0, TimeUnit.NANOSECONDS);
        circuitBreaker.onError(0, TimeUnit.NANOSECONDS, new RuntimeException());

        assertCircuitBreakerMetricsEqualTo(-1f, 1, 2, 1, 0L);

        circuitBreaker.reset(); // Should create a CircuitBreakerOnResetEvent (20)
        assertThatMetricsAreReset();

    }

    @Test
    public void shouldDisableCircuitBreaker() {
        circuitBreaker
                .transitionToDisabledState(); // Should create a CircuitBreakerOnStateTransitionEvent

        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(
                ICircuitBreakerService.State.DISABLED); // Should create a CircuitBreakerOnStateTransitionEvent (21)
        assertThatMetricsAreReset();

        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker
                .onSuccess(0, TimeUnit.NANOSECONDS); // Should not create a CircuitBreakerOnSuccessEvent

        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker.onError(0, TimeUnit.NANOSECONDS,
                new RuntimeException()); // Should not create a CircuitBreakerOnErrorEvent

        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker.onError(0, TimeUnit.NANOSECONDS,
                new RuntimeException()); // Should not create a CircuitBreakerOnErrorEvent

        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker.onError(0, TimeUnit.NANOSECONDS,
                new RuntimeException()); // Should not create a CircuitBreakerOnErrorEvent

        circuitBreaker.acquirePermission();
        circuitBreaker.onError(0, TimeUnit.NANOSECONDS,
                new RuntimeException()); // Should not create a CircuitBreakerOnErrorEvent

        assertThatMetricsAreReset();
    }

    @Test
    public void shouldReleasePermissionWhenExceptionIgnored() {
        circuitBreaker.transitionToOpenState();
        circuitBreaker.transitionToHalfOpenState();
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.HALF_OPEN);

        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker.onSuccess(0, TimeUnit.NANOSECONDS);
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.HALF_OPEN);
        assertCircuitBreakerMetricsEqualTo(-1f, 1, 1, 0, 0L);

        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker.onSuccess(0, TimeUnit.NANOSECONDS);
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.HALF_OPEN);
        assertCircuitBreakerMetricsEqualTo(-1f, 2, 2, 0, 0L);

        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker.onSuccess(0, TimeUnit.NANOSECONDS); //
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.HALF_OPEN);
        assertCircuitBreakerMetricsEqualTo(-1f, 3, 3, 0, 0L);

        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        // Should ignore NumberFormatException and release permission
        circuitBreaker.onError(0, TimeUnit.NANOSECONDS, new NumberFormatException());
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.HALF_OPEN);
        assertCircuitBreakerMetricsEqualTo(-1f, 3, 3, 0, 0L);

        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker.onError(0, TimeUnit.NANOSECONDS, new RuntimeException());
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.CLOSED);
        assertCircuitBreakerMetricsEqualTo(-1f, 0, 0, 0, 0L);
    }

    @Test
    public void shouldIgnoreExceptionsAndThenTransitionToClosed() {
        circuitBreaker.transitionToOpenState();
        circuitBreaker.transitionToHalfOpenState();
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.HALF_OPEN);

        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        // Should ignore NumberFormatException and release permission
        circuitBreaker.onError(0, TimeUnit.NANOSECONDS, new NumberFormatException());
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.HALF_OPEN);

        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        // Should ignore NumberFormatException and release permission
        circuitBreaker.onError(0, TimeUnit.NANOSECONDS, new NumberFormatException());
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.HALF_OPEN);

        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        // Should ignore NumberFormatException and release permission
        circuitBreaker.onError(0, TimeUnit.NANOSECONDS, new NumberFormatException());
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.HALF_OPEN);

        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        // Should ignore NumberFormatException and release permission
        circuitBreaker.onError(0, TimeUnit.NANOSECONDS, new NumberFormatException());
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.HALF_OPEN);

        assertCircuitBreakerMetricsEqualTo(-1f, 0, 0, 0, 0L);

        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker.onSuccess(0, TimeUnit.NANOSECONDS); //
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.HALF_OPEN);

        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker.onSuccess(0, TimeUnit.NANOSECONDS); //
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.HALF_OPEN);

        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker.onSuccess(0, TimeUnit.NANOSECONDS); //
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.HALF_OPEN);

        assertCircuitBreakerMetricsEqualTo(-1f, 3, 3, 0, 0L);

        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
        circuitBreaker.onSuccess(0, TimeUnit.NANOSECONDS); //
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.CLOSED);

        assertCircuitBreakerMetricsEqualTo(-1f, 0, 0, 0, 0L);
    }


    @Test
    public void shouldNotAllowTransitionFromClosedToHalfOpen() {
        assertThatThrownBy(() -> circuitBreaker.transitionToHalfOpenState())
                .isInstanceOf(IllegalStateTransitionException.class)
                .hasMessage(
                        "CircuitBreaker 'testName' tried an illegal state transition from CLOSED to HALF_OPEN");
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.CLOSED);
    }

    @Test
    public void shouldNotAllowTransitionFromClosedToClosed() {
        assertThatThrownBy(() -> circuitBreaker.transitionToClosedState())
                .isInstanceOf(IllegalStateTransitionException.class)
                .hasMessage(
                        "CircuitBreaker 'testName' tried an illegal state transition from CLOSED to CLOSED");
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.CLOSED);
    }

    @Test
    public void shouldResetToClosedState() {
        circuitBreaker.transitionToOpenState();
        circuitBreaker.reset();
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.CLOSED);
    }

    @Test
    public void shouldResetClosedState() {
        circuitBreaker.onSuccess(0, TimeUnit.NANOSECONDS);
        circuitBreaker.onSuccess(0, TimeUnit.NANOSECONDS);
        Assertions.assertThat(circuitBreaker.getMetrics().getNumberOfSuccessfulCalls()).isEqualTo(2);

        circuitBreaker.reset();
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.CLOSED);
        Assertions.assertThat(circuitBreaker.getMetrics().getNumberOfSuccessfulCalls()).isEqualTo(0);
    }

    @Test
    public void shouldResetMetricsAfterMetricsOnlyStateTransition() {
        circuitBreaker.onSuccess(0, TimeUnit.NANOSECONDS);
        circuitBreaker.onSuccess(0, TimeUnit.NANOSECONDS);
        Assertions.assertThat(circuitBreaker.getMetrics().getNumberOfSuccessfulCalls()).isEqualTo(2);

        circuitBreaker.transitionToMetricsOnlyState();
        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(ICircuitBreakerService.State.METRICS_ONLY);
        Assertions.assertThat(circuitBreaker.getMetrics().getNumberOfSuccessfulCalls()).isEqualTo(0);
    }

//    @Test
//    public void shouldRecordMetricsInMetricsOnlyState() {
//        // A ring buffer with size 5 is used in closed state
//        // Initially the CircuitBreaker is closed
//        circuitBreaker.transitionToMetricsOnlyState();
//        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
//        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.METRICS_ONLY);
//        assertThatMetricsAreReset();
//        circuitBreaker.getEventPublisher().onSuccess(mockOnSuccessEventConsumer);
//        circuitBreaker.getEventPublisher().onError(mockOnErrorEventConsumer);
//        circuitBreaker.getEventPublisher().onStateTransition(mockOnStateTransitionEventConsumer);
//        circuitBreaker.getEventPublisher().onFailureRateExceeded(mockOnFailureRateExceededEventConsumer);
//
//        // Call 1 is a failure
//        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
//        circuitBreaker.onError(0, TimeUnit.NANOSECONDS, new RuntimeException());
//        verify(mockOnErrorEventConsumer, times(1)).consumeEvent(any(CircuitBreakerOnErrorEvent.class));
//        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.METRICS_ONLY);
//        assertCircuitBreakerMetricsEqualTo(-1f, 0, 1, 1, 0L);
//
//        // Call 2 is a failure
//        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
//        circuitBreaker.onError(0, TimeUnit.NANOSECONDS, new RuntimeException());
//        verify(mockOnErrorEventConsumer, times(2)).consumeEvent(any(CircuitBreakerOnErrorEvent.class));
//        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.METRICS_ONLY);
//        assertCircuitBreakerMetricsEqualTo(-1f, 0, 2, 2, 0L);
//
//        // Call 3 is a failure
//        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
//        circuitBreaker.onError(0, TimeUnit.NANOSECONDS, new RuntimeException());
//        verify(mockOnErrorEventConsumer, times(3)).consumeEvent(any(CircuitBreakerOnErrorEvent.class));
//        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.METRICS_ONLY);
//        assertCircuitBreakerMetricsEqualTo(-1f, 0, 3, 3, 0L);
//
//        // Call 4 is a success
//        Assertions.assertThat(circuitBreaker.tryAcquirePermission()).isEqualTo(true);
//        circuitBreaker.onSuccess(0, TimeUnit.NANOSECONDS);
//        verify(mockOnSuccessEventConsumer, times(1)).consumeEvent(any(CircuitBreakerOnSuccessEvent.class));
//        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.METRICS_ONLY);
//        assertCircuitBreakerMetricsEqualTo(-1f, 1, 4, 3, 0L);
//
//        // Call 5 is a success
//        circuitBreaker.onSuccess(0, TimeUnit.NANOSECONDS);
//        verify(mockOnSuccessEventConsumer, times(2)).consumeEvent(any(CircuitBreakerOnSuccessEvent.class));
//
//        // The ring buffer is filled and the failure rate is above 50%
//        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.METRICS_ONLY);
//        verify(mockOnStateTransitionEventConsumer, never()).consumeEvent(any(CircuitBreakerOnStateTransitionEvent.class));
//        verify(mockOnFailureRateExceededEventConsumer, times(1)).consumeEvent(any(CircuitBreakerOnFailureRateExceededEvent.class));
//        Assertions.assertThat(circuitBreaker.getMetrics().getNumberOfBufferedCalls()).isEqualTo(5);
//        Assertions.assertThat(circuitBreaker.getMetrics().getNumberOfFailedCalls()).isEqualTo(3);
//        Assertions.assertThat(circuitBreaker.getMetrics().getFailureRate()).isEqualTo(60.0f);
//        assertCircuitBreakerMetricsEqualTo(60.0f, 2, 5, 3, 0L);
//
//        circuitBreaker.onError(0, TimeUnit.NANOSECONDS, new RuntimeException());
//        verify(mockOnFailureRateExceededEventConsumer, times(1)).consumeEvent(any(CircuitBreakerOnFailureRateExceededEvent.class));
//        verify(mockOnErrorEventConsumer, times(4)).consumeEvent(any(CircuitBreakerOnErrorEvent.class));
//        Assertions.assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.METRICS_ONLY);
//    }

    private void assertCircuitBreakerMetricsEqualTo(Float expectedFailureRate,
            Integer expectedSuccessCalls, Integer expectedBufferedCalls, Integer expectedFailedCalls,
            Long expectedNotPermittedCalls) {
        assertCircuitBreakerMetricsEqualTo(circuitBreaker, expectedFailureRate,
                expectedSuccessCalls, expectedBufferedCalls, expectedFailedCalls,
                expectedNotPermittedCalls);
    }

    private void assertCircuitBreakerMetricsEqualTo(CircuitBreaker toTest,
            Float expectedFailureRate, Integer expectedSuccessCalls, Integer expectedBufferedCalls,
            Integer expectedFailedCalls, Long expectedNotPermittedCalls) {
        final CircuitBreaker.Metrics metrics = toTest.getMetrics();
        Assertions.assertThat(metrics.getFailureRate()).isEqualTo(expectedFailureRate);
        Assertions.assertThat(metrics.getNumberOfSuccessfulCalls()).isEqualTo(expectedSuccessCalls);
        Assertions.assertThat(metrics.getNumberOfBufferedCalls()).isEqualTo(expectedBufferedCalls);
        Assertions.assertThat(metrics.getNumberOfFailedCalls()).isEqualTo(expectedFailedCalls);
        Assertions.assertThat(metrics.getNumberOfNotPermittedCalls()).isEqualTo(expectedNotPermittedCalls);
    }

    private void assertThatMetricsAreReset() {
        assertCircuitBreakerMetricsEqualTo(-1f, 0, 0, 0, 0L);
    }

}
