package com.tencent.tsf.femas.governance.route.entity;

import com.tencent.tsf.femas.common.rule.Rule;
import java.io.Serializable;
import java.util.List;

/**
 * 路由规则集合
 * 一个微服务可以设置一组路由规则
 */
public class RouteRuleGroup implements Rule, Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -3066353351857943148L;
    /**
     * 路由规则名称
     */
    private String routeName;
    /**
     * 路由规则列表
     */
    private List<RouteRule> ruleList;
    /**
     * serviceId 微服务所属命名空间id
     */
    private String namespace;
    /**
     * serviceId 微服务 服务名称
     */
    private String serviceName;
    /**
     * 是否开启路由规则保护策略
     */
    private Boolean fallback = false;
    /**
     * 规则版本
     */
    private String version;

    /**
     * 空构造函数
     */
    public RouteRuleGroup() {
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public List<RouteRule> getRuleList() {
        return ruleList;
    }

    public void setRuleList(List<RouteRule> ruleList) {
        this.ruleList = ruleList;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Boolean getFallback() {
        return fallback;
    }

    public void setFallback(Boolean fallback) {
        this.fallback = fallback;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "RouteRuleGroup{" +
                "routeName='" + routeName + '\'' +
                ", ruleList=" + ruleList +
                ", namespace='" + namespace + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", fallback=" + fallback +
                ", version='" + version + '\'' +
                '}';
    }
}
