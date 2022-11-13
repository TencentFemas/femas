package com.tencent.tsf.femas.governance.event;

import com.tencent.tsf.femas.common.context.Context;
import java.util.Date;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Cody
 * @date 2021 2021/7/14 5:16 下午
 */
public class LimitEventCollector extends EventCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(LimitEventCollector.class);

    public static void addLimitEvent(Map<String, String> sysTag) {

        FemasEventData eventData = FemasEventData.custom()
                .setInstanceId(instanceId)
                .setEventType(EventTypeEnum.RATELIMIT)
                .setOccurTime(System.currentTimeMillis())
                .setUpstream(sysTag.get("source.service.name"))
                .setDownstream(sysTag.get("service.name"))
                .setAddition(UPSTREAM_NAMESPACE_ID_KEY, sysTag.get("namespace.id"))
                .setAddition(DOWNSTREAM_NAMESPACE_ID_KEY, Context.getSystemTag("namespace.id"))
                .build();
        try {
            eventQueue.add(eventData);
            LOGGER.info("[FEMAS RateLimit EVENT COLLECTOR] Add event to EventQueue. TargetServiceName : " +
                    sysTag.get("source.service.name"));
        } catch (Exception e) {
            LOGGER.warn("[FEMAS EVENT COLLECTOR] eventQueue is full. Log this event and drop it.");
        }
    }
}
