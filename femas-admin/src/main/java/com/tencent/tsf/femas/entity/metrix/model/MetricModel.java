package com.tencent.tsf.femas.entity.metrix.model;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author Cody
 * @date 2021 2021/8/19 10:28 上午
 */
public class MetricModel {

    @ApiModelProperty("命名空间")
    private String namespaceId;

    @ApiModelProperty("服务名")
    private String serviceName;

    @ApiModelProperty("起止时间")
    private Long startTime;

    @ApiModelProperty("结束时间")
    private Long endTime;

    @ApiModelProperty("步长")
    private Long step;

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

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Long getStep() {
        return step;
    }

    public void setStep(Long step) {
        this.step = step;
    }
}
