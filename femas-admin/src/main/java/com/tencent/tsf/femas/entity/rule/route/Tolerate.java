package com.tencent.tsf.femas.entity.rule.route;

import io.swagger.annotations.ApiModelProperty;


public class Tolerate {

    private String namespaceId;

    private String serviceName;

    @ApiModelProperty("容错保护 1：开启 0：关闭")
    private String isTolerant;


    public Tolerate() {
    }

    public Tolerate(String namespaceId, String serviceName) {
        this.namespaceId = namespaceId;
        this.serviceName = serviceName;
        this.isTolerant = "0";
    }

    public Tolerate(String namespaceId, String serviceName, boolean isTolerant) {
        this.namespaceId = namespaceId;
        this.serviceName = serviceName;
        this.isTolerant = (isTolerant ? "1" : "0");
    }

    public String getIsTolerant() {
        return isTolerant;
    }

    public void setIsTolerant(String isTolerant) {
        this.isTolerant = isTolerant;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        return "命名空间：" + namespaceId + "，服务：" + serviceName + "，容错保护开关：" + ("1".equalsIgnoreCase(isTolerant) ? "开启" : "关闭");
    }
}
