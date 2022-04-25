package com.tencent.tsf.femas.entity.rule.route;


public class TolerateModel {

    private String namespaceId;

    private String serviceName;

    public TolerateModel() {
    }

    public TolerateModel(String namespaceId, String serviceName) {
        this.namespaceId = namespaceId;
        this.serviceName = serviceName;
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


}
