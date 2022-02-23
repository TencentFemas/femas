package com.tencent.tsf.femas.adaptor.paas.governance.lane;

import java.sql.Timestamp;

/**
 * 泳道规则标签
 *
 * User: MackZhang
 * Date: 2020/1/15
 */
public class LaneRuleTag {

    private String tagId;

    private String tagName;

    private String tagOperator;

    private String tagValue;

    private String laneRuleId;

    /**
     * 规则创建时间
     */
    private Timestamp createTime;

    /**
     * 规则更新时间
     */
    private Timestamp updateTime;

    public String getTagId() {
        return tagId;
    }

    public void setTagId(final String tagId) {
        this.tagId = tagId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(final String tagName) {
        this.tagName = tagName;
    }

    public String getTagOperator() {
        return tagOperator;
    }

    public void setTagOperator(final String tagOperator) {
        this.tagOperator = tagOperator;
    }

    public String getTagValue() {
        return tagValue;
    }

    public void setTagValue(final String tagValue) {
        this.tagValue = tagValue;
    }

    public String getLaneRuleId() {
        return laneRuleId;
    }

    public void setLaneRuleId(final String laneRuleId) {
        this.laneRuleId = laneRuleId;
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
}
