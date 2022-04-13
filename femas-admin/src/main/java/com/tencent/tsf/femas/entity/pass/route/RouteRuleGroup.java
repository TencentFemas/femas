package com.tencent.tsf.femas.entity.pass.route;

import java.io.Serializable;
import java.util.List;

/**
 * TSF 路由规则实体
 *
 * @author jingerzhang
 */
public class RouteRuleGroup implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -3066353351857943148L;
    /**
     * 路由规则主键，全局唯一，前缀route
     */
    private String routeId;
    /**
     * 路由规则名称
     */
    private String routeName;
    /**
     * 路由规则描述
     */
    private String routeDesc;
    /**
     * 路由规则微服务ID
     */
    private String microserviceId;
    /**
     * TAG 路由规则详情
     */
    private List<RouteRule> ruleList;
    /**
     * microserviceId 微服务所属命名空间id
     */
    private String namespaceId;
    /**
     * microserviceId 微服务 服务名称
     */
    private String microserviceName;
    /**
     * 是否开启路由规则保护策略
     */
    private Boolean fallbackStatus;

    /**
     * 空构造函数
     */
    public RouteRuleGroup() {
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getRouteDesc() {
        return routeDesc;
    }

    public void setRouteDesc(String routeDesc) {
        this.routeDesc = routeDesc;
    }

    public String getMicroserviceId() {
        return microserviceId;
    }

    public void setMicroserviceId(String microserviceId) {
        this.microserviceId = microserviceId;
    }

    public List<RouteRule> getRuleList() {
        return ruleList;
    }

    public void setRuleList(List<RouteRule> ruleList) {
        this.ruleList = ruleList;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getMicroserviceName() {
        return microserviceName;
    }

    public void setMicroserviceName(String microserviceName) {
        this.microserviceName = microserviceName;
    }

    public Boolean getFallbackStatus() {
        return fallbackStatus;
    }

    public void setFallbackStatus(Boolean fallbackStatus) {
        this.fallbackStatus = fallbackStatus;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RouteRuleGroup{");
        sb.append("routeId='").append(routeId).append('\'');
        sb.append(", routeName='").append(routeName).append('\'');
        sb.append(", routeDesc='").append(routeDesc).append('\'');
        sb.append(", microserviceId='").append(microserviceId).append('\'');
        sb.append(", ruleList=").append(ruleList);
        sb.append(", namespaceId='").append(namespaceId).append('\'');
        sb.append(", microserviceName='").append(microserviceName).append('\'');
        sb.append(", fallbackStatus=").append(fallbackStatus);
        sb.append('}');
        return sb.toString();
    }
}
