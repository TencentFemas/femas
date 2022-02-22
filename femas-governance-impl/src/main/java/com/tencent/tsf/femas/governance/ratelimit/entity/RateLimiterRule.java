package com.tencent.tsf.femas.governance.ratelimit.entity;

import com.tencent.tsf.femas.common.tag.TagRule;
import com.tencent.tsf.femas.governance.ratelimit.constant.RateLimiterConstant;
import java.io.Serializable;
import java.util.Objects;

public class RateLimiterRule implements Serializable {

    private String ruleId;

    private TagRule tagRule;

    private int limit;
    private int duration;

    private int rateLimiterType = RateLimiterConstant.RATE_LIMITER_TYPE_QPS;

    private int warmUpPeriodSec = 10;
    /**
     * Max queueing time in rate limiter behavior.
     */
    private int maxQueueingTimeMs = 500;

    public int getRateLimiterType() {
        return rateLimiterType;
    }

    public TagRule getTagRule() {
        return tagRule;
    }

    public void setTagRule(TagRule tagRule) {
        this.tagRule = tagRule;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RateLimiterRule)) {
            return false;
        }
        RateLimiterRule that = (RateLimiterRule) o;
        return ruleId.equals(that.ruleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleId);
    }


}
