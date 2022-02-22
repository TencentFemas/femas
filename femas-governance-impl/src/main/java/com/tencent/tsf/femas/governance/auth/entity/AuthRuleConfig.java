package com.tencent.tsf.femas.governance.auth.entity;

/**
 * @author Cody
 * @date 2021 2021/10/13 3:12 下午
 */
public class AuthRuleConfig {

    private String serviceName;

    private AuthRuleGroup authRuleGroup;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public AuthRuleGroup getAuthRuleGroup() {
        return authRuleGroup;
    }

    public void setAuthRuleGroup(AuthRuleGroup authRuleGroup) {
        this.authRuleGroup = authRuleGroup;
    }
}
