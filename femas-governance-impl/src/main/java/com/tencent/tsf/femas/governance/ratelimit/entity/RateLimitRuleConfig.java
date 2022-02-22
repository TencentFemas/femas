package com.tencent.tsf.femas.governance.ratelimit.entity;

import java.util.List;

/**
 * @author Cody
 * @date 2021 2021/10/14 10:35 上午
 */
public class RateLimitRuleConfig {

    private List<InitLimitRule> limitRuleGroup;

    private String serviceName;

    public List<InitLimitRule> getLimitRuleGroup() {
        return limitRuleGroup;
    }

    public void setLimitRuleGroup(List<InitLimitRule> limitRuleGroup) {
        this.limitRuleGroup = limitRuleGroup;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
