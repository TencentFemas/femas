package com.tencent.tsf.femas.plugin.impl.config.rule.circuitbreaker;

import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.governance.circuitbreaker.FemasCircuitBreakerIsolationLevelEnum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhixinzxliu
 */
public class CircuitBreakerRule implements Serializable {

    private Service targetService;
    /**
     * TAG 熔断规则详情
     */
    private List<CircuitBreakerStrategy> strategyList;
    private FemasCircuitBreakerIsolationLevelEnum isolationLevel;

    /**
     * 空构造函数
     */
    public CircuitBreakerRule() {
    }

    public FemasCircuitBreakerIsolationLevelEnum getIsolationLevel() {
        return isolationLevel;
    }

    public void setIsolationLevel(FemasCircuitBreakerIsolationLevelEnum isolationLevel) {
        this.isolationLevel = isolationLevel;
    }

    public Service getTargetService() {
        return targetService;
    }

    public void setTargetService(Service targetService) {
        this.targetService = targetService;
    }

    public List<CircuitBreakerStrategy> getStrategyList() {
        return strategyList;
    }

    public void setStrategyList(List<CircuitBreakerStrategy> strategyList) {
        this.strategyList = strategyList;
    }

    public boolean validate() {
        if (isolationLevel == null) {
            return false;
        }

        if (targetService == null) {
            return false;
        }

        if (FemasCircuitBreakerIsolationLevelEnum.INSTANCE != isolationLevel &&
                FemasCircuitBreakerIsolationLevelEnum.API != isolationLevel &&
                FemasCircuitBreakerIsolationLevelEnum.SERVICE != isolationLevel) {
            return false;
        }

        if (getStrategyList() == null || getStrategyList().size() < 1) {
            if (isolationLevel != FemasCircuitBreakerIsolationLevelEnum.API) {
                // 如果是Instance和Service级别，放入默认的strategy
                this.strategyList = new ArrayList<>();
                this.strategyList.add(new CircuitBreakerStrategy());
            } else {
                return false;
            }
        }

        // 在实例级别和服务级别，策略有且只能有一个
        if (isolationLevel == FemasCircuitBreakerIsolationLevelEnum.INSTANCE
                || isolationLevel == FemasCircuitBreakerIsolationLevelEnum.SERVICE) {
            if (getStrategyList().size() != 1 || !getStrategyList().get(0).validate()) {
                return false;
            }
        }

        if (isolationLevel == FemasCircuitBreakerIsolationLevelEnum.API) {
            for (CircuitBreakerStrategy strategy : strategyList) {
                if (strategy.getApiList() == null || strategy.getApiList().isEmpty()) {
                    return false;
                }
            }
        }

        for (CircuitBreakerStrategy strategy : getStrategyList()) {
            if (!strategy.validate()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "CircuitBreakerRule{" +
                "strategyList=" + strategyList +
                ", targetService='" + targetService + '\'' +
                ", isolationLevel=" + isolationLevel +
                '}';
    }
}
