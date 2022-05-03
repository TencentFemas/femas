package com.tencent.tsf.femas.entity.pass.route;

import java.io.Serializable;

/**
 * TSF 路由规则路由目标匹配项实体
 *
 * @author jingerzhang
 */
public class RouteDestItem implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -2276599402100262473L;
    /**
     * 路由规则路由目标匹配项ID
     */
    private String routeDestItemId;
    /**
     * 所属路由规则路由目标ID
     */
    private String routeDestId;
    /**
     * 路由规则目标字段名称
     */
    private String destItemField;
    /**
     * 路由规则目标字段取值
     */
    private String destItemValue;

    /**
     * 空构造函数
     */
    public RouteDestItem() {
    }

    public String getDestItemField() {
        return destItemField;
    }

    public void setDestItemField(String destItemField) {
        this.destItemField = destItemField;
    }

    public String getDestItemValue() {
        return destItemValue;
    }

    public void setDestItemValue(String destItemValue) {
        this.destItemValue = destItemValue;
    }

    public String getRouteDestItemId() {
        return routeDestItemId;
    }

    public void setRouteDestItemId(String routeDestItemId) {
        this.routeDestItemId = routeDestItemId;
    }

    public String getRouteDestId() {
        return routeDestId;
    }

    public void setRouteDestId(String routeDestId) {
        this.routeDestId = routeDestId;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RouteDestItem{");
        sb.append("routeDestItemId='").append(routeDestItemId).append('\'');
        sb.append(", routeDestId='").append(routeDestId).append('\'');
        sb.append(", destItemField='").append(destItemField).append('\'');
        sb.append(", destItemValue='").append(destItemValue).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
