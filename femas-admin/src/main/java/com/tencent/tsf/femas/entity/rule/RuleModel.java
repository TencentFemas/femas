package com.tencent.tsf.femas.entity.rule;

import io.swagger.annotations.ApiModelProperty;

/**
 * @Auther: yrz
 * @Date: 2021/05/08/17:24
 * @Descriptioin
 */
public class RuleModel {

    @ApiModelProperty("命名空间id")
    private String namespaceId;

    @ApiModelProperty("服务名称")
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

    @Override
    public String toString() {
        return "命名空间：" + namespaceId + " 服务名：" + serviceName + "规则id：" + ruleId;
    }
}
