package com.tencent.tsf.femas.config;

import com.google.common.base.Function;
import com.tencent.tsf.femas.config.model.ConfigChangeEvent;

import java.util.List;


public interface Config<T> {

    boolean publishConfig(Object[] params);

    T getProperty(String key, T defaultValue);

    <V> V getProperty(String key, Function<T, V> function, V defaultValue);

    void subscribe(String key, ConfigChangeListener<T> listener);

    /**
     * zk，consul，etcd等支持该API
     * Apollo，redis等不支持
     *
     * throws unsupport
     */
    void subscribeDirectory(String key, ConfigChangeListener<T> listener);

    List<ConfigChangeEvent<T>> getDirectory(String key);

    void unsubscribe(String key);

    void unsubscribe(String key, ConfigChangeListener listener);
}
