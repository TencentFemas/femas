package com.tencent.tsf.femas.entity.registry;

import io.swagger.annotations.ApiModelProperty;

public class ServiceApi {

    @ApiModelProperty("接口路径")
    private String path;

    @ApiModelProperty("接口状态 1 正常  2 异常")
    private String status;

    @ApiModelProperty("服务版本")
    private String serviceVersion;

    @ApiModelProperty("请求类型 POST,GET")
    private String method;

    @ApiModelProperty("命名空间")
    private String namespaceId;

    @ApiModelProperty("服务名")
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

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }
}
