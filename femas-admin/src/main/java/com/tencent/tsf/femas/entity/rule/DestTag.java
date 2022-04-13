package com.tencent.tsf.femas.entity.rule;

import io.swagger.annotations.ApiModelProperty;

public class DestTag {

    @ApiModelProperty("服务版本")
    private String serviceVersion;
    @ApiModelProperty("权重")
    private Integer weight;

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }
}