package com.tencent.tsf.femas.entity.rule.lane;


/**
 * @Author: cody
 * @Date: 2022/7/26
 * @Descriptioin
 */
public class LaneRuleTag {

    private String tagName;

    private String tagOperator;

    private String tagValue;


    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagOperator() {
        return tagOperator;
    }

    public void setTagOperator(String tagOperator) {
        this.tagOperator = tagOperator;
    }

    public String getTagValue() {
        return tagValue;
    }

    public void setTagValue(String tagValue) {
        this.tagValue = tagValue;
    }
}
