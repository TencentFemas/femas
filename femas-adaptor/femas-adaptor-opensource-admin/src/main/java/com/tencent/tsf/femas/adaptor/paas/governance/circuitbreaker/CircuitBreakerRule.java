package com.tencent.tsf.femas.adaptor.paas.governance.circuitbreaker;


import com.tencent.tsf.femas.governance.circuitbreaker.FemasCircuitBreakerIsolationLevelEnum;
import java.io.Serializable;
import java.util.List;

/**
 * @author zhixinzxliu
 */
public class CircuitBreakerRule implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 熔断规则主键，全局唯一
     */
    private String ruleId;

    /**
     * 熔断规则微服务ID
     */
    private String serviceName;

    /**
     * TAG 熔断规则详情
     */
    private List<CircuitBreakerStrategy> strategyList;

    /**
     * 微服务所属命名空间id
     */
    private String namespaceId;

    private String updateTime;

    private String targetServiceName;

    private String targetNamespaceId;

    private FemasCircuitBreakerIsolationLevelEnum isolationLevel;

    private String ruleName;

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public FemasCircuitBreakerIsolationLevelEnum getIsolationLevel() {
        return isolationLevel;
    }

    public void setIsolationLevel(FemasCircuitBreakerIsolationLevelEnum isolationLevel) {
        this.isolationLevel = isolationLevel;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getTargetServiceName() {
        return targetServiceName;
    }

    public void setTargetServiceName(String targetServiceName) {
        this.targetServiceName = targetServiceName;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<CircuitBreakerStrategy> getStrategyList() {
        return strategyList;
    }

    public void setStrategyList(List<CircuitBreakerStrategy> strategyList) {
        this.strategyList = strategyList;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }


    public String getTargetNamespaceId() {
        return targetNamespaceId;
    }

    public void setTargetNamespaceId(String targetNamespaceId) {
        this.targetNamespaceId = targetNamespaceId;
    }

    @Override
    public String toString() {
        return "CircuitBreakerRule{"
                + "ruleId='" + ruleId + '\''
                + ", serviceName='" + serviceName + '\''
                + ", strategyList=" + strategyList
                + ", namespaceId='" + namespaceId + '\''
                + ", updateTime='" + updateTime + '\''
                + ", targetServiceName='" + targetServiceName + '\''
                + ", targetNamespaceId='" + targetNamespaceId + '\''
                + ", isolationLevel=" + isolationLevel
                + ", ruleName='" + ruleName + '\''
                + '}';
    }
}
