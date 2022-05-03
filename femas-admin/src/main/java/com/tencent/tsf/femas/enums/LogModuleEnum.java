package com.tencent.tsf.femas.enums;

/**
 * 日志根据模块查询枚举
 */
public enum LogModuleEnum {


    NAMESPACE("命名空间"),

    REGISTRY("注册中心"),

    AUTH("服务鉴权"),

    BREAKER("服务熔断"),

    LIMIT("服务限流"),

    ROUTE("服务路由");

    private String name;

    LogModuleEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
