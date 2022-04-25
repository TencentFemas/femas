package com.tencent.tsf.femas.extension.springcloud.hoxton.discovery.ribbon;

import com.netflix.loadbalancer.Server;

import java.util.List;


public interface FemasServiceFilterLoadBalancer {

    void beforeChooseServer(Object key);

    void afterChooseServer(Server server, Object key);

    List<Server> filterAllServer(List<Server> servers);
}
