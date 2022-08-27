package com.tencent.tsf.femas.governance.route;

import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.common.tag.TagRule;
import com.tencent.tsf.femas.common.tag.engine.TagEngine;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.governance.event.RouterEventCollector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.tencent.tsf.femas.plugin.context.ConfigContext;
import com.tencent.tsf.femas.plugin.impl.config.ServiceRouterConfigImpl;
import com.tencent.tsf.femas.plugin.impl.config.rule.router.RouteDest;
import com.tencent.tsf.femas.plugin.impl.config.rule.router.RouteRule;
import com.tencent.tsf.femas.plugin.impl.config.rule.router.RouteRuleGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FemasDefaultRouteRuleRouter implements Router {

    private static final Logger logger = LoggerFactory.getLogger(FemasDefaultRouteRuleRouter.class);

    private static final Random RANDOM = new Random(System.currentTimeMillis());

    private volatile Context commonContext = ContextFactory.getContextInstance();

    @Override
    public Collection<ServiceInstance> route(Service service, Collection<ServiceInstance> instances) {
        RouteRuleGroup routeRuleGroup = RouterRuleManager.getRouteRuleGroup(service);

        if (CollectionUtil.isEmpty(instances) || routeRuleGroup == null) {
            if (CollectionUtil.isEmpty(instances)) {
                RouterEventCollector
                        .addRouterEvent(service, routeRuleGroup, Context.getAllSystemTags(), "no available instance!");
            }
            return instances;
        }

        boolean hit = false;
        if (!CollectionUtil.isEmpty(routeRuleGroup.getRuleList())) {
            for (RouteRule routeRule : routeRuleGroup.getRuleList()) {
                if (checkRouteRuleHit(routeRule)) {
                    hit = true;

                    // 根据命中的路由规则选择实例
                    List<ServiceInstance> instanceList = chooseInstanceByRouteRule(routeRule, instances);

                    if (!CollectionUtil.isEmpty(instanceList)) {
                        return instanceList;
                    }
                }
            }
        }

        /**
         * 命中规则且没有开启兜底策略
         */
        if (hit && routeRuleGroup.getFallback() == false) {
            RouterEventCollector
                    .addRouterEvent(service, routeRuleGroup, Context.getAllSystemTags(), "no available instance!");
            throw new RuntimeException("No available instances.");
        }
        if (hit) {
            RouterEventCollector
                    .addRouterEvent(service, routeRuleGroup, Context.getAllSystemTags(), "tolerant protection");
        }

        return instances;
    }

    @Override
    public String name() {
        return "FEMAS-DEFAULT-ROUTE-RULE-ROUTER";
    }

    @Override
    public int priority() {
        return 200;
    }

    /**
     * @param routeRule
     * @return
     */
    private List<ServiceInstance> chooseInstanceByRouteRule(RouteRule routeRule,
            Collection<ServiceInstance> instances) {
        Map<RouteDest, List<ServiceInstance>> routeDestInstanceMap = new HashMap<>();

        for (ServiceInstance instance : instances) {
            // 遍历每个routeDest
            // TODO 此处是否要要求 routeDest 之间条件互斥？互斥的话此处需要加 break
            for (RouteDest routeDest : routeRule.getDestList()) {
                if (matchRouteDest(instance, routeDest)) {
                    routeDestInstanceMap.computeIfAbsent(routeDest, k -> new ArrayList());
                    routeDestInstanceMap.get(routeDest).add(instance);
                } else {
                    routeDestInstanceMap.computeIfAbsent(routeDest, k -> new ArrayList());
                }
            }
        }

        // 如果此处根据rule没有筛选出可用实例，则返回null
        if (routeDestInstanceMap.isEmpty()) {
            return null;
        }

        RouteDest dest = randomRouteDest(routeDestInstanceMap.keySet());
        if (dest != null) {
            return routeDestInstanceMap.get(dest);
        }

        return null;
    }

    private Boolean matchRouteDest(ServiceInstance endpoint, RouteDest routeDest) {
        TagRule destItemList = routeDest.getDestItemList();

        // SysTag 可以是group，version等
        // UserTag可以是用户自己打得标签
        return TagEngine.checkRuleHit(destItemList, endpoint.getAllMetadata(), endpoint.getTags());
    }

    /**
     * 从 routeDestList 中，根据各routeDestList权重分配，选取命中的 routeDest
     *
     * @return
     */
    private RouteDest randomRouteDest(Collection<RouteDest> routeDestCollection) {
        if (!CollectionUtil.isEmpty(routeDestCollection)) {
            int sum = 0;
            Map<RouteDest, Integer> weightMap = new HashMap<>();
            for (RouteDest routeDest : routeDestCollection) {
                weightMap.put(routeDest, routeDest.getDestWeight());
                sum += routeDest.getDestWeight();
            }

            int random = RANDOM.nextInt(sum);
            int current = 0;
            for (Map.Entry<RouteDest, Integer> entry : weightMap.entrySet()) {
                current += entry.getValue();
                if (random < current) {
                    return entry.getKey();
                }
            }
        }

        return null;
    }

    /**
     * 校验 RouteRuleTagList 是否匹配命中
     * TAG 列表中，各TAG匹配条件需全部匹配满足，则命中结果为true， 否则为false
     *
     * @param routeRule 路由规则TAG列表
     * @return 是否匹配
     */
    private Boolean checkRouteRuleHit(RouteRule routeRule) {
        if (routeRule.getTagRule() != null) {
            return TagEngine.checkRuleHitByCurrentTags(routeRule.getTagRule());
        }

        // routeTagList 中各 routeTag 都满足匹配规则，则返回命中此routeTagList
        return true;
    }

    @Override
    public String getName() {
        return "FemasDefaultRoute";
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public void init(ConfigContext conf) throws FemasRuntimeException {
        ServiceRouterConfigImpl routerConfig = (ServiceRouterConfigImpl) conf.getConfig().getServiceRouter();
        if (routerConfig == null || routerConfig.getRouteRule() == null) {
            return;
        }
        Service service = new Service();
        RouteRuleGroup routeRuleGroup = routerConfig.getRouteRule();
        service.setName(routeRuleGroup.getServiceName());
        service.setNamespace(routeRuleGroup.getNamespace());
        try {
            RouterRuleManager.refreshRouteRule(service, routeRuleGroup);
        } catch (Exception e) {
            throw new FemasRuntimeException("route rule init refresh error");
        }
        logger.info("init circuit breaker rule: {}", routeRuleGroup.toString());
    }

    @Override
    public void destroy() {

    }
}
