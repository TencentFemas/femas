package com.tencent.tsf.femas.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * @author mroccyen
 */
@Component
public class EventPublishCenter {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventPublishCenter.class);

    private final ApplicationEventPublisher eventPublisher;

    public EventPublishCenter(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publishEvent(final ApplicationEvent event) {
        try {
            eventPublisher.publishEvent(event);
        } catch (Throwable ex) {
            LOGGER.error("There was an exception to the message publish application event: ", ex);
        }
    }

    public void publishEvent(final Object event) {
        try {
            eventPublisher.publishEvent(event);
        } catch (Throwable ex) {
            LOGGER.error("There was an exception to the message publish custom event: ", ex);
        }
    }
}
