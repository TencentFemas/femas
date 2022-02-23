package com.tencent.tsf.femas.governance.loadbalance.impl;

import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.entity.Request;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.common.util.PositiveAtomicCounter;
import com.tencent.tsf.femas.governance.plugin.context.ConfigContext;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RoundRobinLoadbalancer extends AbstractLoadbalancer {

    private final ConcurrentMap<String, PositiveAtomicCounter> indexes = new ConcurrentHashMap();

    @Override
    public ServiceInstance doSelect(List<ServiceInstance> serviceInstances) {
        Request request = Context.getRpcInfo().getRequest();

        String key = getServiceKey(request); // 每个方法级自己轮询，互不影响
        int length = serviceInstances.size(); // 总个数
        PositiveAtomicCounter index = indexes.get(key);
        if (index == null) {
            indexes.putIfAbsent(key, new PositiveAtomicCounter());
            index = indexes.get(key);
        }

        return serviceInstances.get(index.getAndIncrement() % length);
    }

    private String getServiceKey(Request request) {
        StringBuilder builder = new StringBuilder();
        builder.append(request.getTargetService()).append("#")
                .append(request.getTargetMethod());
        return builder.toString();
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
        return "roundRobin";
    }

    @Override
    public void destroy() {

    }
}
