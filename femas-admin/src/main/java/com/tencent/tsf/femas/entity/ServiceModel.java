package com.tencent.tsf.femas.entity;

import io.swagger.annotations.ApiModelProperty;

/**
 * @Auther: yrz
 * @Date: 2021/05/08/16:52
 * @Descriptioin
 */
public class ServiceModel {

    @ApiModelProperty("命名空间id")
    public String namespaceId;

    @ApiModelProperty("服务名")
    public String serviceName;

    @ApiModelProperty("页数")
    public Integer pageNo;

    @ApiModelProperty("一页的数量")
    public Integer pageSize;

    public ServiceModel() {
    }

    public ServiceModel(String namespaceId, String serviceName) {
        this.namespaceId = namespaceId;
        this.serviceName = serviceName;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
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
