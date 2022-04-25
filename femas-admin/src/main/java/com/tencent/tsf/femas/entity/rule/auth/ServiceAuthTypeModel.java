package com.tencent.tsf.femas.entity.rule.auth;

import io.swagger.annotations.ApiModelProperty;

/**
 * @Auther: yrz
 * @Date: 2021/05/08/18:48
 * @Descriptioin
 */
public class ServiceAuthTypeModel {

    @ApiModelProperty("命名空间id")
    private String namespaceId;

    @ApiModelProperty("服务名")
    private String serviceName;

    @ApiModelProperty("规则开启状态：close、white、black")
    private String type;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        String w = "";
        if ("close".equalsIgnoreCase(type)) {
            w = "关闭";
        } else if ("white".equalsIgnoreCase(type)) {
            w = "白名单";
        } else if ("black".equalsIgnoreCase(type)) {
            w = "黑名单";
        }
        return "命名空间：" + namespaceId + "，服务名：" + serviceName + "，设置规则类型：" + w;
    }
}
