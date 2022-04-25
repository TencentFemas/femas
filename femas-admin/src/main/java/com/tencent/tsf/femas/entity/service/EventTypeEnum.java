package com.tencent.tsf.femas.entity.service;


public enum EventTypeEnum {


    /**
     * 熔断事件
     */
    CIRCUITBREAKER("熔断事件"),

    /**
     * 限流事件
     */
    RATELIMIT("限流事件"),

    /**
     * 鉴权事件
     */
    AUTH("鉴权事件"),

    /**
     * 路由事件
     */
    ROUTER("路由事件");

    public String name;

    EventTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
