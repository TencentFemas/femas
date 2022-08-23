package com.tencent.tsf.femas.governance.route;

import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.plugin.Plugin;
import java.util.Collection;

/**
 * 路由器
 *
 * @author zhixinzxliu
 */
public interface Router extends Plugin {

    /**
     * 给定一组服务节点，返回零个或多个服务节点
     *
     * @param serviceInstances
     * @return
     */
    Collection<ServiceInstance> route(Service service, Collection<ServiceInstance> serviceInstances);

    /**
     * 返回该Router对应的名称
     *
     * @return
     */
    String name();

    /**
     * 返回该路由器的优先级
     *
     * @return
     */
    int priority();
}
