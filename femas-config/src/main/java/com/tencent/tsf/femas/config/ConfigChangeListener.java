package com.tencent.tsf.femas.config;

import com.tencent.tsf.femas.config.model.ConfigChangeEvent;
import java.util.List;

public interface ConfigChangeListener<T> {

    /**
     * Invoked when there is any config change for the namespace.
     *
     * @param changeEvents the events for this change
     */
    void onChange(List<ConfigChangeEvent<T>> changeEvents);

    /**
     * Invoked when there is any config change for the namespace.
     *
     * @param changeEvent the events for this change
     */
    void onChange(ConfigChangeEvent<T> changeEvent);
}
