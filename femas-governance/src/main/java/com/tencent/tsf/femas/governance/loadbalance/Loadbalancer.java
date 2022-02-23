package com.tencent.tsf.femas.governance.loadbalance;

import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.governance.plugin.Plugin;
import java.util.List;

/**
 * @author zhixinzxliu
 */
public interface Loadbalancer extends Plugin {

    /**
     * 给定一组服务节点，根据负载算法，选择一台服务节点
     *
     * @param serviceInstances
     * @return
     */
    ServiceInstance select(List<ServiceInstance> serviceInstances);
}
