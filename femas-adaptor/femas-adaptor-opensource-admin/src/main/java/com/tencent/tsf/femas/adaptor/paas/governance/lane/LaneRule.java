package com.tencent.tsf.femas.adaptor.paas.governance.lane;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

/**
 * User: MackZhang
 * Date: 2020/1/14
 */
public class LaneRule {

    private String ruleId;

    private String ruleName;

    /**
     * 越小优先级越高
     */
    private Long priority;

    private String remark;

    private List<LaneRuleTag> ruleTagList;

    private RuleTagRelationship ruleTagRelationship;

    /**
     * 关联泳道
     */
    private HashMap<String, Integer> relativeLane;

    /**
     * 灰度类型 蓝绿：tag  金丝雀：canary
     */
    private String grayType;

    private String laneId;

    private boolean enable;

    /**
     * 规则创建时间
     */
    private Timestamp createTime;

    /**
     * 规则更新时间
     */
    private Timestamp updateTime;

    public HashMap<String, Integer> getRelativeLane() {
        return relativeLane;
    }

    public void setRelativeLane(HashMap<String, Integer> relativeLane) {
        this.relativeLane = relativeLane;
    }

    public String getGrayType() {
        return grayType;
    }

    public void setGrayType(String grayType) {
        this.grayType = grayType;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(final String ruleId) {
        this.ruleId = ruleId;
    }

    public Long getPriority() {
        return priority;
    }

    public void setPriority(final Long priority) {
        this.priority = priority;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(final String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(final String remark) {
        this.remark = remark;
    }

    public List<LaneRuleTag> getRuleTagList() {
        return ruleTagList;
    }

    public void setRuleTagList(final List<LaneRuleTag> ruleTagList) {
        this.ruleTagList = ruleTagList;
    }

    public RuleTagRelationship getRuleTagRelationship() {
        return ruleTagRelationship;
    }

    public void setRuleTagRelationship(final RuleTagRelationship ruleTagRelationship) {
        this.ruleTagRelationship = ruleTagRelationship;
    }

    public String getLaneId() {
        return laneId;
    }

    public void setLaneId(final String laneId) {
        this.laneId = laneId;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(final Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(final Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(final boolean enable) {
        this.enable = enable;
    }
}
