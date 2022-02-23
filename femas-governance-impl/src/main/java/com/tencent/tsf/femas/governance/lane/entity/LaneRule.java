package com.tencent.tsf.femas.governance.lane.entity;

import com.tencent.tsf.femas.common.tag.TagRule;
import java.sql.Timestamp;
import java.util.Objects;

public class LaneRule {

    private String ruleId;

    /**
     * 越小优先级越高
     */
    private Integer priority;

    private TagRule tagRule;

    private String laneId;

    /**
     * 规则创建时间
     */
    private Timestamp createTime;

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getLaneId() {
        return laneId;
    }

    public void setLaneId(String laneId) {
        this.laneId = laneId;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(final Integer priority) {
        this.priority = priority;
    }

    public TagRule getTagRule() {
        return tagRule;
    }

    public void setTagRule(TagRule tagRule) {
        this.tagRule = tagRule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LaneRule)) {
            return false;
        }
        LaneRule laneRule = (LaneRule) o;
        return Objects.equals(ruleId, laneRule.ruleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleId);
    }
}
