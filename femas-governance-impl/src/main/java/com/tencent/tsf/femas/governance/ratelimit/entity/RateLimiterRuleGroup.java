package com.tencent.tsf.femas.governance.ratelimit.entity;

import java.io.Serializable;
import java.util.List;

public class RateLimiterRuleGroup implements Serializable {

    /**
     * 规则列表
     */
    private List<RateLimiterRule> rules;
}
