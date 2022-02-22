/*
 *
 *  Copyright 2017: Robert Winkler
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

package com.tencent.tsf.femas.governance.circuitbreaker.core;

import com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService;
import com.tencent.tsf.femas.governance.circuitbreaker.core.internal.CircuitBreakerStateMachine;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A CircuitBreaker instance is thread-safe can be used to decorate multiple requests.
 * <p>
 * A {@link CircuitBreaker} manages the state of a backend system. The CircuitBreaker is implemented
 * via a finite state machine with five states: CLOSED, OPEN, HALF_OPEN, DISABLED AND FORCED_OPEN.
 * The CircuitBreaker does not know anything about the backend's state by itself, but uses the
 * information provided by the decorators via {@link CircuitBreaker#onSuccess} and {@link
 * CircuitBreaker#onError} events. Before communicating with the backend, the permission to do so
 * must be obtained via the method {@link CircuitBreaker#tryAcquirePermission()}.
 * <p>
 * The state of the CircuitBreaker changes from CLOSED to OPEN when the failure rate is above a
 * (configurable) threshold. Then, all access to the backend is rejected for a (configurable) time
 * duration. No further calls are permitted.
 * <p>
 * After the time duration has elapsed, the CircuitBreaker state changes from OPEN to HALF_OPEN and
 * allows a number of calls to see if the backend is still unavailable or has become available
 * again. If the failure rate is above the configured threshold, the state changes back to OPEN. If
 * the failure rate is below or equal to the threshold, the state changes back to CLOSED.
 */
public interface CircuitBreaker {

    /**
     * Creates a CircuitBreaker with a default CircuitBreaker configuration.
     *
     * @param name the name of the CircuitBreaker
     * @return a CircuitBreaker with a default CircuitBreaker configuration.
     */
    static CircuitBreaker ofDefaults(String name) {
        return new CircuitBreakerStateMachine(name);
    }

    /**
     * Creates a CircuitBreaker with a custom CircuitBreaker configuration.
     *
     * @param name the name of the CircuitBreaker
     * @param circuitBreakerConfig a custom CircuitBreaker configuration
     * @return a CircuitBreaker with a custom CircuitBreaker configuration.
     */
    static CircuitBreaker of(String name, CircuitBreakerConfig circuitBreakerConfig) {
        return new CircuitBreakerStateMachine(name, circuitBreakerConfig);
    }

    /**
     * Creates a CircuitBreaker with a custom CircuitBreaker configuration.
     * <p>
     * The {@code tags} passed will be appended to the tags already configured for the registry.
     * When tags (keys) of the two collide the tags passed with this method will override the tags
     * of the registry.
     *
     * @param name the name of the CircuitBreaker
     * @param circuitBreakerConfig a custom CircuitBreaker configuration
     * @param tags tags added to the Retry
     * @return a CircuitBreaker with a custom CircuitBreaker configuration.
     */
    static CircuitBreaker of(String name, CircuitBreakerConfig circuitBreakerConfig,
            io.vavr.collection.Map<String, String> tags) {
        return new CircuitBreakerStateMachine(name, circuitBreakerConfig, tags);
    }

    /**
     * Creates a CircuitBreaker with a custom CircuitBreaker configuration.
     *
     * @param name the name of the CircuitBreaker
     * @param circuitBreakerConfigSupplier a supplier of a custom CircuitBreaker configuration
     * @return a CircuitBreaker with a custom CircuitBreaker configuration.
     */
    static CircuitBreaker of(String name,
            Supplier<CircuitBreakerConfig> circuitBreakerConfigSupplier) {
        return new CircuitBreakerStateMachine(name, circuitBreakerConfigSupplier);
    }

    /**
     * Creates a CircuitBreaker with a custom CircuitBreaker configuration.
     * <p>
     * The {@code tags} passed will be appended to the tags already configured for the registry.
     * When tags (keys) of the two collide the tags passed with this method will override the tags
     * of the registry.
     *
     * @param name the name of the CircuitBreaker
     * @param circuitBreakerConfigSupplier a supplier of a custom CircuitBreaker configuration
     * @param tags tags added to the CircuitBreaker
     * @return a CircuitBreaker with a custom CircuitBreaker configuration.
     */
    static CircuitBreaker of(String name,
            Supplier<CircuitBreakerConfig> circuitBreakerConfigSupplier,
            io.vavr.collection.Map<String, String> tags) {
        return new CircuitBreakerStateMachine(name, circuitBreakerConfigSupplier, tags);
    }


    /**
     * Acquires a permission to execute a call, only if one is available at the time of invocation.
     * If a call is not permitted, the number of not permitted calls is increased.
     * <p>
     * Returns false when the state is OPEN or FORCED_OPEN. Returns true when the state is CLOSED or
     * DISABLED. Returns true when the state is HALF_OPEN and further test calls are allowed.
     * Returns false when the state is HALF_OPEN and the number of test calls has been reached. If
     * the state is HALF_OPEN, the number of allowed test calls is decreased. Important: Make sure
     * to call onSuccess or onError after the call is finished. If the call is cancelled before it
     * is invoked, you have to release the permission again.
     *
     * @return {@code true} if a permission was acquired and {@code false} otherwise
     */
    boolean tryAcquirePermission();

    /**
     * Releases a permission.
     * <p>
     * Should only be used when a permission was acquired but not used. Otherwise use {@link
     * CircuitBreaker#onSuccess(long, TimeUnit)} or {@link CircuitBreaker#onError(long, TimeUnit,
     * Throwable)} to signal a completed or failed call.
     * <p>
     * If the state is HALF_OPEN, the number of allowed test calls is increased by one.
     */
    void releasePermission();

    /**
     * Try to obtain a permission to execute a call. If a call is not permitted, the number of not
     * permitted calls is increased.
     * <p>
     * Throws a CallNotPermittedException when the state is OPEN or FORCED_OPEN. Returns when the
     * state is CLOSED or DISABLED. Returns when the state is HALF_OPEN and further test calls are
     * allowed. Throws a CallNotPermittedException when the state is HALF_OPEN and the number of
     * test calls has been reached. If the state is HALF_OPEN, the number of allowed test calls is
     * decreased. Important: Make sure to call onSuccess or onError after the call is finished. If
     * the call is cancelled before it is invoked, you have to release the permission again.
     *
     * @throws CallNotPermittedException when CircuitBreaker is OPEN or HALF_OPEN and no further
     *         test calls are permitted.
     */
    void acquirePermission();

    /**
     * Records a failed call. This method must be invoked when a call failed.
     *
     * @param duration The elapsed time duration of the call
     * @param durationUnit The duration unit
     * @param throwable The throwable which must be recorded
     */
    void onError(long duration, TimeUnit durationUnit, Throwable throwable);

    /**
     * Records a successful call. This method must be invoked when a call was
     * successful.
     *
     * @param duration The elapsed time duration of the call
     * @param durationUnit The duration unit
     */
    void onSuccess(long duration, TimeUnit durationUnit);

    /**
     * Returns the circuit breaker to its original closed state, losing statistics.
     * <p>
     * Should only be used, when you want to want to fully reset the circuit breaker without
     * creating a new one.
     */
    void reset();

    void registerCallback(StateTransitionCallback callback);

    void setCircuitBreakerTargetObject(Object targetObject);

    /**
     * Transitions the state machine to CLOSED state.
     * <p>
     * Should only be used, when you want to force a state transition. State transition are normally
     * done internally.
     */
    void transitionToClosedState();

    /**
     * Transitions the state machine to OPEN state.
     * <p>
     * Should only be used, when you want to force a state transition. State transition are normally
     * done internally.
     */
    void transitionToOpenState();

    /**
     * Transitions the state machine to HALF_OPEN state.
     * <p>
     * Should only be used, when you want to force a state transition. State transition are normally
     * done internally.
     */
    void transitionToHalfOpenState();

    /**
     * Transitions the state machine to a DISABLED state, stopping state transition, metrics and
     * event publishing.
     * <p>
     * Should only be used, when you want to disable the circuit breaker allowing all calls to pass.
     * To recover from this state you must force a new state transition
     */
    void transitionToDisabledState();

    /**
     * Transitions the state machine to METRICS_ONLY state, stopping all state transitions but
     * continues to capture metrics and publish events.
     * <p>
     * Should only be used when you want to collect metrics while keeping the circuit breaker
     * disabled, allowing all calls to pass.
     * To recover from this state you must force a new state transition.
     */
    void transitionToMetricsOnlyState();

    /**
     * Transitions the state machine to a FORCED_OPEN state,  stopping state transition, metrics and
     * event publishing.
     * <p>
     * Should only be used, when you want to disable the circuit breaker allowing no call to pass.
     * To recover from this state you must force a new state transition
     */
    void transitionToForcedOpenState();

    /**
     * Returns the name of this CircuitBreaker.
     *
     * @return the name of this CircuitBreaker
     */
    String getName();

    /**
     * Returns the state of this CircuitBreaker.
     *
     * @return the state of this CircuitBreaker
     */
    ICircuitBreakerService.State getState();

    /**
     * Returns the CircuitBreakerConfig of this CircuitBreaker.
     *
     * @return the CircuitBreakerConfig of this CircuitBreaker
     */
    CircuitBreakerConfig getCircuitBreakerConfig();

    /**
     * Returns the Metrics of this CircuitBreaker.
     *
     * @return the Metrics of this CircuitBreaker
     */
    Metrics getMetrics();

    /**
     * States of the CircuitBreaker state machine.
     */


    /**
     * State transitions of the CircuitBreaker state machine.
     */
    enum StateTransition {
        CLOSED_TO_OPEN(ICircuitBreakerService.State.CLOSED, ICircuitBreakerService.State.OPEN),
        CLOSED_TO_DISABLED(ICircuitBreakerService.State.CLOSED, ICircuitBreakerService.State.DISABLED),
        CLOSED_TO_METRICS_ONLY(ICircuitBreakerService.State.CLOSED, ICircuitBreakerService.State.METRICS_ONLY),
        CLOSED_TO_FORCED_OPEN(ICircuitBreakerService.State.CLOSED, ICircuitBreakerService.State.FORCED_OPEN),
        HALF_OPEN_TO_CLOSED(ICircuitBreakerService.State.HALF_OPEN, ICircuitBreakerService.State.CLOSED),
        HALF_OPEN_TO_OPEN(ICircuitBreakerService.State.HALF_OPEN, ICircuitBreakerService.State.OPEN),
        HALF_OPEN_TO_DISABLED(ICircuitBreakerService.State.HALF_OPEN, ICircuitBreakerService.State.DISABLED),
        HALF_OPEN_TO_METRICS_ONLY(ICircuitBreakerService.State.HALF_OPEN, ICircuitBreakerService.State.METRICS_ONLY),
        HALF_OPEN_TO_FORCED_OPEN(ICircuitBreakerService.State.HALF_OPEN, ICircuitBreakerService.State.FORCED_OPEN),
        OPEN_TO_CLOSED(ICircuitBreakerService.State.OPEN, ICircuitBreakerService.State.CLOSED),
        OPEN_TO_HALF_OPEN(ICircuitBreakerService.State.OPEN, ICircuitBreakerService.State.HALF_OPEN),
        OPEN_TO_DISABLED(ICircuitBreakerService.State.OPEN, ICircuitBreakerService.State.DISABLED),
        OPEN_TO_METRICS_ONLY(ICircuitBreakerService.State.OPEN, ICircuitBreakerService.State.METRICS_ONLY),
        OPEN_TO_FORCED_OPEN(ICircuitBreakerService.State.OPEN, ICircuitBreakerService.State.FORCED_OPEN),
        FORCED_OPEN_TO_CLOSED(ICircuitBreakerService.State.FORCED_OPEN, ICircuitBreakerService.State.CLOSED),
        FORCED_OPEN_TO_OPEN(ICircuitBreakerService.State.FORCED_OPEN, ICircuitBreakerService.State.OPEN),
        FORCED_OPEN_TO_DISABLED(ICircuitBreakerService.State.FORCED_OPEN, ICircuitBreakerService.State.DISABLED),
        FORCED_OPEN_TO_METRICS_ONLY(ICircuitBreakerService.State.FORCED_OPEN,
                ICircuitBreakerService.State.METRICS_ONLY),
        FORCED_OPEN_TO_HALF_OPEN(ICircuitBreakerService.State.FORCED_OPEN, ICircuitBreakerService.State.HALF_OPEN),
        DISABLED_TO_CLOSED(ICircuitBreakerService.State.DISABLED, ICircuitBreakerService.State.CLOSED),
        DISABLED_TO_OPEN(ICircuitBreakerService.State.DISABLED, ICircuitBreakerService.State.OPEN),
        DISABLED_TO_FORCED_OPEN(ICircuitBreakerService.State.DISABLED, ICircuitBreakerService.State.FORCED_OPEN),
        DISABLED_TO_HALF_OPEN(ICircuitBreakerService.State.DISABLED, ICircuitBreakerService.State.HALF_OPEN),
        DISABLED_TO_METRICS_ONLY(ICircuitBreakerService.State.DISABLED, ICircuitBreakerService.State.METRICS_ONLY),
        METRICS_ONLY_TO_CLOSED(ICircuitBreakerService.State.METRICS_ONLY, ICircuitBreakerService.State.CLOSED),
        METRICS_ONLY_TO_FORCED_OPEN(ICircuitBreakerService.State.METRICS_ONLY,
                ICircuitBreakerService.State.FORCED_OPEN),
        METRICS_ONLY_TO_DISABLED(ICircuitBreakerService.State.METRICS_ONLY, ICircuitBreakerService.State.DISABLED);

        private static final Map<Tuple2<ICircuitBreakerService.State, ICircuitBreakerService.State>, StateTransition> STATE_TRANSITION_MAP = Arrays
                .stream(StateTransition.values())
                .collect(Collectors.toMap(v -> Tuple.of(v.fromState, v.toState), Function.identity()));
        private final ICircuitBreakerService.State fromState;
        private final ICircuitBreakerService.State toState;

        StateTransition(ICircuitBreakerService.State fromState, ICircuitBreakerService.State toState) {
            this.fromState = fromState;
            this.toState = toState;
        }

        public static StateTransition transitionBetween(String name, ICircuitBreakerService.State fromState,
                ICircuitBreakerService.State toState) {
            final StateTransition stateTransition = STATE_TRANSITION_MAP
                    .get(Tuple.of(fromState, toState));
            if (stateTransition == null) {
                throw new IllegalStateTransitionException(name, fromState, toState);
            }
            return stateTransition;
        }

        public ICircuitBreakerService.State getFromState() {
            return fromState;
        }

        public ICircuitBreakerService.State getToState() {
            return toState;
        }

        @Override
        public String toString() {
            return String.format("State transition from %s to %s", fromState, toState);
        }
    }

    interface Metrics {

        /**
         * Returns the current failure rate in percentage. If the number of measured calls is below
         * the minimum number of measured calls, it returns -1.
         *
         * @return the failure rate in percentage
         */
        float getFailureRate();

        /**
         * Returns the current percentage of calls which were slower than a certain threshold. If
         * the number of measured calls is below the minimum number of measured calls, it returns
         * -1.
         *
         * @return the failure rate in percentage
         */
        float getSlowCallRate();

        /**
         * Returns the current total number of calls which were slower than a certain threshold.
         *
         * @return the current total number of calls which were slower than a certain threshold
         */
        int getNumberOfSlowCalls();

        /**
         * Returns the current number of successful calls which were slower than a certain
         * threshold.
         *
         * @return the current number of successful calls which were slower than a certain threshold
         */
        int getNumberOfSlowSuccessfulCalls();

        /**
         * Returns the current number of failed calls which were slower than a certain threshold.
         *
         * @return the current number of failed calls which were slower than a certain threshold
         */
        int getNumberOfSlowFailedCalls();

        /**
         * Returns the current total number of buffered calls in the ring buffer.
         *
         * @return he current total number of buffered calls in the ring buffer
         */
        int getNumberOfBufferedCalls();

        /**
         * Returns the current number of failed buffered calls in the ring buffer.
         *
         * @return the current number of failed buffered calls in the ring buffer
         */
        int getNumberOfFailedCalls();

        /**
         * Returns the current number of not permitted calls, when the state is OPEN.
         * <p>
         * The number of denied calls is always 0, when the CircuitBreaker state is CLOSED or
         * HALF_OPEN. The number of denied calls is only increased when the CircuitBreaker state is
         * OPEN.
         *
         * @return the current number of not permitted calls
         */
        long getNumberOfNotPermittedCalls();

        /**
         * Returns the current number of successful buffered calls in the ring buffer.
         *
         * @return the current number of successful buffered calls in the ring buffer
         */
        int getNumberOfSuccessfulCalls();
    }
}
