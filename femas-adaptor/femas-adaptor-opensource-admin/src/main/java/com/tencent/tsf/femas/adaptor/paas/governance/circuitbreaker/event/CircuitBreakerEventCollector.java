package com.tencent.tsf.femas.adaptor.paas.governance.circuitbreaker.event;

import com.tencent.tsf.femas.common.context.FemasContext;
import com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService;
import com.tencent.tsf.femas.governance.event.EventCollector;
import com.tencent.tsf.femas.governance.event.EventTypeEnum;
import com.tencent.tsf.femas.governance.event.FemasEventData;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CircuitBreakerEventCollector extends EventCollector {

    public static final String TO_STATE = "to_state";
    private static final Logger LOGGER = LoggerFactory.getLogger(CircuitBreakerEventCollector.class);
    // dimensions keys
    private static final String APP_ID_KEY = "app_id";
    private static final String NAMESPACE_ID_KEY = "namespace_id";
    private static final String SERVICE_NAME = "service_name";
    // additionalMsg keys
    private static final String ISOLATION_OBJECT_KEY = "isolation_object";
    private static final String FAILURE_RATE_KEY = "failure_rate";
    private static final String SLOW_CALL_DURATION_KEY = "slow_call_rate";
    private static final String FROM_STATE = "from_state";

    public CircuitBreakerEventCollector() {
    }

    public static void addCircuitBreakerEvent(long occurTime, ICircuitBreakerService.State from,
            ICircuitBreakerService.State to,
            String targetServiceName,
            String targetNamespace, String isolationObject,
            String failureRate, String slowCallRate) {

        FemasEventData eventData = FemasEventData.custom()
                .setOccurTime(occurTime)
                .setEventType(EventTypeEnum.CIRCUITBREAKER)
                .setInstanceId(instanceId)
                .setUpstream(FemasContext.getServiceName())
                .setDownstream(targetServiceName)
                .setAddition(NAMESPACE_ID_KEY, namespaceId)
                .setAddition(SERVICE_NAME, FemasContext.getServiceName())
                .setAddition(UPSTREAM_NAMESPACE_ID_KEY, namespaceId)
                .setAddition(DOWNSTREAM_NAMESPACE_ID_KEY, targetNamespace)
                .setAddition(ISOLATION_OBJECT_KEY, isolationObject)
                .setAddition(FAILURE_RATE_KEY, failureRate)
                .setAddition(SLOW_CALL_DURATION_KEY, slowCallRate)
                .setAddition(FROM_STATE, from.toString())
                .setAddition(TO_STATE, to.toString())
                .build();

        // 如果满了就抛出异常
        try {
            eventQueue.add(eventData);
            LOGGER.info("[FEMAS CIRCUIT BREAKER EVENT COLLECTOR] Add event to EventQueue. TargetServiceName : "
                    + targetServiceName + ", isolationObject : " + isolationObject);
        } catch (Exception e) {
            LOGGER.warn("[FEMAS EVENT COLLECTOR] eventQueue is full. Log this event and drop it.");
        }
    }

    public BlockingQueue<FemasEventData> getEventQueue() {
        return eventQueue;
    }

}
