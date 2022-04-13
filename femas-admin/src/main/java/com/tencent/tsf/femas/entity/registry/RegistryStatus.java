package com.tencent.tsf.femas.entity.registry;

public enum RegistryStatus {

    RUNNING(1, "运行中"),
    DOWN(2, "异常");
    Integer code;
    String status;

    RegistryStatus(Integer code, String status) {
        this.code = code;
        this.status = status;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}