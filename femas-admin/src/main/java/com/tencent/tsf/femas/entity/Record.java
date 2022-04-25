package com.tencent.tsf.femas.entity;


import io.swagger.annotations.ApiModelProperty;

public class Record {

    @ApiModelProperty("事件时间")
    private long time;
    @ApiModelProperty("模块")
    private String module;
    @ApiModelProperty("操作类型")
    private String type;
    @ApiModelProperty("操作详情")
    private String detail;
    @ApiModelProperty("执行状态： true 成功  false 失败")
    private boolean status;
    @ApiModelProperty("操作人")
    private String user;
    @ApiModelProperty("id")
    private String logId;

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Record{" +
                "time=" + time +
                ", module='" + module + '\'' +
                ", type='" + type + '\'' +
                ", detail='" + detail + '\'' +
                ", status=" + status +
                ", user='" + user + '\'' +
                '}';
    }
}
