package com.tencent.tsf.femas.config.internals;

import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.common.util.NamedThreadFactory;
import com.tencent.tsf.femas.config.Config;
import com.tencent.tsf.femas.config.ConfigChangeListener;
import com.tencent.tsf.femas.config.model.ConfigChangeEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author zhixinzxliu
 */
public abstract class AbstractConfig<T> implements Config<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConfig.class);

    private static final ExecutorService notifyExecutor;

    static {
        notifyExecutor = Executors.newCachedThreadPool(new NamedThreadFactory("Femas-config-thread", true));
    }

    private volatile Map<String, Set<ConfigChangeListener>> keyListenerMap = Maps.newConcurrentMap();
    private volatile Cache<String, T> cache = newCache();

    @Override
    public synchronized boolean publishConfig(Object[] params) {
        LOGGER.debug("Ready to publishConfig, params:{}", params);
        return true;
    }

    @Override
    public synchronized void subscribe(String key, ConfigChangeListener<T> listener) {
        if (StringUtils.isEmpty(key)) {
            return;
        }

        if (!keyListenerMap.containsKey(key)) {
            keyListenerMap.put(key, Sets.newConcurrentHashSet());
            doSubscribe(key);
        }

        if (listener != null) {
            keyListenerMap.get(key).add(listener);
        }
    }

    @Override
    public synchronized void subscribeDirectory(String key, ConfigChangeListener<T> listener) {
        if (StringUtils.isEmpty(key)) {
            return;
        }

        if (!keyListenerMap.containsKey(key)) {
            keyListenerMap.put(key, Sets.newConcurrentHashSet());
            doSubscribeDirectory(key);
        }

        if (listener != null) {
            keyListenerMap.get(key).add(listener);
        }
    }


    protected abstract void doSubscribe(String key);

    protected abstract void doSubscribeDirectory(String key);

    public List<ConfigChangeEvent<T>> getDirectory(String key) {
        throw new UnsupportedOperationException("method getDirectory has no implementation");
    }

    protected void fireValueChange(String key, ConfigChangeEvent<T> changeEvent) {
        LOGGER.debug("Ready to fire change event. Key : " + key);
        if (keyListenerMap.containsKey(key)) {
            notifyExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    for (ConfigChangeListener listener : keyListenerMap.get(key)) {
                        listener.onChange(changeEvent);
                    }
                }
            });
        }
    }

    protected void fireDirectoryChange(String key, List<ConfigChangeEvent<T>> changeEvents) {
        if (CollectionUtil.isEmpty(changeEvents)) {
            return;
        }

        LOGGER.info("Ready to fire change events. Path : " + key);
        if (keyListenerMap.containsKey(key)) {
            notifyExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    for (ConfigChangeListener listener : keyListenerMap.get(key)) {
                        listener.onChange(changeEvents);
                    }
                }
            });
        }
    }

    @Override
    public synchronized void unsubscribe(String key) {
        if (StringUtils.isEmpty(key)) {
            return;
        }

        if (keyListenerMap.containsKey(key)) {
            keyListenerMap.remove(key);
            doUnSubscribe(key);
        }
    }

    @Override
    public synchronized void unsubscribe(String key, ConfigChangeListener listener) {
        if (StringUtils.isEmpty(key)) {
            return;
        }

        if (keyListenerMap.containsKey(key)) {
            keyListenerMap.get(key).remove(listener);
        }

        if (keyListenerMap.get(key).isEmpty()) {
            doUnSubscribe(key);
        }
    }

    protected abstract void doUnSubscribe(String key);

    public T getProperty(String key, T defaultValue) {
        try {
            T value = cache.get(key, new Callable<T>() {
                @Override
                public T call() throws Exception {
                    return doGetProperty(key);
                }
            });

            return value;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    protected abstract T doGetProperty(String key);

    public <V> V getProperty(String key, Function<T, V> function, V defaultValue) {
        try {
            T value = getProperty(key, null);

            if (value != null) {
                return function.apply(value);
            }
        } catch (Throwable ex) {
            LOGGER.error("getProperty for %s failed, return default value %s", key, defaultValue);
        }

        return defaultValue;
    }

    /**
     * 子类可以自行覆盖
     * 增加config的配置
     *
     * @param <T>
     * @return
     */
    protected <T> Cache<String, T> newCache() {
        Cache<String, T> cache = CacheBuilder.newBuilder()
                .maximumSize(500)
                .expireAfterAccess(60, TimeUnit.SECONDS)
                .build();
        return cache;
    }

    /**
     * Clear config cache
     */
    protected void clearConfigCache() {
        synchronized (this) {
            cache.cleanUp();
        }
    }
}
