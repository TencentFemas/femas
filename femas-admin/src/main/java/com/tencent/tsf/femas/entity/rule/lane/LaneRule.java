package com.tencent.tsf.femas.entity.rule.lane;


import com.tencent.tsf.femas.entity.Page;

import java.util.List;


/**
 * @Author: cody
 * @Date: 2022/7/26
 * @Descriptioin
 */
public class LaneRule extends Page {

    /**
     * 泳道规则id
     */
    private String ruleId;

    /**
     * 泳道规则名称
     */
    private String ruleName;

    /**
     * 备注
     */
    private String remark;

    /**
     * tag 列表
     */
    private List<LaneRuleTag> ruleTagList;

    /**
     * tag聚合关系
     */
    private RuleTagRelationship ruleTagRelationship;

    /**
     * 关联泳道id
     */
    private String laneId;

    /**
     * 是否开启 1：开启 0：关闭
     */
    private Integer enable;

    /**
     * 规则创建时间
     */
    private Long createTime = System.currentTimeMillis();

    /**
     * 规则更新时间
     */
    private Long updateTime = System.currentTimeMillis();

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<LaneRuleTag> getRuleTagList() {
        return ruleTagList;
    }

    public void setRuleTagList(List<LaneRuleTag> ruleTagList) {
        this.ruleTagList = ruleTagList;
    }

    public RuleTagRelationship getRuleTagRelationship() {
        return ruleTagRelationship;
    }

    public void setRuleTagRelationship(RuleTagRelationship ruleTagRelationship) {
        this.ruleTagRelationship = ruleTagRelationship;
    }

    public String getLaneId() {
        return laneId;
    }

    public void setLaneId(String laneId) {
        this.laneId = laneId;
    }

    public boolean isEnable() {
        return enable == 1;
    }

    public Integer getEnable() {
        return enable;
    }

    public void setEnable(Integer enable) {
        this.enable = enable;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
}
