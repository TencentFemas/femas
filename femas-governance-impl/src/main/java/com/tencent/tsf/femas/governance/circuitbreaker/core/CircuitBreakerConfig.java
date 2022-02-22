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

package com.tencent.tsf.femas.governance.circuitbreaker.core;

import com.tencent.tsf.femas.governance.circuitbreaker.rule.CircuitBreakerRule;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.core.lang.Nullable;
import io.github.resilience4j.core.predicate.PredicateCreator;
import java.time.Duration;
import java.util.function.Predicate;


/**
 * A {@link CircuitBreakerConfig} configures a {@link CircuitBreaker}
 */
public class CircuitBreakerConfig {

    public static final int DEFAULT_FAILURE_RATE_THRESHOLD = 50; // Percentage
    public static final int DEFAULT_SLOW_CALL_RATE_THRESHOLD = 100; // Percentage
    public static final int DEFAULT_WAIT_DURATION_IN_OPEN_STATE = 60; // Seconds
    public static final int DEFAULT_PERMITTED_CALLS_IN_HALF_OPEN_STATE = 10;
    public static final int DEFAULT_MINIMUM_NUMBER_OF_CALLS = 100;
    public static final int DEFAULT_SLIDING_WINDOW_SIZE = 100;
    public static final int DEFAULT_SLOW_CALL_DURATION_THRESHOLD = 60; // Seconds
    //统计metrics方式，默认计数器窗口
    public static final SlidingWindowType DEFAULT_SLIDING_WINDOW_TYPE = SlidingWindowType.COUNT_BASED;
    public static final boolean DEFAULT_WRITABLE_STACK_TRACE_ENABLED = true;
    private static final Predicate<Throwable> DEFAULT_RECORD_EXCEPTION_PREDICATE = throwable -> true;
    private static final Predicate<Throwable> DEFAULT_IGNORE_EXCEPTION_PREDICATE = throwable -> false;
    // The default exception predicate counts all exceptions as failures.
    private Predicate<Throwable> recordExceptionPredicate = DEFAULT_RECORD_EXCEPTION_PREDICATE;
    // The default exception predicate ignores no exceptions.
    private Predicate<Throwable> ignoreExceptionPredicate = DEFAULT_IGNORE_EXCEPTION_PREDICATE;

    @SuppressWarnings("unchecked")
    private Class<? extends Throwable>[] recordExceptions = new Class[0];
    @SuppressWarnings("unchecked")
    private Class<? extends Throwable>[] ignoreExceptions = new Class[0];

    private float failureRateThreshold = DEFAULT_FAILURE_RATE_THRESHOLD;
    private int permittedNumberOfCallsInHalfOpenState = DEFAULT_PERMITTED_CALLS_IN_HALF_OPEN_STATE;
    private int slidingWindowSize = DEFAULT_SLIDING_WINDOW_SIZE;
    private SlidingWindowType slidingWindowType = DEFAULT_SLIDING_WINDOW_TYPE;
    private int minimumNumberOfCalls = DEFAULT_MINIMUM_NUMBER_OF_CALLS;
    private boolean writableStackTraceEnabled = DEFAULT_WRITABLE_STACK_TRACE_ENABLED;
    private boolean automaticTransitionFromOpenToHalfOpenEnabled = false;
    private IntervalFunction waitIntervalFunctionInOpenState = IntervalFunction
            .of(Duration.ofSeconds(DEFAULT_WAIT_DURATION_IN_OPEN_STATE));
    private float slowCallRateThreshold = DEFAULT_SLOW_CALL_RATE_THRESHOLD;
    private Duration slowCallDurationThreshold = Duration
            .ofSeconds(DEFAULT_SLOW_CALL_DURATION_THRESHOLD);
    private CircuitBreakerRule circuitBreakerRule;

    private CircuitBreakerConfig() {
    }

    /**
     * Returns a builder to create a custom CircuitBreakerConfig.
     *
     * @return a {@link Builder}
     */
    public static Builder custom() {
        return new Builder();
    }

    /**
     * Returns a builder to create a custom CircuitBreakerConfig based on another
     * CircuitBreakerConfig.
     *
     * @return a {@link Builder}
     */
    public static Builder from(CircuitBreakerConfig baseConfig) {
        return new Builder(baseConfig);
    }

    /**
     * Creates a default CircuitBreaker configuration.
     *
     * @return a default CircuitBreaker configuration.
     */
    public static CircuitBreakerConfig ofDefaults() {
        return new Builder().build();
    }

    public float getFailureRateThreshold() {
        return failureRateThreshold;
    }

    /**
     * Returns an interval function which controls how long the CircuitBreaker should stay open,
     * before it switches to half open.
     *
     * @return the CircuitBreakerConfig.Builder
     */
    public IntervalFunction getWaitIntervalFunctionInOpenState() {
        return waitIntervalFunctionInOpenState;
    }

    public int getSlidingWindowSize() {
        return slidingWindowSize;
    }

    public Predicate<Throwable> getRecordExceptionPredicate() {
        return recordExceptionPredicate;
    }

    public Predicate<Throwable> getIgnoreExceptionPredicate() {
        return ignoreExceptionPredicate;
    }

    public boolean isAutomaticTransitionFromOpenToHalfOpenEnabled() {
        return automaticTransitionFromOpenToHalfOpenEnabled;
    }

    public int getMinimumNumberOfCalls() {
        return minimumNumberOfCalls;
    }

    public boolean isWritableStackTraceEnabled() {
        return writableStackTraceEnabled;
    }

    public int getPermittedNumberOfCallsInHalfOpenState() {
        return permittedNumberOfCallsInHalfOpenState;
    }

    public SlidingWindowType getSlidingWindowType() {
        return slidingWindowType;
    }

    public CircuitBreakerRule getCircuitBreakerRule() {
        return circuitBreakerRule;
    }

    public float getSlowCallRateThreshold() {
        return slowCallRateThreshold;
    }

    public Duration getSlowCallDurationThreshold() {
        return slowCallDurationThreshold;
    }

    public enum SlidingWindowType {
        TIME_BASED, COUNT_BASED
    }

    public static class Builder {

        @Nullable
        private Predicate<Throwable> recordExceptionPredicate;
        @Nullable
        private Predicate<Throwable> ignoreExceptionPredicate;

        @SuppressWarnings("unchecked")
        private Class<? extends Throwable>[] recordExceptions = new Class[0];
        @SuppressWarnings("unchecked")
        private Class<? extends Throwable>[] ignoreExceptions = new Class[0];

        private float failureRateThreshold = DEFAULT_FAILURE_RATE_THRESHOLD;
        private int minimumNumberOfCalls = DEFAULT_MINIMUM_NUMBER_OF_CALLS;
        private boolean writableStackTraceEnabled = DEFAULT_WRITABLE_STACK_TRACE_ENABLED;
        private int permittedNumberOfCallsInHalfOpenState = DEFAULT_PERMITTED_CALLS_IN_HALF_OPEN_STATE;
        private int slidingWindowSize = DEFAULT_SLIDING_WINDOW_SIZE;

        private IntervalFunction waitIntervalFunctionInOpenState = IntervalFunction
                .of(Duration.ofSeconds(DEFAULT_SLOW_CALL_DURATION_THRESHOLD));

        private boolean automaticTransitionFromOpenToHalfOpenEnabled = false;
        private SlidingWindowType slidingWindowType = DEFAULT_SLIDING_WINDOW_TYPE;
        private float slowCallRateThreshold = DEFAULT_SLOW_CALL_RATE_THRESHOLD;
        private Duration slowCallDurationThreshold = Duration
                .ofSeconds(DEFAULT_SLOW_CALL_DURATION_THRESHOLD);
        private CircuitBreakerRule circuitBreakerRule;

        public Builder(CircuitBreakerConfig baseConfig) {
            this.waitIntervalFunctionInOpenState = baseConfig.waitIntervalFunctionInOpenState;
            this.permittedNumberOfCallsInHalfOpenState = baseConfig.permittedNumberOfCallsInHalfOpenState;
            this.slidingWindowSize = baseConfig.slidingWindowSize;
            this.slidingWindowType = baseConfig.slidingWindowType;
            this.minimumNumberOfCalls = baseConfig.minimumNumberOfCalls;
            this.failureRateThreshold = baseConfig.failureRateThreshold;
            this.ignoreExceptions = baseConfig.ignoreExceptions;
            this.recordExceptions = baseConfig.recordExceptions;
            this.recordExceptionPredicate = baseConfig.recordExceptionPredicate;
            this.ignoreExceptionPredicate = baseConfig.ignoreExceptionPredicate;
            this.automaticTransitionFromOpenToHalfOpenEnabled = baseConfig.automaticTransitionFromOpenToHalfOpenEnabled;
            this.slowCallRateThreshold = baseConfig.slowCallRateThreshold;
            this.slowCallDurationThreshold = baseConfig.slowCallDurationThreshold;
            this.writableStackTraceEnabled = baseConfig.writableStackTraceEnabled;
        }

        public Builder() {

        }

        /**
         * Configures the failure rate threshold in percentage. If the failure rate is equal to or
         * greater than the threshold, the CircuitBreaker transitions to open and starts
         * short-circuiting calls.
         * <p>
         * The threshold must be greater than 0 and not greater than 100. Default value is 50
         * percentage.
         *
         * @param failureRateThreshold the failure rate threshold in percentage
         * @return the CircuitBreakerConfig.Builder
         * @throws IllegalArgumentException if {@code failureRateThreshold <= 0 ||
         *         failureRateThreshold > 100}
         */
        public Builder failureRateThreshold(float failureRateThreshold) {
            if (failureRateThreshold <= 0 || failureRateThreshold > 100) {
                throw new IllegalArgumentException(
                        "failureRateThreshold must be between 1 and 100");
            }
            this.failureRateThreshold = failureRateThreshold;
            return this;
        }

        /**
         * Configures a threshold in percentage. The CircuitBreaker considers a call as slow when
         * the call duration is greater than {@link #slowCallDurationThreshold(Duration)}. When the
         * percentage of slow calls is equal to or greater than the threshold, the CircuitBreaker
         * transitions to open and starts short-circuiting calls.
         *
         * <p>
         * The threshold must be greater than 0 and not greater than 100. Default value is 100
         * percentage which means that all recorded calls must be slower than {@link
         * #slowCallDurationThreshold(Duration)}.
         *
         * @param slowCallRateThreshold the slow calls threshold in percentage
         * @return the CircuitBreakerConfig.Builder
         * @throws IllegalArgumentException if {@code slowCallRateThreshold <= 0 ||
         *         slowCallRateThreshold > 100}
         */
        public Builder slowCallRateThreshold(float slowCallRateThreshold) {
            if (slowCallRateThreshold <= 0 || slowCallRateThreshold > 100) {
                throw new IllegalArgumentException(
                        "slowCallRateThreshold must be between 1 and 100");
            }
            this.slowCallRateThreshold = slowCallRateThreshold;
            return this;
        }

        /**
         * Enables writable stack traces. When set to false, {@link Exception#getStackTrace()}
         * returns a zero length array. This may be used to reduce log spam when the circuit breaker
         * is open as the cause of the exceptions is already known (the circuit breaker is
         * short-circuiting calls).
         *
         * @param writableStackTraceEnabled the flag to enable writable stack traces.
         * @return the CircuitBreakerConfig.Builder
         */
        public Builder writableStackTraceEnabled(boolean writableStackTraceEnabled) {
            this.writableStackTraceEnabled = writableStackTraceEnabled;
            return this;
        }

        /**
         * Configures an interval function with a fixed wait duration which controls how long the
         * CircuitBreaker should stay open, before it switches to half open. Default value is 60
         * seconds.
         *
         * @param waitDurationInOpenState the wait duration which specifies how long the
         *         CircuitBreaker should stay open
         * @return the CircuitBreakerConfig.Builder
         * @throws IllegalArgumentException if {@code waitDurationInOpenState.toMillis() < 1}
         */
        public Builder waitDurationInOpenState(Duration waitDurationInOpenState) {
            long waitDurationInMillis = waitDurationInOpenState.toMillis();
            if (waitDurationInMillis < 1) {
                throw new IllegalArgumentException(
                        "waitDurationInOpenState must be at least 1[ms]");
            }
            this.waitIntervalFunctionInOpenState = IntervalFunction.of(waitDurationInMillis);
            return this;
        }

        /**
         * Configures an interval function which controls how long the CircuitBreaker should stay
         * open, before it switches to half open. The default interval function returns a fixed wait
         * duration of 60 seconds.
         * <p>
         * A custom interval function is useful if you need an exponential backoff algorithm.
         *
         * @param waitIntervalFunctionInOpenState Interval function that returns wait time as a
         *         function of attempts
         * @return the CircuitBreakerConfig.Builder
         */
        public Builder waitIntervalFunctionInOpenState(
                IntervalFunction waitIntervalFunctionInOpenState) {
            this.waitIntervalFunctionInOpenState = waitIntervalFunctionInOpenState;
            return this;
        }

        /**
         * Configures the duration threshold above which calls are considered as slow and increase
         * the slow calls percentage. Default value is 60 seconds.
         *
         * @param slowCallDurationThreshold the duration above which calls are considered as slow
         * @return the CircuitBreakerConfig.Builder
         * @throws IllegalArgumentException if {@code slowCallDurationThreshold.toNanos() < 1}
         */
        public Builder slowCallDurationThreshold(Duration slowCallDurationThreshold) {
            if (slowCallDurationThreshold.toNanos() < 1) {
                throw new IllegalArgumentException(
                        "slowCallDurationThreshold must be at least 1[ns]");
            }
            this.slowCallDurationThreshold = slowCallDurationThreshold;
            return this;
        }

        /**
         * Configures the number of permitted calls when the CircuitBreaker is half open.
         * <p>
         * The size must be greater than 0. Default size is 10.
         *
         * @param permittedNumberOfCallsInHalfOpenState the permitted number of calls when the
         *         CircuitBreaker is half open
         * @return the CircuitBreakerConfig.Builder
         * @throws IllegalArgumentException if {@code permittedNumberOfCallsInHalfOpenState < 1}
         */
        public Builder permittedNumberOfCallsInHalfOpenState(
                int permittedNumberOfCallsInHalfOpenState) {
            if (permittedNumberOfCallsInHalfOpenState < 1) {
                throw new IllegalArgumentException(
                        "permittedNumberOfCallsInHalfOpenState must be greater than 0");
            }
            this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
            return this;
        }

        /**
         * Configures the sliding window which is used to record the outcome of calls when the
         * CircuitBreaker is closed. {@code slidingWindowSize} configures the size of the sliding
         * window. Sliding window can either be count-based or time-based, specified by {@code
         * slidingWindowType}. {@code minimumNumberOfCalls} configures the minimum number of calls
         * which are required (per sliding window period) before the CircuitBreaker can calculate
         * the error rate. For example, if {@code minimumNumberOfCalls} is 10, then at least 10
         * calls must be recorded, before the failure rate can be calculated. If only 9 calls have
         * been recorded, the CircuitBreaker will not transition to open, even if all 9 calls have
         * failed.
         * <p>
         * If {@code slidingWindowSize} is 100 and {@code slidingWindowType} is COUNT_BASED, the
         * last 100 calls are recorded and aggregated. If {@code slidingWindowSize} is 10 and {@code
         * slidingWindowType} is TIME_BASED, the calls of the last 10 seconds are recorded and
         * aggregated.
         * <p>
         * The {@code slidingWindowSize} must be greater than 0. The {@code minimumNumberOfCalls}
         * must be greater than 0. If the {@code slidingWindowType} is COUNT_BASED, the {@code
         * minimumNumberOfCalls} may not be greater than {@code slidingWindowSize}. If a greater
         * value is provided, {@code minimumNumberOfCalls} will be equal to {@code
         * slidingWindowSize}. If the {@code slidingWindowType} is TIME_BASED, the {@code
         * minimumNumberOfCalls} may be any amount.
         * <p>
         * Default slidingWindowSize is 100, minimumNumberOfCalls is 100 and slidingWindowType is
         * COUNT_BASED.
         *
         * @param slidingWindowSize the size of the sliding window when the CircuitBreaker is
         *         closed.
         * @param minimumNumberOfCalls the minimum number of calls that must be recorded before the
         *         failure rate can be calculated.
         * @param slidingWindowType the type of the sliding window. Either COUNT_BASED or
         *         TIME_BASED.
         * @return the CircuitBreakerConfig.Builder
         * @throws IllegalArgumentException if {@code slidingWindowSize < 1 || minimumNumberOfCalls
         *         < 1}
         */
        public Builder slidingWindow(int slidingWindowSize, int minimumNumberOfCalls,
                SlidingWindowType slidingWindowType) {
            if (slidingWindowSize < 1) {
                throw new IllegalArgumentException("slidingWindowSize must be greater than 0");
            }
            if (minimumNumberOfCalls < 1) {
                throw new IllegalArgumentException("minimumNumberOfCalls must be greater than 0");
            }
            if (slidingWindowType == SlidingWindowType.COUNT_BASED) {
                this.minimumNumberOfCalls = Math.min(minimumNumberOfCalls, slidingWindowSize);
            } else {
                this.minimumNumberOfCalls = minimumNumberOfCalls;
            }
            this.slidingWindowSize = slidingWindowSize;
            this.slidingWindowType = slidingWindowType;
            return this;
        }

        /**
         * Configures the size of the sliding window which is used to record the outcome of calls
         * when the CircuitBreaker is closed. {@code slidingWindowSize} configures the size of the
         * sliding window.
         * <p>
         * The {@code slidingWindowSize} must be greater than 0.
         * <p>
         * Default slidingWindowSize is 100.
         *
         * @param slidingWindowSize the size of the sliding window when the CircuitBreaker is
         *         closed.
         * @return the CircuitBreakerConfig.Builder
         * @throws IllegalArgumentException if {@code slidingWindowSize < 1}
         * @see #slidingWindow(int, int, SlidingWindowType)
         */
        public Builder slidingWindowSize(int slidingWindowSize) {
            if (slidingWindowSize < 1) {
                throw new IllegalArgumentException("slidingWindowSize must be greater than 0");
            }
            this.slidingWindowSize = slidingWindowSize;
            return this;
        }

        /**
         * Configures the minimum number of calls which are required (per sliding window period)
         * before the CircuitBreaker can calculate the error rate. For example, if {@code
         * minimumNumberOfCalls} is 10, then at least 10 calls must be recorded, before the failure
         * rate can be calculated. If only 9 calls have been recorded, the CircuitBreaker will not
         * transition to open, even if all 9 calls have failed.
         * <p>
         * Default minimumNumberOfCalls is 100
         *
         * @param minimumNumberOfCalls the minimum number of calls that must be recorded before the
         *         failure rate can be calculated.
         * @return the CircuitBreakerConfig.Builder
         * @throws IllegalArgumentException if {@code minimumNumberOfCalls < 1}
         * @see #slidingWindow(int, int, SlidingWindowType)
         */
        public Builder minimumNumberOfCalls(int minimumNumberOfCalls) {
            if (minimumNumberOfCalls < 1) {
                throw new IllegalArgumentException("minimumNumberOfCalls must be greater than 0");
            }
            this.minimumNumberOfCalls = minimumNumberOfCalls;
            return this;
        }

        /**
         * Configures the type of the sliding window which is used to record the outcome of calls
         * when the CircuitBreaker is closed. Sliding window can either be count-based or
         * time-based.
         * <p>
         * Default slidingWindowType is COUNT_BASED.
         *
         * @param slidingWindowType the type of the sliding window. Either COUNT_BASED or
         *         TIME_BASED.
         * @return the CircuitBreakerConfig.Builder
         * @see #slidingWindow(int, int, SlidingWindowType)
         */
        public Builder slidingWindowType(SlidingWindowType slidingWindowType) {
            this.slidingWindowType = slidingWindowType;
            return this;
        }

        /**
         * Configures a Predicate which evaluates if an exception should be recorded as a failure
         * and thus increase the failure rate. The Predicate must return true if the exception
         * should count as a failure. The Predicate must return false, if the exception should count
         * as a success, unless the exception is explicitly ignored by {@link
         * #ignoreExceptions(Class[])} or {@link #ignoreException(Predicate)}.
         *
         * @param predicate the Predicate which evaluates if an exception should count as a failure
         * @return the CircuitBreakerConfig.Builder
         */
        public Builder recordException(Predicate<Throwable> predicate) {
            this.recordExceptionPredicate = predicate;
            return this;
        }

        /**
         * Configures a Predicate which evaluates if an exception should be ignored and neither
         * count as a failure nor success. The Predicate must return true if the exception should be
         * ignored. The Predicate must return false, if the exception should count as a failure.
         *
         * @param predicate the Predicate which evaluates if an exception should count as a failure
         * @return the CircuitBreakerConfig.Builder
         */
        public Builder ignoreException(Predicate<Throwable> predicate) {
            this.ignoreExceptionPredicate = predicate;
            return this;
        }

        /**
         * Configures a list of error classes that are recorded as a failure and thus increase the
         * failure rate. Any exception matching or inheriting from one of the list should count as a
         * failure, unless ignored via {@link #ignoreExceptions(Class[])} or {@link
         * #ignoreException(Predicate)}.
         *
         * @param errorClasses the error classes that are recorded
         * @return the CircuitBreakerConfig.Builder
         * @see #ignoreExceptions(Class[]) ). Ignoring an exception has priority over recording an
         *         exception.
         *         <p>
         *         Example: recordExceptions(Throwable.class) and ignoreExceptions(RuntimeException.class)
         *         would capture all Errors and checked Exceptions, and ignore RuntimeExceptions.
         *         <p>
         *         For a more sophisticated exception management use the
         * @see #recordException(Predicate) method
         */
        @SuppressWarnings("unchecked")
        @SafeVarargs
        public final Builder recordExceptions(
                @Nullable Class<? extends Throwable>... errorClasses) {
            this.recordExceptions = errorClasses != null ? errorClasses : new Class[0];
            return this;
        }

        /**
         * Configures a list of error classes that are ignored and thus neither count as a failure
         * nor success. Any exception matching or inheriting from one of the list will not count as
         * a failure nor success, even if marked via {@link #recordExceptions(Class[])} or {@link
         * #recordException(Predicate)}.
         *
         * @param errorClasses the error classes that are ignored
         * @return the CircuitBreakerConfig.Builder
         * @see #recordExceptions(Class[]) . Ignoring an exception has priority over recording an
         *         exception.
         *         <p>
         *         Example: ignoreExceptions(Throwable.class) and recordExceptions(Exception.class) would
         *         capture nothing.
         *         <p>
         *         Example: ignoreExceptions(Exception.class) and recordExceptions(Throwable.class) would
         *         capture Errors.
         *         <p>
         *         For a more sophisticated exception management use the
         * @see #ignoreException(Predicate) method
         */
        @SuppressWarnings("unchecked")
        @SafeVarargs
        public final Builder ignoreExceptions(
                @Nullable Class<? extends Throwable>... errorClasses) {
            this.ignoreExceptions = errorClasses != null ? errorClasses : new Class[0];
            return this;
        }

        /**
         * Enables automatic transition from OPEN to HALF_OPEN state once the
         * waitDurationInOpenState has passed.
         *
         * @return the CircuitBreakerConfig.Builder
         */
        public Builder enableAutomaticTransitionFromOpenToHalfOpen() {
            this.automaticTransitionFromOpenToHalfOpenEnabled = true;
            return this;
        }

        /**
         * Enables automatic transition from OPEN to HALF_OPEN state once the
         * waitDurationInOpenState has passed.
         *
         * @param enableAutomaticTransitionFromOpenToHalfOpen the flag to enable the automatic
         *         transitioning.
         * @return the CircuitBreakerConfig.Builder
         */
        public Builder automaticTransitionFromOpenToHalfOpenEnabled(
                boolean enableAutomaticTransitionFromOpenToHalfOpen) {
            this.automaticTransitionFromOpenToHalfOpenEnabled = enableAutomaticTransitionFromOpenToHalfOpen;
            return this;
        }

        public Builder circuitBreakerRule(CircuitBreakerRule circuitBreakerRule) {
            this.circuitBreakerRule = circuitBreakerRule;
            return this;
        }

        /**
         * Builds a CircuitBreakerConfig
         *
         * @return the CircuitBreakerConfig
         */
        public CircuitBreakerConfig build() {
            CircuitBreakerConfig config = new CircuitBreakerConfig();
            config.waitIntervalFunctionInOpenState = waitIntervalFunctionInOpenState;
            config.slidingWindowType = slidingWindowType;
            config.slowCallDurationThreshold = slowCallDurationThreshold;
            config.slowCallRateThreshold = slowCallRateThreshold;
            config.failureRateThreshold = failureRateThreshold;
            config.slidingWindowSize = slidingWindowSize;
            config.minimumNumberOfCalls = minimumNumberOfCalls;
            config.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
            config.recordExceptions = recordExceptions;
            config.ignoreExceptions = ignoreExceptions;
            config.automaticTransitionFromOpenToHalfOpenEnabled = automaticTransitionFromOpenToHalfOpenEnabled;
            config.writableStackTraceEnabled = writableStackTraceEnabled;
            config.recordExceptionPredicate = createRecordExceptionPredicate();
            config.ignoreExceptionPredicate = createIgnoreFailurePredicate();
            config.circuitBreakerRule = circuitBreakerRule;
            return config;
        }

        private Predicate<Throwable> createIgnoreFailurePredicate() {
            return PredicateCreator.createExceptionsPredicate(ignoreExceptions)
                    .map(predicate -> ignoreExceptionPredicate != null ? predicate
                            .or(ignoreExceptionPredicate) : predicate)
                    .orElseGet(() -> ignoreExceptionPredicate != null ? ignoreExceptionPredicate
                            : DEFAULT_IGNORE_EXCEPTION_PREDICATE);
        }

        private Predicate<Throwable> createRecordExceptionPredicate() {
            return PredicateCreator.createExceptionsPredicate(recordExceptions)
                    .map(predicate -> recordExceptionPredicate != null ? predicate
                            .or(recordExceptionPredicate) : predicate)
                    .orElseGet(() -> recordExceptionPredicate != null ? recordExceptionPredicate
                            : DEFAULT_RECORD_EXCEPTION_PREDICATE);
        }
    }
}
