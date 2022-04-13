package com.tencent.tsf.femas.entity.pass.auth;

import java.io.Serializable;
import java.util.List;

public class AuthRuleGroup implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1643183648126834802L;
    /**
     * 规则列表
     */
    private List<AuthRule> rules;
    /**
     * 规则计算规则
     */
    private String ruleProgram;

    /**
     * 鉴权类型
     */
    private String type;

    public List<AuthRule> getRules() {
        return rules;
    }

    public void setRules(List<AuthRule> rules) {
        this.rules = rules;
    }

    public String getRuleProgram() {
        return ruleProgram;
    }

    public void setRuleProgram(String ruleProgram) {
        this.ruleProgram = ruleProgram;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("AuthRuleGroup{");
        sb.append("rules=").append(rules);
        sb.append(", ruleProgram='").append(ruleProgram).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
