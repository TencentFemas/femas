package com.tencent.tsf.femas.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/23 20:05
 * @Version 1.0
 */
public abstract class AbstractRegistryBuilder<T> implements RegistryBuilder<T> {

    private static final Logger log = LoggerFactory.getLogger(AbstractRegistryBuilder.class);

    private final Map<String, T> registryMap = new ConcurrentHashMap<>();

    @Override
    public T certificateClient(Supplier<String> serverAddressSupplier, String namespace, Object... args) {
        return null;
    }

    @Override
    public T build(Supplier<String> serverAddressSupplier, String namespace) {
        return null;
    }

    @Override
    public T describeClient(Supplier<String> serverAddressSupplier, String namespace, boolean certificate, Object... args) {
        String key = getKey(serverAddressSupplier);
        registryMap.computeIfAbsent(key, rk -> {
            try {
                if (certificate && args != null) {
                    return certificateClient(serverAddressSupplier, namespace, args);
                } else {
                    return build(serverAddressSupplier, namespace);
                }
            } catch (Exception e) {
                log.error("build Registry failed:{0}", e);
                return null;
            }
        });
        return registryMap.get(key);
    }

}
