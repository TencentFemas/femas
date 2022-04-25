package com.tencent.tsf.femas.entity.param;

import com.tencent.tsf.femas.entity.Page;
import io.swagger.annotations.ApiModelProperty;

public class InstanceVersionParam extends Page {

    @ApiModelProperty("服务版本过滤")
    private String serviceVersion;

    @ApiModelProperty("实例状态过滤")
    private String status;

    @ApiModelProperty("实例id过滤")
    private String keyword;

    private String namespaceId;

    private String serviceName;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
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


    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
