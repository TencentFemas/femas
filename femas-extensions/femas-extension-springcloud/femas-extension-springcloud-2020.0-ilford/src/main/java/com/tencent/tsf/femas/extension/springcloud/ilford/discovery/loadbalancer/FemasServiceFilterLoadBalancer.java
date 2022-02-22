package com.tencent.tsf.femas.extension.springcloud.ilford.discovery.loadbalancer;

import org.springframework.cloud.client.ServiceInstance;


public interface FemasServiceFilterLoadBalancer {

    void beforeChooseServer(Object key);

    void afterChooseServer(ServiceInstance server, Object key);

}
