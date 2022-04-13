package com.tencent.tsf.femas.entity.rule;

import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.entity.pass.circuitbreaker.CircuitBreakerRule;
import com.tencent.tsf.femas.entity.pass.circuitbreaker.CircuitBreakerStrategy;
import com.tencent.tsf.femas.entity.pass.circuitbreaker.FemasCircuitBreakerIsolationLevelEnum;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FemasCircuitBreakerRule {

    @ApiModelProperty("规则id")
    private String ruleId;

    @ApiModelProperty("命名空间")
    private String namespaceId;

    @ApiModelProperty("服务名")
    private String serviceName;

    @ApiModelProperty("下游命名空间")
    private String targetNamespaceId;

    @ApiModelProperty("下游服务名")
    private String targetServiceName;

    @ApiModelProperty("规则名称")
    private String ruleName;

    @ApiModelProperty("隔离级别")
    private String isolationLevel;

    @ApiModelProperty("熔断策略")
    private List<FemasCircuitBreakerStrategy> strategy;


    @ApiModelProperty("是否开启 ：1开启；0：关闭")
    private String isEnable;

    @ApiModelProperty("生效时间")
    private Long updateTime;

    @ApiModelProperty("描述")
    private String desc;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getIsEnable() {
        return isEnable;
    }

    public void setIsEnable(String isEnable) {
        this.isEnable = isEnable;
    }

    public boolean judgeStatus() {
        if (StringUtils.isEmpty(isEnable)) {
            return false;
        }
        return isEnable.equals("1");
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getIsolationLevel() {
        return isolationLevel;
    }

    public void setIsolationLevel(String isolationLevel) {
        this.isolationLevel = isolationLevel;
    }

    public List<FemasCircuitBreakerStrategy> getStrategy() {
        return strategy;
    }

    public void setStrategy(List<FemasCircuitBreakerStrategy> strategy) {
        this.strategy = strategy;
    }

    public String getTargetNamespaceId() {
        return targetNamespaceId;
    }

    public void setTargetNamespaceId(String targetNamespaceId) {
        this.targetNamespaceId = targetNamespaceId;
    }

    public String getTargetServiceName() {
        return targetServiceName;
    }

    public void setTargetServiceName(String targetServiceName) {
        this.targetServiceName = targetServiceName;
    }

    @Override
    public int hashCode() {
        return ruleId.hashCode();
    }

    public CircuitBreakerRule toPassRule() {
        CircuitBreakerRule passRule = new CircuitBreakerRule();
        passRule.setRuleId(this.ruleId);
        passRule.setRuleName(this.ruleName);
        passRule.setNamespaceId(this.namespaceId);
        passRule.setServiceName(this.serviceName);
        passRule.setIsolationLevel(FemasCircuitBreakerIsolationLevelEnum.valueOf(this.isolationLevel));
        passRule.setTargetNamespaceId(this.targetNamespaceId);
        passRule.setTargetServiceName(this.targetServiceName);
        passRule.setUpdateTime(new Date(this.updateTime).toString());
        ArrayList<CircuitBreakerStrategy> strategies = new ArrayList<>();
        if (!CollectionUtil.isEmpty(this.strategy)) {
            strategy.stream().forEach(s -> {
                CircuitBreakerStrategy circuitBreakerStrategy = s.toPassRule();
                circuitBreakerStrategy.setRuleId(this.ruleId);
                strategies.add(circuitBreakerStrategy);
            });
        }
        passRule.setStrategyList(strategies);
        return passRule;
    }


    @Override
    public String toString() {
        return "命名空间：" + namespaceId + "，服务：" + serviceName + "，下游服务：" + targetServiceName;
    }
}
