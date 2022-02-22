package com.tencent.tsf.femas.governance.lane;

import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.governance.plugin.context.ConfigContext;
import java.util.List;

public class MockLaneFilter implements LaneFilter {

    @Override
    public String getType() {
        // 可能暂时不用
        return null;
    }

    @Override
    public String getName() {
        return "mockLane";
    }

    @Override
    public void preProcessLaneId() {

    }

    @Override
    public List<ServiceInstance> filterInstancesWithLane(Service service, List<ServiceInstance> serviceInstances) {
        // 直接返回
        return serviceInstances;
    }

    @Override
    public void init(ConfigContext conf) throws FemasRuntimeException {

    }

    @Override
    public void destroy() {

    }
}
