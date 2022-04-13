package com.tencent.tsf.femas.entity.registry;

import io.swagger.annotations.ApiModelProperty;

public class RegistryConfigModel {

    @ApiModelProperty("注册中心状态 1：运行中 2：异常")
    private Integer status;

    @ApiModelProperty("实例数")
    private Integer instanceCount;

    public Integer getInstanceCount() {
        return instanceCount;
    }

    public void setInstanceCount(Integer instanceCount) {
        this.instanceCount = instanceCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
