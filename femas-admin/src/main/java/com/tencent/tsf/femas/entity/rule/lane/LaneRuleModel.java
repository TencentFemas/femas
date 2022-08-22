package com.tencent.tsf.femas.entity.rule.lane;

import com.tencent.tsf.femas.entity.Page;

/**
 * @Author: cody
 * @Date: 2022/7/28
 * @Descriptioin
 */
public class LaneRuleModel extends Page {

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
}
