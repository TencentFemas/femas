package com.tencent.tsf.femas.entity.rule.auth;

/**
 * @Auther: yrz
 * @Date: 2021/05/08/19:50
 * @Descriptioin
 */
public class ServiceAuthRuleModel {

    private String namespaceId;

    private String serviceName;

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
        return "命名空间：" + namespaceId + "，服务：" + serviceName;
    }
}
