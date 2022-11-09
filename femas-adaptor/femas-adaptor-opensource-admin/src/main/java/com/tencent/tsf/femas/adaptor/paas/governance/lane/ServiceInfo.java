package com.tencent.tsf.femas.adaptor.paas.governance.lane;

import java.util.Optional;

/**
 * @Author: cody
 * @Date: 2022/7/26
 * @Descriptioin
 */
public class ServiceInfo {

    private String serviceName;

    private String version;

    private String namespaceId;

    private String namespaceName;

    private Boolean entrance;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

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

    public Boolean getEntrance() {
        return Optional.ofNullable(entrance)
                .orElse(false);
    }

    public void setEntrance(Boolean entrance) {
        this.entrance = entrance;
    }
}
