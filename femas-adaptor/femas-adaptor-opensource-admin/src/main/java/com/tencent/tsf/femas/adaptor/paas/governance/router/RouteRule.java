package com.tencent.tsf.femas.adaptor.paas.governance.router;


import java.io.Serializable;
import java.util.List;

/**
 * 路由规则项实体
 *
 * @author jingerzhang
 */
public class RouteRule implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -1886125299472426511L;
    /**
     * 路由规则项ID
     */
    private String routeId;
    /**
     * 命名空间id
     */
    private String namespaceId;
    /**
     * 服务名
     */
    private String serviceName;
    /**
     * 路由规则项包含的匹配条件列表
     */
    private List<RouteTag> tagList;
    /**
     * 路由规则项包含的目的列表
     */
    private List<RouteDest> destList;
    /**
     * 规则名
     */
    private String ruleName;

    /**
     * 空构造函数
     */
    public RouteRule() {
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public List<RouteTag> getTagList() {
        return tagList;
    }

    public void setTagList(List<RouteTag> tagList) {
        this.tagList = tagList;
    }

    public List<RouteDest> getDestList() {
        return destList;
    }

    public void setDestList(List<RouteDest> destList) {
        this.destList = destList;
    }

    @Override
    public String toString() {
        return "命名空间：" + namespaceId + "，服务：" + serviceName + "，规则：" + ruleName;
    }
}
