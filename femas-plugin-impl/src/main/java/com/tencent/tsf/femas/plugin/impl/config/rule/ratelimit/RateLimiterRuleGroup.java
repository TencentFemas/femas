package com.tencent.tsf.femas.plugin.impl.config.rule.ratelimit;

import java.io.Serializable;
import java.util.List;

public class RateLimiterRuleGroup implements Serializable {

    /**
     * 规则列表
     */
    private List<RateLimiterRule> rules;
}
