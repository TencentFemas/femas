package com.tencent.tsf.femas.entity.pass.route;

import java.io.Serializable;

/**
 * TSF 路由规则项匹配条件实体
 *
 * @author jingerzhang
 */
public class RouteTag implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 2327676288471562419L;
    /**
     * 路由规则项TAG匹配项Id
     */
    private String tagId;
    /**
     * 标签类型，系统标签或自定义标签
     */
    private String tagType;
    /**
     * 标签字段名称
     */
    private String tagField;
    /**
     * 标签匹配规则，等于、不等于、包含、不包含、正则
     */
    private String tagOperator;
    /**
     * 标签取值
     */
    private String tagValue;
    /**
     * 匹配项所述路由规则项Id
     */
    private String routeRuleId;

    /**
     * 空构造函数
     */
    public RouteTag() {
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
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

    public String getRouteRuleId() {
        return routeRuleId;
    }

    public void setRouteRuleId(String routeRuleId) {
        this.routeRuleId = routeRuleId;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RouteTag{");
        sb.append("tagId='").append(tagId).append('\'');
        sb.append(", tagType='").append(tagType).append('\'');
        sb.append(", tagField='").append(tagField).append('\'');
        sb.append(", tagOperator='").append(tagOperator).append('\'');
        sb.append(", tagValue='").append(tagValue).append('\'');
        sb.append(", routeRuleId='").append(routeRuleId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
