package com.tencent.tsf.femas.adaptor.paas.governance.router;

import java.io.Serializable;
import java.util.List;

/**
 * 路由规则目标实体
 *
 * @author jingerzhang
 */

public class RouteDest implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 984582541720418394L;
    /**
     * 路由规则项路由目标Id
     */

    private String destId;
    /**
     * 路由目标权重
     */
    private Integer destWeight;
    /**
     * 路由目标匹配条件列表
     *
     * 忽略到数据库表字段的映射
     */

    private List<RouteDestItem> destItemList;
    /**
     * 路由目标所属路由规则项Id
     */
    private String routeRuleId;

    /**
     * 空构造函数
     */
    public RouteDest() {
    }

    public String getDestId() {
        return destId;
    }

    public void setDestId(String destId) {
        this.destId = destId;
    }

    public Integer getDestWeight() {
        return destWeight;
    }

    public void setDestWeight(Integer destWeight) {
        this.destWeight = destWeight;
    }

    public List<RouteDestItem> getDestItemList() {
        return destItemList;
    }

    public void setDestItemList(List<RouteDestItem> destItemList) {
        this.destItemList = destItemList;
    }

    public String getRouteRuleId() {
        return routeRuleId;
    }

    public void setRouteRuleId(String routeRuleId) {
        this.routeRuleId = routeRuleId;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RouteDest{");
        sb.append("destId='").append(destId).append('\'');
        sb.append(", destWeight=").append(destWeight);
        sb.append(", destItemList=").append(destItemList);
        sb.append(", routeRuleId='").append(routeRuleId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
