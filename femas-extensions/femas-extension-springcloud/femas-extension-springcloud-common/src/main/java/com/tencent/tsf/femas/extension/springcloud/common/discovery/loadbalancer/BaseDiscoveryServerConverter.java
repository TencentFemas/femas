package com.tencent.tsf.femas.extension.springcloud.common.discovery.loadbalancer;/*
/**
 * 所有spring cloud版本通用的converter转换器里的方法
 * 
 * @Author juanyinyang
 */

public interface BaseDiscoveryServerConverter {

    default String getNamespace() {
        return null;
    }

    default String getServiceName() {
        return null;
    }

    default String getVersion() {
        return null;
    }

}
