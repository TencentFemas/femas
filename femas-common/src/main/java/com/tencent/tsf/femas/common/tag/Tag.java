package com.tencent.tsf.femas.common.tag;

import java.util.Objects;

/**
 * 标签规则实体
 */
public class Tag {

    /**
     * 标签类型
     * 定义在TagConstant.TYPE
     */
    private String tagType;

    /**
     * 标签名
     */
    private String tagField;

    /**
     * 标签运算符
     * 定义在TagConstant.OPERATOR
     */
    private String tagOperator;

    /**
     * 标签的被运算对象值
     */
    private String tagValue;

    @Override
    public String toString() {
        return "Tag{" +
                "tagType='" + tagType + '\'' +
                ", tagField='" + tagField + '\'' +
                ", tagOperator='" + tagOperator + '\'' +
                ", tagValue='" + tagValue + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Tag)) {
            return false;
        }
        Tag tag = (Tag) o;
        return tagType.equals(tag.tagType) &&
                tagField.equals(tag.tagField) &&
                tagOperator.equals(tag.tagOperator) &&
                tagValue.equals(tag.tagValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagType, tagField, tagOperator, tagValue);
    }

    public String getTagType() {
        return tagType;
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }

    public String getTagField() {
        return tagField;
    }

    public void setTagField(String tagField) {
        this.tagField = tagField;
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
