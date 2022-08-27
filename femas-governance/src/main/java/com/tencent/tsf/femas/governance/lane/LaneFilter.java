package com.tencent.tsf.femas.governance.lane;

import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.plugin.Plugin;
import java.util.List;

public interface LaneFilter extends Plugin {

    void preProcessLaneId();

    List<ServiceInstance> filterInstancesWithLane(Service service, List<ServiceInstance> serviceInstances);
}
