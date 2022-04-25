package com.tencent.tsf.femas.entity.registry;

import io.swagger.annotations.ApiModelProperty;

public class RegistrySearch {

    @ApiModelProperty(value = "注册中心类型，ex:consul")
    private String registryType;

    @ApiModelProperty("注册中心状态 1：运行中 2：异常")
    private String status;

    public String getRegistryType() {
        return registryType;
    }

    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
