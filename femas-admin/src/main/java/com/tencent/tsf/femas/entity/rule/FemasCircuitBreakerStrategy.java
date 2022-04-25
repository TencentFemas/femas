package com.tencent.tsf.femas.entity.rule;

import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.entity.pass.circuitbreaker.CircuitBreakerApi;
import com.tencent.tsf.femas.entity.pass.circuitbreaker.CircuitBreakerStrategy;
import com.tencent.tsf.femas.entity.registry.ServiceApi;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;

public class FemasCircuitBreakerStrategy {


    @ApiModelProperty("api")
    private List<ServiceApi> api;

    @ApiModelProperty("滑动时间窗口")
    private int slidingWindowSize;

    @ApiModelProperty("最少请求数")
    private int minimumNumberOfCalls;

    @ApiModelProperty("失败请求率")
    private int failureRateThreshold;

    @ApiModelProperty("慢请求时间阈值")
    private int slowCallDurationThreshold;

    @ApiModelProperty("慢请求比例")
    private int slowCallRateThreshold;

    @ApiModelProperty("最大熔断比率")
    private int maxEjectionPercent;

    @ApiModelProperty("半开时间")
    private int waitDurationInOpenState;

    public List<ServiceApi> getApi() {
        return api;
    }

    public void setApi(List<ServiceApi> api) {
        this.api = api;
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

    public int getWaitDurationInOpenState() {
        return waitDurationInOpenState;
    }

    public void setWaitDurationInOpenState(int waitDurationInOpenState) {
        this.waitDurationInOpenState = waitDurationInOpenState;
    }

    public CircuitBreakerStrategy toPassRule() {
        CircuitBreakerStrategy passRule = new CircuitBreakerStrategy();
        passRule.setFailureRateThreshold(this.failureRateThreshold);
        passRule.setMaxEjectionPercent(this.maxEjectionPercent);
        passRule.setMinimumNumberOfCalls(this.minimumNumberOfCalls);
        passRule.setSlowCallRateThreshold(this.slowCallRateThreshold);
        passRule.setWaitDurationInOpenState(this.waitDurationInOpenState);
        passRule.setSlidingWindowSize(this.slidingWindowSize);
        passRule.setMaxEjectionPercent(this.maxEjectionPercent);
        passRule.setSlowCallDurationThreshold(this.getSlowCallDurationThreshold());
        ArrayList<CircuitBreakerApi> circuitBreakerApis = null;
        if (!CollectionUtil.isEmpty(this.api)) {
            circuitBreakerApis = new ArrayList<>();
            for (ServiceApi s : this.api) {
                CircuitBreakerApi circuitBreakerApi = new CircuitBreakerApi();
                circuitBreakerApi.setMethod(s.getMethod());
                circuitBreakerApi.setPath(s.getPath());
                circuitBreakerApis.add(circuitBreakerApi);
            }
        }
        passRule.setApiList(circuitBreakerApis);
        return passRule;
    }


    @Override
    public String toString() {
        return "FemasCircuitBreakerStrategy{" +
                "api='" + api + '\'' +
                ", slidingWindowSize=" + slidingWindowSize +
                ", minimumNumberOfCalls=" + minimumNumberOfCalls +
                ", failureRateThreshold=" + failureRateThreshold +
                ", slowCallDurationThreshold=" + slowCallDurationThreshold +
                ", slowCallRateThreshold=" + slowCallRateThreshold +
                ", maxEjectionPercent=" + maxEjectionPercent +
                ", waitDurationInOpenState=" + waitDurationInOpenState +
                '}';
    }
}
