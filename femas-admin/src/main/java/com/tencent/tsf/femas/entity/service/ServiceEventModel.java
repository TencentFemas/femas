package com.tencent.tsf.femas.entity.service;

import com.tencent.tsf.femas.entity.ServiceModel;
import io.swagger.annotations.ApiModelProperty;

public class ServiceEventModel extends ServiceModel {

    @ApiModelProperty("查询起始时间戳")
    private Long startTime;

    @ApiModelProperty("查询截止时间戳")
    private Long endTime;

    @ApiModelProperty("事件类型过滤: CIRCUITBREAKER")
    private String eventType;


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

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
