package com.tencent.tsf.femas.agent.loadbalance.instrument;

import com.netflix.loadbalancer.Server;

import java.util.List;

/**
 * @Author leoziltong@tencent.com
 */
public interface FemasServiceFilterLoadBalancer {

    void beforeChooseServer(Object key);

    void afterChooseServer(Server server, Object key);

    List<Server> filterAllServer(List<Server> servers);
}
