package com.tencent.tsf.femas.adaptor.paas.registry;


import com.tencent.tsf.femas.common.RegistryEnum;
import com.tencent.tsf.femas.common.constant.FemasConstant;
import com.tencent.tsf.femas.common.context.FemasContext;
import com.tencent.tsf.femas.common.discovery.DiscoveryService;
import com.tencent.tsf.femas.common.discovery.ServiceDiscoveryClient;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistryMetadata;
import com.tencent.tsf.femas.common.serviceregistry.RegistryService;
import com.tencent.tsf.femas.common.serviceregistry.ServiceRegistry;
import com.tencent.tsf.femas.common.util.StringUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/4/6 16:51
 * @Version 1.0
 */
public class FemasRegistry extends AbstractServiceRegistryMetadata {

    private static volatile ServiceDiscoveryClient serviceDiscoveryClient;

    private static volatile ServiceRegistry serviceRegistry;

    /**
     * 多注册中心的支持
     *
     * @param service
     * @param type
     * @return
     */
    public static List<ServiceInstance> getServiceInstances(Service service, RegistryEnum type) {
        if (serviceDiscoveryClient == null) {
            synchronized (FemasRegistry.class) {
                if (serviceDiscoveryClient == null) {
                    serviceDiscoveryClient = DiscoveryService
                            .createDiscoveryClient(type.name(), FemasContext.REGISTRY_CONFIG_MAP);
                }
            }
        }

        serviceDiscoveryClient.subscribe(service);
        List<ServiceInstance> serviceInstances = serviceDiscoveryClient.getInstances(service);

        // TODO
        // 处理流程
        return serviceInstances;
    }

    public static void registerInstance(ServiceInstance instance, RegistryEnum type) {
        if (serviceRegistry == null) {
            synchronized (FemasRegistry.class) {
                if (serviceRegistry == null) {
                    serviceRegistry = RegistryService.createRegistry(type.name(), FemasContext.REGISTRY_CONFIG_MAP);
                }
            }
        }
        serviceRegistry.register(instance);
    }

    public static void deregisterInstance(ServiceInstance instance, RegistryEnum type) {
        if (serviceRegistry == null) {
            synchronized (FemasRegistry.class) {
                if (serviceRegistry == null) {
                    serviceRegistry = RegistryService.createRegistry(type.name(), FemasContext.REGISTRY_CONFIG_MAP);
                }
            }
        }
        serviceRegistry.deregister(instance);
    }

    private static String getEmptyOrDefault(String value, String defaultValue) {
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        return value;
    }

    public Map<String, String> getRegisterMetadataMap() {
        Map<String, String> metaDataMap = new HashMap<>(13);
        metaDataMap.put(FemasConstant.FEMAS_META_APPLICATION_ID_KEY,
                getEmptyOrDefault(FemasContext.getSystemTag(FemasConstant.FEMAS_APPLICATION_ID), ""));
        metaDataMap.put(FemasConstant.FEMAS_META_APPLICATION_VERSION_KEY,
                getEmptyOrDefault(FemasContext.getSystemTag(FemasConstant.FEMAS_APPLICATION_VERSION), ""));
        metaDataMap.put(FemasConstant.FEMAS_META_GROUP_ID_KEY,
                getEmptyOrDefault(FemasContext.getSystemTag(FemasConstant.FEMAS_GROUP_ID), ""));
        metaDataMap.put(FemasConstant.FEMAS_META_INSTANCE_ID_KEY,
                getEmptyOrDefault(FemasContext.getSystemTag(FemasConstant.FEMAS_INSTANCE_ID), ""));
        metaDataMap.put(FemasConstant.FEMAS_META_REGION_KEY,
                getEmptyOrDefault(FemasContext.getSystemTag(FemasConstant.FEMAS_REGION), ""));
        metaDataMap.put(FemasConstant.FEMAS_META_ZONE_KEY,
                getEmptyOrDefault(FemasContext.getSystemTag(FemasConstant.FEMAS_ZONE), ""));
        metaDataMap.put(FemasConstant.FEMAS_META_CLUSTER_ID_KEY,
                getEmptyOrDefault(FemasContext.getSystemTag(FemasConstant.FEMAS_CLUSTER_ID), ""));
        metaDataMap.put(FemasConstant.FEMAS_META_NAMESPACE_ID_KEY,
                getEmptyOrDefault(FemasContext.getSystemTag(FemasConstant.FEMAS_NAMESPACE_ID), ""));
        metaDataMap.put(FemasConstant.FEMAS_CLIENT_SDK_VERSION,
                getEmptyOrDefault(FemasContext.getSystemTag(FemasConstant.FEMAS_CLIENT_SDK_VERSION_KEY),
                        FemasConstant.FEMAS_CLIENT_SDK_DEFAULT_VERSION));
        return metaDataMap;
    }

}
