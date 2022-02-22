package com.tencent.tsf.femas.governance.circuitbreaker;


/**
 * 熔断级别
 *
 * @author zhixinzxliu
 */
public enum FemasCircuitBreakerIsolationLevelEnum {
    // 服务级别熔断
    SERVICE,

    // API级别熔断
    API,

    // 实例级别熔断
    INSTANCE
}
