package com.tencent.tsf.femas.springcloud.discovery.starter.discovery.discovery.reactive;

import com.tencent.tsf.femas.common.discovery.ServiceDiscoveryClient;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;

/**
 * 描述：
 * 创建日期：2022年05月17 19:47:21
 *
 * @author gong zhao
 **/
public class FemasReactiveDiscoveryClient implements ReactiveDiscoveryClient {

    private static final String DESCRIPTION = "Spring Cloud Femas Reactive Discovery Client";

    private ServiceDiscoveryClient serviceDiscoveryClient;

    public FemasReactiveDiscoveryClient(ServiceDiscoveryClient serviceDiscoveryClient) {
        this.serviceDiscoveryClient = serviceDiscoveryClient;
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    @Override
    public reactor.core.publisher.Flux<ServiceInstance> getInstances(String serviceId) {
        return null;
    }

    @Override
    public reactor.core.publisher.Flux<String> getServices() {
        return null;
    }
}
