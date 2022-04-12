package com.tencent.tsf.femas.entity.service;

import io.swagger.annotations.ApiModelProperty;

public class ServiceEventView {

    @ApiModelProperty("实例id")
    private String instanceId;

    @ApiModelProperty("事件类型")
    private EventTypeEnum eventType;

    @ApiModelProperty("对象详情")
    private String detail;

    @ApiModelProperty("指标值")
    private String quality;

    @ApiModelProperty("发生时间")
    private Long createTime;

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public EventTypeEnum getEventType() {
        return eventType;
    }

    public void setEventType(EventTypeEnum eventType) {
        this.eventType = eventType;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }
}
