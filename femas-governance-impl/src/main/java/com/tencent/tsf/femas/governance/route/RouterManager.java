package com.tencent.tsf.femas.governance.route;

import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.plugin.impl.FemasPluginContext;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author zhixinzxliu
 */
public class RouterManager {

    private static List<Router> routers = FemasPluginContext.getServiceRouters();

    // 按照权重从小到到排序，小的在前
    private static Comparator<Router> DEFAULT_COMPARATOR = priorityComparator();

    public static Collection<ServiceInstance> route(Service service, Collection<ServiceInstance> serviceInstances) {
        Collection<ServiceInstance> instances = serviceInstances;

        for (Router router : routers) {
            instances = router.route(service, instances);
        }

        return instances;
    }

    /**
     * 注册一个路由器
     * 按照路由器权重，从小到大进行排序
     *
     * @param router
     */
    public static synchronized void registerRouter(Router router) {
        routers.add(router);
        Collections.sort(routers, DEFAULT_COMPARATOR);
    }

    /**
     * 从队列中删除一个路由器
     *
     * @param router
     */
    public static synchronized void removeRouter(Router router) {
        routers.remove(router);
    }

    public static List<Router> getRouters() {
        return Collections.unmodifiableList(routers);
    }

    private static Comparator<Router> priorityComparator() {
        return new Comparator<Router>() {
            @Override
            public int compare(Router o1, Router o2) {
                return o1.priority() - o2.priority();
            }
        };
    }
}
