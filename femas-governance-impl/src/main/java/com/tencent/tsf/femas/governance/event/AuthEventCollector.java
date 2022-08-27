package com.tencent.tsf.femas.governance.event;


import java.util.Date;
import java.util.Map;

import com.tencent.tsf.femas.plugin.impl.config.rule.auth.AuthRuleGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Cody
 * @date 2021 2021/7/13 6:51 下午
 */
public class AuthEventCollector extends EventCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthEventCollector.class);

    public static void addAuthEvent(AuthRuleGroup authRuleGroup, Map<String, String> sysTag) {
        FemasEventData eventData = FemasEventData.custom()
                .setEventType(EventTypeEnum.AUTH)
                .setInstanceId(instanceId)
                .setOccurTime(new Date().getTime())
                .setUpstream(sysTag.get("source.service.name"))
                .setDownstream(sysTag.get("service.name"))
                .setAddition(UPSTREAM_NAMESPACE_ID_KEY, sysTag.get("namespace.id"))
                .setAddition(DOWNSTREAM_NAMESPACE_ID_KEY, sysTag.get("namespace.id"))
                .setAddition("type", authRuleGroup.getType())
                .build();
        try {
            eventQueue.add(eventData);
            LOGGER.info("[FEMAS AUTH EVENT COLLECTOR] Add event to EventQueue sourceService : " + sysTag
                    .get("source.service.name"));
        } catch (Exception e) {
            LOGGER.warn("[FEMAS EVENT COLLECTOR] eventQueue is full. Log this event and drop it.");
        }
    }
}
