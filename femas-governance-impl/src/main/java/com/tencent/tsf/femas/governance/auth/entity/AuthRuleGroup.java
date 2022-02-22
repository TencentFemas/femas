package com.tencent.tsf.femas.governance.auth.entity;

import com.tencent.tsf.femas.common.tag.TagRule;
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
    private List<TagRule> rules;

    /**
     * 鉴权类型，黑名单/白名单
     */
    private String type;

    public List<TagRule> getRules() {
        return rules;
    }

    public void setRules(List<TagRule> rules) {
        this.rules = rules;
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
        sb.append(", type='").append(type).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
