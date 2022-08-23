package com.tencent.tsf.femas.governance.circuitbreaker.core;

import com.tencent.tsf.femas.plugin.impl.config.rule.circuitbreaker.CircuitBreakerRule;
import com.tencent.tsf.femas.plugin.impl.config.rule.circuitbreaker.CircuitBreakerStrategy;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取熔断器的工厂类
 *
 * @author zhixinzxliu
 */
public class CircuitBreakerFactory {

    private static List<StateTransitionCallback> callbacks = new ArrayList<>();

    // 用于circuitBreaker创建好后动态添加回调
    private static ArrayList<CircuitBreaker> circuitBreakersContext = new ArrayList<>();

    /**
     * 根据熔断策略获取熔断器，目前默认实现为R4j
     *
     * @param circuitBreakerName 熔断器名
     * @param strategy 熔断器策略
     * @return
     */
    public static CircuitBreaker newCircuitBreaker(String circuitBreakerName, CircuitBreakerRule rule,
            CircuitBreakerStrategy strategy) {
        return newCircuitBreaker(circuitBreakerName, rule, strategy, false);
    }

    /**
     * 该方法增加自动状态转换参数
     * 在实例熔断级别，实例被熔断后，不会再放流量进来，需要开启自动状态转换
     *
     * @param circuitBreakerName 熔断器名
     * @param strategy 熔断器策略
     * @param automaticTransitionFromOpenToHalfOpenEnabled 是否开启自动从OPEN状态到HALF_OPEN状态转换
     * @return
     */
    public static CircuitBreaker newCircuitBreaker(String circuitBreakerName, CircuitBreakerRule rule,
            CircuitBreakerStrategy strategy, boolean automaticTransitionFromOpenToHalfOpenEnabled) {
        int failureRateThreshold = strategy.getFailureRateThreshold();
        int waitDurationInOpenState = strategy.getWaitDurationInOpenState();
        int slidingWindowSize = strategy.getSlidingWindowSize();
        int minimumNumberOfCalls = strategy.getMinimumNumberOfCalls();
        int slowCallDurationThreshold = strategy.getSlowCallDurationThreshold();
        int slowCallRateThreshold = strategy.getSlowCallRateThreshold();

        final CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(failureRateThreshold)
                .waitDurationInOpenState(Duration.ofSeconds(waitDurationInOpenState))
                .slidingWindowSize(slidingWindowSize)
                .minimumNumberOfCalls(minimumNumberOfCalls)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                .slowCallDurationThreshold(Duration.ofMillis(slowCallDurationThreshold))
                .slowCallRateThreshold(slowCallRateThreshold)
                // 目前记录所有异常，留下接口供未来只记录某些异常
                .recordExceptions(Throwable.class)
                .automaticTransitionFromOpenToHalfOpenEnabled(automaticTransitionFromOpenToHalfOpenEnabled)
                .circuitBreakerRule(rule)
                .build();

        CircuitBreaker circuitBreaker = CircuitBreaker.of(circuitBreakerName, config);

        for (StateTransitionCallback callback : callbacks) {
            circuitBreaker.registerCallback(callback);
        }
        circuitBreakersContext.add(circuitBreaker);
        return circuitBreaker;
    }

    /**
     * 目前需要在执行newFemasCircuitBreaker前注册callback
     *
     * TODO 后续修改顺序为任意时刻可注册callback
     *
     * @param callback
     */
    public static void registerCallback(StateTransitionCallback callback) {
        for (CircuitBreaker circuitBreaker : circuitBreakersContext) {
            circuitBreaker.registerCallback(callback);
        }
        callbacks.add(callback);
    }
}
