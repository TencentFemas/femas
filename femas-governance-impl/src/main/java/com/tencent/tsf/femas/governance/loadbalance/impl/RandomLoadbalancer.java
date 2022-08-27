package com.tencent.tsf.femas.governance.loadbalance.impl;

import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.plugin.context.ConfigContext;
import java.util.List;
import java.util.Random;

/**
 *
 */
public class RandomLoadbalancer extends AbstractLoadbalancer {

    private final Random random = new Random(System.currentTimeMillis());

    @Override
    public ServiceInstance doSelect(List<ServiceInstance> serviceInstances) {
        int size = serviceInstances.size(); // 总个数
        return serviceInstances.get(random.nextInt(size));
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public void init(ConfigContext conf) throws FemasRuntimeException {

    }

    @Override
    public String getName() {
        return "random";
    }

    @Override
    public void destroy() {

    }
}
