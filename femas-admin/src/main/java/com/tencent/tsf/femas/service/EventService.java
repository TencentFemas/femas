package com.tencent.tsf.femas.service;

import static com.tencent.tsf.femas.entity.service.EventTypeEnum.AUTH;
import static com.tencent.tsf.femas.entity.service.EventTypeEnum.CIRCUITBREAKER;
import static com.tencent.tsf.femas.entity.service.EventTypeEnum.RATELIMIT;
import static com.tencent.tsf.femas.entity.service.EventTypeEnum.ROUTER;

import com.tencent.tsf.femas.entity.rule.FemasEventData;
import com.tencent.tsf.femas.entity.service.ServiceEventView;

/**
 * @author Cody
 * @date 2021 2021/7/14 3:12 下午
 */
public class EventService {

    public static ServiceEventView parse(FemasEventData eventData) {
        ServiceEventView serviceEventView = new ServiceEventView();
        serviceEventView.setInstanceId(eventData.getInstanceId());
        serviceEventView.setCreateTime(eventData.getOccurTime());
        switch (eventData.getEventType()) {
            case CIRCUITBREAKER:
                serviceEventView = parseCircuitBreakerEvent(eventData, serviceEventView);
                break;
            case ROUTER:
                serviceEventView = parseRouterEvent(eventData, serviceEventView);
                break;
            case AUTH:
                serviceEventView = parseAuthEvent(eventData, serviceEventView);
                break;
            case RATELIMIT:
                serviceEventView = parseRateLimitEvent(eventData, serviceEventView);
        }
        return serviceEventView;
    }

    public static ServiceEventView parseCircuitBreakerEvent(FemasEventData eventData,
            ServiceEventView serviceEventView) {
        serviceEventView.setEventType(CIRCUITBREAKER);
        String failureRate = eventData.getAdditionalMsg().get("failure_rate");
        String slowCallRate = eventData.getAdditionalMsg().get("slow_call_rate");
        String downStream = eventData.getDownstream();
        String fromState = eventData.getAdditionalMsg().get("from_state");
        String toState = eventData.getAdditionalMsg().get("to_state");
        serviceEventView.setDetail("downstream_service：" + downStream);
        serviceEventView.setQuality("failure_rate:" + failureRate + "  slow_call_rate:" + slowCallRate +
                "(" + fromState + " -> " + toState + ")");
        return serviceEventView;
    }

    public static ServiceEventView parseRouterEvent(FemasEventData eventData, ServiceEventView serviceEventView) {
        serviceEventView.setEventType(ROUTER);
        serviceEventView.setDetail("downstream_service：" + eventData.getDownstream());
        serviceEventView.setQuality(eventData.getAdditionalMsg().get("detail"));
        return serviceEventView;
    }

    public static ServiceEventView parseAuthEvent(FemasEventData eventData, ServiceEventView serviceEventView) {
        serviceEventView.setEventType(AUTH);
        serviceEventView.setDetail("upstream_service：" + eventData.getUpstream());
        String type = "";
        switch (eventData.getAdditionalMsg().get("type")) {
            case "B":
                type = "black list";
                break;
            case "W":
                type = "white list";
                break;
            default:
        }
        serviceEventView.setQuality(type + " intercept");
        return serviceEventView;
    }

    public static ServiceEventView parseRateLimitEvent(FemasEventData eventData, ServiceEventView serviceEventView) {
        serviceEventView.setEventType(RATELIMIT);
        serviceEventView.setDetail("upstream_service：" + eventData.getUpstream());
        serviceEventView.setQuality("qps overflow");
        // serviceEventView.setQuality(eventData.getAdditionalMsg().get("type") + " intercept");
        return serviceEventView;
    }
}
