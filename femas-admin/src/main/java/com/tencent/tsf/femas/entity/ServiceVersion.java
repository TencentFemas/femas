package com.tencent.tsf.femas.entity;

/**
 * @Author: cody
 * @Date: 2022/9/13
 * @Descriptioin
 */
public class ServiceVersion {

    private String serviceName;

    private String version;

    private String namespaceId;

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

    public ServiceVersion(String serviceName, String version, String namespaceId) {
        this.serviceName = serviceName;
        this.version = version;
        this.namespaceId = namespaceId;
    }

    public ServiceVersion() {
    }
}
