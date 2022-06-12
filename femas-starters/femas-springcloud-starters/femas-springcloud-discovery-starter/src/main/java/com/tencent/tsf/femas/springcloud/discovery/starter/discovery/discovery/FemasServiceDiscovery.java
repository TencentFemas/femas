package com.tencent.tsf.femas.springcloud.discovery.starter.discovery.discovery;

import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.springcloud.discovery.starter.discovery.FemasDiscoveryProperties;
import com.tencent.tsf.femas.springcloud.discovery.starter.discovery.entity.FemasServiceInstance;
import org.springframework.cloud.client.ServiceInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 描述：
 * 创建日期：2022年05月23 00:43:11
 *
 * @author gong zhao
 **/
public class FemasServiceDiscovery {

    private FemasDiscoveryProperties femasDiscoveryProperties;

    public FemasServiceDiscovery(FemasDiscoveryProperties femasDiscoveryProperties) {
        this.femasDiscoveryProperties = femasDiscoveryProperties;
    }

    /**
     * Return all instances for the given service.
     *
     * @param serviceId id of service
     * @return list of instances
     */
    public List<ServiceInstance> getInstances(String serviceId) {
        List<com.tencent.tsf.femas.common.entity.ServiceInstance> serviceInstanceList = serviceDiscoveryClient()
                .getInstances(new Service(serviceId, femasDiscoveryProperties.getNamespace()));
        return convertToServiceInstanceList(serviceInstanceList, serviceId);
    }

    /**
     * Return the names of all services.
     *
     * @return list of service names
     */
    public List<String> getServices() {
        return Collections.emptyList();
    }

    public static List<ServiceInstance> convertToServiceInstanceList(
            List<com.tencent.tsf.femas.common.entity.ServiceInstance> instances, String serviceId) {
        List<ServiceInstance> result = new ArrayList<>(instances.size());
        for (com.tencent.tsf.femas.common.entity.ServiceInstance instance : instances) {
            ServiceInstance serviceInstance = convertToServiceInstance(instance, serviceId);
            if (serviceInstance != null) {
                result.add(serviceInstance);
            }
        }
        return result;
    }

    public static ServiceInstance convertToServiceInstance(com.tencent.tsf.femas.common.entity.ServiceInstance instance,
                                                           String serviceId) {
        if (instance == null) {
            return null;
        }
        FemasServiceInstance femasServiceInstance = new FemasServiceInstance();
        femasServiceInstance.setServiceId(serviceId);
        femasServiceInstance.setHost(instance.getHost());
        femasServiceInstance.setPort(instance.getPort());
        Map<String, String> metadata = instance.getAllMetadata();
        if (metadata.containsKey("secure")) {
            boolean secure = Boolean.parseBoolean(metadata.get("secure"));
            femasServiceInstance.setSecure(secure);
        }
        femasServiceInstance.setMetadata(instance.getAllMetadata());
        return femasServiceInstance;
    }

    private com.tencent.tsf.femas.common.discovery.ServiceDiscoveryClient serviceDiscoveryClient() {
        return femasDiscoveryProperties.serviceDiscoveryClient();
    }
}
