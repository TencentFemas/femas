package com.tencent.tsf.femas.entity.registry;

import java.io.Serializable;

public class ServiceApiRequest implements Serializable {

    private String namespaceId;

    private String serviceName;

    private String applicationVersion;

    private String data;

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

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ServiceApiRequest{" +
                "namespaceId='" + namespaceId + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
