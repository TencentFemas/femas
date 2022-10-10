package com.tencent.tsf.femas.event;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author mroccyen
 */
@SuppressWarnings("all")
@Component
public class ConfigUpdateEventDispatcher implements ApplicationListener<ConfigUpdateEvent>, InitializingBean {

    private final ApplicationContext applicationContext;

    private List<ConfigDataChangedListener> listeners;

    public ConfigUpdateEventDispatcher(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ConfigUpdateEvent event) {
        for (ConfigDataChangedListener listener : listeners) {
            listener.onChanged(event.getKey(), event.getSource());
        }
    }

    @Override
    public void afterPropertiesSet() {
        Collection<ConfigDataChangedListener> listenerBeans = applicationContext.getBeansOfType(ConfigDataChangedListener.class).values();
        this.listeners = Collections.unmodifiableList(new ArrayList<>(listenerBeans));
    }
}
