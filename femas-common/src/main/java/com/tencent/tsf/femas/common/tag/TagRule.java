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

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof TagRule)) {
            return false;
        } else {
            TagRule other = (TagRule)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                label59: {
                    Object this$id = this.getId();
                    Object other$id = other.getId();
                    if (this$id == null) {
                        if (other$id == null) {
                            break label59;
                        }
                    } else if (this$id.equals(other$id)) {
                        break label59;
                    }

                    return false;
                }

                Object this$name = this.getName();
                Object other$name = other.getName();
                if (this$name == null) {
                    if (other$name != null) {
                        return false;
                    }
                } else if (!this$name.equals(other$name)) {
                    return false;
                }

                Object this$tags = this.getTags();
                Object other$tags = other.getTags();
                if (this$tags == null) {
                    if (other$tags != null) {
                        return false;
                    }
                } else if (!this$tags.equals(other$tags)) {
                    return false;
                }

                Object this$expression = this.getExpression();
                Object other$expression = other.getExpression();
                if (this$expression == null) {
                    if (other$expression != null) {
                        return false;
                    }
                } else if (!this$expression.equals(other$expression)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof TagRule;
    }

    @Override
    public int hashCode() {
        int result = 1;
        Object $id = this.getId();
        result = result * 59 + ($id == null ? 43 : $id.hashCode());
        Object $name = this.getName();
        result = result * 59 + ($name == null ? 43 : $name.hashCode());
        Object $tags = this.getTags();
        result = result * 59 + ($tags == null ? 43 : $tags.hashCode());
        Object $expression = this.getExpression();
        result = result * 59 + ($expression == null ? 43 : $expression.hashCode());
        return result;
    }
}
