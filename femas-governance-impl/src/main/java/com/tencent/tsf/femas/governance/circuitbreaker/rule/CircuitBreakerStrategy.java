package com.tencent.tsf.femas.governance.circuitbreaker.rule;

import static com.tencent.tsf.femas.governance.circuitbreaker.constant.FemasCircuitBreakerConstant.MAX_EJECTION_RATE_THRESHOLD;
import static com.tencent.tsf.femas.governance.circuitbreaker.constant.FemasCircuitBreakerConstant.MAX_FAILURE_RATE_THRESHOLD;
import static com.tencent.tsf.femas.governance.circuitbreaker.constant.FemasCircuitBreakerConstant.MAX_SLIDING_WINDOW_SIZE;
import static com.tencent.tsf.femas.governance.circuitbreaker.constant.FemasCircuitBreakerConstant.MAX_WAIT_DURATION_IN_OPEN_STATE;
import static com.tencent.tsf.femas.governance.circuitbreaker.constant.FemasCircuitBreakerConstant.MINIMUN_NUMBER_OF_CALLS;
import static com.tencent.tsf.femas.governance.circuitbreaker.constant.FemasCircuitBreakerConstant.MIN_EJECTION_RATE_THRESHOLD;
import static com.tencent.tsf.femas.governance.circuitbreaker.constant.FemasCircuitBreakerConstant.MIN_FAILURE_RATE_THRESHOLD;
import static com.tencent.tsf.femas.governance.circuitbreaker.constant.FemasCircuitBreakerConstant.MIN_SLIDING_WINDOW_SIZE;
import static com.tencent.tsf.femas.governance.circuitbreaker.constant.FemasCircuitBreakerConstant.MIN_WAIT_DURATION_IN_OPEN_STATE;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhixinzxliu
 */
public class CircuitBreakerStrategy implements Serializable {

    /**
     * 熔断策略作用API
     * 只有在有API级别或者ALL级别时需要用的该field
     */
    private List<CircuitBreakerApi> apiList;
    /**
     * 滚动窗口统计时间
     */
    private int slidingWindowSize = 10;
    /**
     * 最少请求数
     */
    private int minimumNumberOfCalls = 10;
    /**
     * 失败请求比例
     */
    private int failureRateThreshold = 50;
    /**
     * 熔断开启到半开间隔,单位s
     */
    private int waitDurationInOpenState = 60;
    /**
     * 实例级别的话需要该参数
     * 最大熔断实例的比例，超过该比例后则不进行熔断了
     */
    private int maxEjectionPercent = 50;
    /**
     * 慢请求时间阈值定义
     */
    private int slowCallDurationThreshold = 60 * 1000;
    /**
     * 慢请求熔断比例阈值
     */
    private int slowCallRateThreshold = 50;

    /**
     * 空构造函数
     */
    public CircuitBreakerStrategy() {
    }

    public int getSlowCallDurationThreshold() {
        return slowCallDurationThreshold;
    }

    public void setSlowCallDurationThreshold(int slowCallDurationThreshold) {
        this.slowCallDurationThreshold = slowCallDurationThreshold;
    }

    public int getSlowCallRateThreshold() {
        return slowCallRateThreshold;
    }

    public void setSlowCallRateThreshold(int slowCallRateThreshold) {
        this.slowCallRateThreshold = slowCallRateThreshold;
    }

    public int getMaxEjectionPercent() {
        return maxEjectionPercent;
    }

    public void setMaxEjectionPercent(int maxEjectionPercent) {
        this.maxEjectionPercent = maxEjectionPercent;
    }

    public List<CircuitBreakerApi> getApiList() {
        return apiList;
    }

    public void setApiList(List<CircuitBreakerApi> apiList) {
        this.apiList = apiList;
    }

    public int getSlidingWindowSize() {
        return slidingWindowSize;
    }

    public void setSlidingWindowSize(int slidingWindowSize) {
        this.slidingWindowSize = slidingWindowSize;
    }

    public int getMinimumNumberOfCalls() {
        return minimumNumberOfCalls;
    }

    public void setMinimumNumberOfCalls(int minimumNumberOfCalls) {
        this.minimumNumberOfCalls = minimumNumberOfCalls;
    }

    public int getFailureRateThreshold() {
        return failureRateThreshold;
    }

    public void setFailureRateThreshold(int failureRateThreshold) {
        this.failureRateThreshold = failureRateThreshold;
    }

    public int getWaitDurationInOpenState() {
        return waitDurationInOpenState;
    }

    public void setWaitDurationInOpenState(int waitDurationInOpenState) {
        this.waitDurationInOpenState = waitDurationInOpenState;
    }

    public boolean validate() {
        if (slidingWindowSize > MAX_SLIDING_WINDOW_SIZE || slidingWindowSize < MIN_SLIDING_WINDOW_SIZE) {
            return false;
        }

        if (minimumNumberOfCalls < MINIMUN_NUMBER_OF_CALLS) {
            return false;
        }

        if (failureRateThreshold > MAX_FAILURE_RATE_THRESHOLD || failureRateThreshold < MIN_FAILURE_RATE_THRESHOLD) {
            return false;
        }

        if (slowCallRateThreshold > MAX_FAILURE_RATE_THRESHOLD || slowCallRateThreshold < MIN_FAILURE_RATE_THRESHOLD) {
            return false;
        }

        if (slowCallDurationThreshold < 1) {
            return false;
        }

        if (waitDurationInOpenState > MAX_WAIT_DURATION_IN_OPEN_STATE
                || waitDurationInOpenState < MIN_WAIT_DURATION_IN_OPEN_STATE) {
            return false;
        }

        if (maxEjectionPercent > MAX_EJECTION_RATE_THRESHOLD || maxEjectionPercent < MIN_EJECTION_RATE_THRESHOLD) {
            return false;
        }

        if (apiList != null) {
            for (CircuitBreakerApi circuitBreakerApi : apiList) {
                if (!circuitBreakerApi.validate()) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return "CircuitBreakerStrategy{" +
                "apiList=" + apiList +
                ", slidingWindowSize=" + slidingWindowSize +
                ", minimumNumberOfCalls=" + minimumNumberOfCalls +
                ", failureRateThreshold=" + failureRateThreshold +
                ", waitDurationInOpenState=" + waitDurationInOpenState +
                ", maxEjectionPercent=" + maxEjectionPercent +
                ", slowCallDurationThreshold=" + slowCallDurationThreshold +
                ", slowCallRateThreshold=" + slowCallRateThreshold +
                '}';
    }
}
