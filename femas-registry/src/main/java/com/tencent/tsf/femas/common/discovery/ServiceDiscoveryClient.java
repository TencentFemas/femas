package com.tencent.tsf.femas.common.discovery;

import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import java.util.List;

/**
 * @author zhixinzxliu
 */
public interface ServiceDiscoveryClient {

    /**
     * Get all ServiceInstances associated with a particular service
     *
     * @param service the service to query
     * @return a List of ServiceInstance
     */
    List<ServiceInstance> getInstances(Service service);

    /**
     * 订阅服务
     */
    void subscribe(Service service);

    void addNotifyListener(ServiceNotifyListener listener);

    /**
     * 取消订阅
     */
    void unsubscribe(Service service);

    void removeNotifyListener(ServiceNotifyListener listener);
}
