package com.tencent.tsf.femas.common.entity;

import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

/**
 * 节点状态
 *
 * @author zhixinzxliu
 */
public enum EndpointStatus {
    /**
     * 启动中
     */
    UP,

    /**
     * 正在初始化
     */
    INITIALIZING,

    /**
     * 正在关闭
     * 优雅关闭使用，不接受新流量
     */
    CLOSING,

    /**
     * 服务下线
     */
    DOWN,

    /**
     * 与注册中心失联
     */
    OUT_OF_CONTACT,

    UNKNOWN;
    
    public static EndpointStatus getTypeByName(String var) {
        if (StringUtils.isBlank(var)) {
            return UNKNOWN;
        }

        return Stream.of(values())
                .filter(status -> var.equalsIgnoreCase(status.name()))
                .findAny()
                .orElse(UNKNOWN);
    }

}
