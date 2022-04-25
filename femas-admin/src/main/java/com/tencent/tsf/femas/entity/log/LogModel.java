package com.tencent.tsf.femas.entity.log;

import com.tencent.tsf.femas.entity.Page;
import io.swagger.annotations.ApiModelProperty;

public class LogModel extends Page {

    @ApiModelProperty("起始时间戳")
    private Long startTime;

    @ApiModelProperty("截止时间戳")
    private Long endTime;

    @ApiModelProperty("模块查询 命名空间：NAMESPACE，注册中心：REGISTRY，鉴权：AUTH，熔断：BREAKER，限流：LIMIT，路由：ROUTE")
    private String module;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
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
}
