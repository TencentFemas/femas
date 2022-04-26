package com.tencent.tsf.femas.agent.common;

/**
 * @Author leoziltong@tencent.com
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
