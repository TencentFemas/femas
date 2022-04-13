package com.tencent.tsf.femas.entity.registry;

import io.swagger.annotations.ApiModelProperty;
import java.util.HashSet;

public class ServiceOverview {

    @ApiModelProperty("版本")
    private HashSet<String> versions;

    @ApiModelProperty("实例数")
    private Integer instanceNum;

    @ApiModelProperty("存活实例数")
    private Integer liveInstanceCount;

    @ApiModelProperty("服务名")
    private String serviceName;

    @ApiModelProperty("服务状态")
    private String status;

    @ApiModelProperty("版本数")
    private Integer versionNum;

    @ApiModelProperty("命名空间id")
    private String namespaceId;

    @ApiModelProperty("命名空间名称")
    private String namespaceName;

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String namespaceName) {
        this.namespaceName = namespaceName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(Integer versionNum) {
        this.versionNum = versionNum;
    }

    public HashSet<String> getVersions() {
        return versions;
    }

    public void setVersions(HashSet<String> versions) {
        this.versions = versions;
    }

    public Integer getInstanceNum() {
        return instanceNum;
    }

    public void setInstanceNum(Integer instanceNum) {
        this.instanceNum = instanceNum;
    }

    public Integer getLiveInstanceCount() {
        return liveInstanceCount;
    }

    public void setLiveInstanceCount(Integer liveInstanceCount) {
        this.liveInstanceCount = liveInstanceCount;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
