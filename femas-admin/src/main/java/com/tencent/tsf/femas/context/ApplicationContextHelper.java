package com.tencent.tsf.femas.context;

import com.tencent.tsf.femas.event.ConfigUpdateEvent;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextHelper implements ApplicationContextAware, ApplicationListener<ConfigUpdateEvent> {

    private static ApplicationContext applicationContext;

    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextHelper.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ConfigUpdateEvent configUpdateEvent) {

    }
}
