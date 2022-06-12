package com.tencent.tsf.femas.springcloud.discovery.starter.discovery.discovery;

import com.tencent.tsf.femas.springcloud.discovery.starter.discovery.FemasDiscoveryProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;

/**
 * 描述：
 * 创建日期：2022年05月11 19:43:31
 *
 * @author gong zhao
 **/
public class FemasDiscoveryClient implements DiscoveryClient {

    private static final String DESCRIPTION = "Spring Cloud Femas Discovery Client";

    private static final String DEFAULT_GROUP = "DEFAULT_GROUP";

    private FemasServiceDiscovery femasServiceDiscovery;
    private FemasDiscoveryProperties femasDiscoveryProperties;

    public FemasDiscoveryClient(FemasServiceDiscovery femasServiceDiscovery, FemasDiscoveryProperties femasDiscoveryProperties) {
        this.femasServiceDiscovery = femasServiceDiscovery;
        this.femasDiscoveryProperties = femasDiscoveryProperties;
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
        return femasServiceDiscovery.getInstances(serviceId);
    }

    @Override
    public List<String> getServices() {
        return femasServiceDiscovery.getServices();
    }
}
