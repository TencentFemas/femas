package com.tencent.tsf.femas.common.tag;

import java.util.List;

/**
 * 标签规则
 */
public class TagRule {

    /**
     * 规则ID
     */
    private String id;

    /**
     * 规则名
     */
    private String name;

    /**
     * 规则引用的标签列表
     */
    private List<Tag> tags;

    /**
     * 规则运算表达式
     *
     * 默认是与逻辑
     */
    private String expression = TagExpression.RELATION_AND;


    @Override
    public String toString() {
        return "TagRule{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", conditions=" + tags +
                ", expression='" + expression + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
