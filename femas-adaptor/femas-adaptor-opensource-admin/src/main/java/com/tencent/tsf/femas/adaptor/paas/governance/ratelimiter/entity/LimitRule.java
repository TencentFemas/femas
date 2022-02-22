package com.tencent.tsf.femas.adaptor.paas.governance.ratelimiter.entity;

import com.tencent.tsf.femas.common.tag.TagRule;
import java.io.Serializable;
import java.util.Objects;

public class LimitRule implements Serializable {

    /**
     * 规则id
     */
    private String ruleId;

    /**
     * 周期长度，以秒为单位
     */
    private int duration;

    /**
     * 服务总的配额
     */
    private int totalQuota;

    /**
     * 名称空间id
     */
    private String namespaceId;

    /**
     * 服务名
     */
    private String serviceName;

    /**
     * 分配给当前实例的配额，null 时表示仍未拿到配额
     */
    private Integer instanceQuota;


    /**
     * 限流类型：GLOBAL, SOURCE_SERVICE, TAG_CONDITION
     */
    private Type type;

    /**
     * 规则标签
     */
    private TagRule tagRule;

    /**
     * 规则名称
     */
    private String ruleName;

    public LimitRule() {
    }

    public LimitRule(String ruleId, int duration, int totalQuota, Integer instanceQuota, Type type, TagRule tagRule) {
        this.ruleId = ruleId;
        this.duration = duration;
        this.totalQuota = totalQuota;
        this.instanceQuota = instanceQuota;
        this.type = type;
        this.tagRule = tagRule;
    }

    public LimitRule(LimitRule other) {
        this.ruleId = other.ruleId;
        this.duration = other.duration;
        this.totalQuota = other.totalQuota;
        this.instanceQuota = other.instanceQuota;
        this.type = other.type;
        this.tagRule = other.tagRule;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LimitRule limitRule = (LimitRule) o;
        return duration == limitRule.duration && totalQuota == limitRule.totalQuota
                && instanceQuota == limitRule.instanceQuota
                && Objects.equals(ruleId, limitRule.ruleId) && type == limitRule.type
                && (tagRule == limitRule.tagRule || Objects.equals(tagRule, limitRule.tagRule));
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleId, duration, totalQuota, instanceQuota, type, tagRule);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
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

    public int getTotalQuota() {
        return totalQuota;
    }

    public void setTotalQuota(int totalQuota) {
        this.totalQuota = totalQuota;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Integer getInstanceQuota() {
        return instanceQuota;
    }

    public void setInstanceQuota(Integer instanceQuota) {
        this.instanceQuota = instanceQuota;
    }

    public boolean isSameDuration(LimitRule other) {
        return this.duration == other.duration;
    }

    public boolean isSameTagRule(LimitRule other) {
        if (this.tagRule != other.tagRule) {
            if (this.tagRule == null) {
                return false;
            }
            if (this.tagRule.getTags().size() != other.tagRule.getTags().size()) {
                return false;
            }
            for (int i = 0; i < this.tagRule.getTags().size(); i++) {
                if (!this.tagRule.getTags().get(i).equals(other.tagRule.getTags().get(i))) {
                    return false;
                }
            }
        }
        return true;
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

    @Override
    public String toString() {
        return "命名空间：" + namespaceId + "，服务：" + serviceName + "，规则：" + ruleName;
    }

    public enum Type {
        GLOBAL, SOURCE_SERVICE, TAG_CONDITION
    }
}
