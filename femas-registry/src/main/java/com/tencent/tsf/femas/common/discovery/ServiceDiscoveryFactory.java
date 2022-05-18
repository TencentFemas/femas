package com.tencent.tsf.femas.common.discovery;

import com.tencent.tsf.femas.common.annotation.SPI;
import com.tencent.tsf.femas.common.spi.SpiExtensionClass;

import java.util.Map;

import static com.tencent.tsf.femas.common.RegistryConstants.REGISTRY_HOST;
import static com.tencent.tsf.femas.common.RegistryConstants.REGISTRY_PORT;
import static com.tencent.tsf.femas.common.util.CommonUtils.checkNotNull;

/**
 * @author zhixinzxliu
 */
@SPI
public interface ServiceDiscoveryFactory extends SpiExtensionClass {

    /**
     * 通过配置获得ServiceDiscovery
     *
     * @param configMap 配置
     * @return ServiceDiscovery
     */
    ServiceDiscoveryClient getServiceDiscovery(Map<String, String> configMap);

    /**
     * 获得注册地址
     *
     * @param configMap 配置
     * @return 注册地址-host:port
     */
    default String getKey(Map<String, String> configMap) {
        String host = checkNotNull(REGISTRY_HOST, configMap.get(REGISTRY_HOST));
        String portString = checkNotNull(REGISTRY_PORT, configMap.get(REGISTRY_PORT));
        int port = Integer.parseInt(portString);
        return host + ":" + port;
    }

}
