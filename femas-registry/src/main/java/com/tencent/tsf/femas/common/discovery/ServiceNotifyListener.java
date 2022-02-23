package com.tencent.tsf.femas.common.discovery;

import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import java.util.List;

public interface ServiceNotifyListener {

    void notify(Service service, List<ServiceInstance> instances);

    void notifyOnRemoved(Service service, List<ServiceInstance> instances);

    void notifyOnAdded(Service service, List<ServiceInstance> instances);
}
