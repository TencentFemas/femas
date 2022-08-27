package com.tencent.tsf.femas.governance.route;

import com.google.common.collect.Maps;
import com.tencent.tsf.femas.common.entity.Service;
import java.util.Map;

import com.tencent.tsf.femas.plugin.impl.config.rule.router.RouteRuleGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouterRuleManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouterRuleManager.class);

    private static Map<Service, RouteRuleGroup> routeRuleMap = Maps.newConcurrentMap();

    public static RouteRuleGroup getRouteRuleGroup(Service service) {
        return routeRuleMap.get(service);
    }

    public static void refreshRouteRule(Service service, RouteRuleGroup routeRuleGroup) {
        if (routeRuleGroup == null) {
            routeRuleMap.remove(service);
        }

        routeRuleMap.put(service, routeRuleGroup);
        LOGGER.info("Refresh router rule. Service : " + service + ", rule " + routeRuleGroup);
    }

    public static void removeRouteRule(Service service) {
        routeRuleMap.remove(service);
        LOGGER.info("Remove router rule. Service : " + service);
    }
}
