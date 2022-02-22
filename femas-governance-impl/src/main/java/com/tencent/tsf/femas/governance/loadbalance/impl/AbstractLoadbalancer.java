package com.tencent.tsf.femas.governance.loadbalance.impl;

import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.entity.Request;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.governance.loadbalance.Loadbalancer;
import com.tencent.tsf.femas.governance.loadbalance.exception.FemasNoAvailableInstanceException;
import java.util.List;

public abstract class AbstractLoadbalancer implements Loadbalancer {

    @Override
    public ServiceInstance select(List<ServiceInstance> serviceInstances) {
        if (CollectionUtil.isEmpty(serviceInstances)) {
            Request request = Context.getRpcInfo().getRequest();
            throw new FemasNoAvailableInstanceException("No available instances. Request : " + request);
        }

        if (serviceInstances.size() == 1) {
            return serviceInstances.iterator().next();
        } else {
            return doSelect(serviceInstances);
        }
    }

    public abstract ServiceInstance doSelect(List<ServiceInstance> serviceInstances);
}
