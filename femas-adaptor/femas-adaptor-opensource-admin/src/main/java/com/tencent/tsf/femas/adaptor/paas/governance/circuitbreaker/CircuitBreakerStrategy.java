package com.tencent.tsf.femas.adaptor.paas.governance.circuitbreaker;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhixinzxliu
 */
public class CircuitBreakerStrategy implements Serializable {

    /**
     * 熔断策略ID
     */
    private String strategyId;
    /**
     * 熔断策略所属熔断规则ID
     */
    private String ruleId;
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

    public String getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
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

    @Override
    public String toString() {
        return "CircuitBreakerStrategy{"
                + "strategyId='" + strategyId + '\''
                + ", ruleId='" + ruleId + '\''
                + ", apiList=" + apiList
                + ", slidingWindowSize=" + slidingWindowSize
                + ", minimumNumberOfCalls=" + minimumNumberOfCalls
                + ", failureRateThreshold=" + failureRateThreshold
                + ", waitDurationInOpenState=" + waitDurationInOpenState
                + ", maxEjectionPercent=" + maxEjectionPercent
                + ", slowCallDurationThreshold=" + slowCallDurationThreshold
                + ", slowCallRateThreshold=" + slowCallRateThreshold
                + '}';
    }
}
