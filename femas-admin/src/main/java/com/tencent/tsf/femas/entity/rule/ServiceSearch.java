package com.tencent.tsf.femas.entity.rule;

import io.swagger.annotations.ApiModelProperty;

public class ServiceSearch {

    @ApiModelProperty("命名空间id")
    private String namespaceId;

    @ApiModelProperty("服务名称")
    private String serviceName;

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
}
