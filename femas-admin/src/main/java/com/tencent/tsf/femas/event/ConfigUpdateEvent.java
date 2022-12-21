package com.tencent.tsf.femas.event;

import org.springframework.context.ApplicationEvent;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/4/16 17:14
 * @Version 1.0
 */
public class ConfigUpdateEvent extends ApplicationEvent {

    private final String key;

    public ConfigUpdateEvent(String key, String updateData) {
        super(updateData);
        this.key = key;
    }

    @Override
    public String getSource() {
        return (String) super.getSource();
    }

    public String getKey() {
        return key;
    }
}
