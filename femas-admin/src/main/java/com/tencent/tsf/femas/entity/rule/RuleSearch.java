package com.tencent.tsf.femas.entity.rule;

import io.swagger.annotations.ApiModelProperty;

public class RuleSearch {

    @ApiModelProperty("命名空间id")
    private String namespaceId;

    @ApiModelProperty("服务名")
    private String serviceName;

    @ApiModelProperty("规则id")
    private String ruleId;

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

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }
}
