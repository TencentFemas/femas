package com.tencent.tsf.femas.common.discovery;

import com.tencent.tsf.femas.common.RegistryEnum;
import com.tencent.tsf.femas.common.spi.SpiService;

import java.util.Map;

public class DiscoveryService {

    private DiscoveryService() {
        // static class
    }

    /**
     * key是注册中心的类型
     * value是注册中心的类别
     */
    private static Map<String, ServiceDiscoveryFactory> DISCOVERY_FACTORIES;

    /**
     * 创建新的Discovery然后返回
     *
     * @param type    类型 {@link RegistryEnum}
     * @param configs 配置
     * @return DiscoveryClient
     * @see ServiceDiscoveryFactory
     * @see RegistryEnum
     */
    public static synchronized ServiceDiscoveryClient createDiscoveryClient(String type, Map<String, String> configs) {
        if (DISCOVERY_FACTORIES == null) {
            DISCOVERY_FACTORIES = SpiService.init(ServiceDiscoveryFactory.class);
        }

        ServiceDiscoveryFactory discoveryFactory = DISCOVERY_FACTORIES.get(type.toUpperCase());
        if (discoveryFactory == null) {
            throw new IllegalArgumentException(
                    "Invalid type " + type + ". ServiceDiscoveryFactory : Type not registered.");
        }

        return discoveryFactory.getServiceDiscovery(configs);
    }

    /**
     * 创建新的Discovery然后返回
     *
     * @param type    类型 {@link RegistryEnum}
     * @param configs 配置
     * @return DiscoveryClient
     * @see ServiceDiscoveryFactory
     */
    public static synchronized ServiceDiscoveryClient createDiscoveryClient(RegistryEnum type, Map<String, String> configs) {
        if (DISCOVERY_FACTORIES == null) {
            DISCOVERY_FACTORIES = SpiService.init(ServiceDiscoveryFactory.class);
        }

        ServiceDiscoveryFactory discoveryFactory = DISCOVERY_FACTORIES.get(type.getAlias().toUpperCase());
        if (discoveryFactory == null) {
            throw new IllegalArgumentException(
                    "Invalid type " + type + ". ServiceDiscoveryFactory : Type not registered.");
        }

        return discoveryFactory.getServiceDiscovery(configs);
    }

}
