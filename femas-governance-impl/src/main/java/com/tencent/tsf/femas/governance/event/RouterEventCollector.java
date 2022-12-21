package com.tencent.tsf.femas.governance.event;

import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.entity.Service;
import java.util.Date;
import java.util.Map;

import com.tencent.tsf.femas.plugin.impl.config.rule.router.RouteRuleGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Cody
 * @date 2021 2021/7/14 10:17 上午
 */
public class RouterEventCollector extends EventCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouterEventCollector.class);

    public static void addRouterEvent(Service targetService, RouteRuleGroup routeRuleGroup, Map sysTag, String detail) {
        FemasEventData eventData = FemasEventData.custom()
                .setEventType(EventTypeEnum.ROUTER)
                .setInstanceId(instanceId)
                .setOccurTime(System.currentTimeMillis())
                .setUpstream(Context.getSystemTag("service.name"))
                .setDownstream(targetService.getName())
                .setAddition(UPSTREAM_NAMESPACE_ID_KEY, Context.getSystemTag("namespace.id"))
                .setAddition(DOWNSTREAM_NAMESPACE_ID_KEY, targetService.getNamespace())
                .setAddition(DETAIL, detail)
                .build();
        try {
            eventQueue.add(eventData);
            LOGGER.info("[FEMAS ROUTER EVENT COLLECTOR] Add event to EventQueue. TargetServiceName : " +
                    targetService.getName());
        } catch (Exception e) {
            LOGGER.warn("[FEMAS EVENT COLLECTOR] eventQueue is full. Log this event and drop it.");
        }
    }
}
