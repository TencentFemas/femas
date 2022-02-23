package com.tencent.tsf.femas.governance.loadbalance;

import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.governance.config.FemasPluginContext;
import java.util.List;

/**
 * @author leoziltong
 */
public class LoadbalancerManager {

    // 根据用户配置选取
    private static volatile Loadbalancer loadbalancer = FemasPluginContext.getLoadBalancer();

    public static ServiceInstance select(List<ServiceInstance> serviceInstances) {
        return loadbalancer.select(serviceInstances);
    }

    public static void update(Loadbalancer loadbalancer) {
        LoadbalancerManager.loadbalancer = loadbalancer;
    }
}
