package com.tencent.tsf.femas.common.discovery;

import static com.tencent.tsf.femas.common.RegistryConstants.REGISTRY_HOST;
import static com.tencent.tsf.femas.common.RegistryConstants.REGISTRY_PORT;
import static com.tencent.tsf.femas.common.util.CommonUtils.checkNotNull;

import com.tencent.tsf.femas.common.annotation.SPI;
import com.tencent.tsf.femas.common.spi.SpiExtensionClass;
import java.util.Map;

/**
 * @author zhixinzxliu
 */
@SPI
public interface ServiceDiscoveryFactory extends SpiExtensionClass {

    ServiceDiscoveryClient getServiceDiscovery(Map<String, String> configMap);

    default String getKey(Map<String, String> configMap) {
        String host = checkNotNull(REGISTRY_HOST, configMap.get(REGISTRY_HOST));
        String portString = checkNotNull(REGISTRY_PORT, configMap.get(REGISTRY_PORT));
        Integer port = Integer.parseInt(portString);
        String key = host + ":" + port;
        return key;
    }

}
