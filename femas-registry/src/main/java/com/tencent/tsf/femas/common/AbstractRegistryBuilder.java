package com.tencent.tsf.femas.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/23 20:05
 * @Version 1.0
 */
public abstract class AbstractRegistryBuilder<T> implements RegistryBuilder {

    private static final  Logger log = LoggerFactory.getLogger(AbstractRegistryBuilder.class);

    private final Map<String, T> registryMap = new ConcurrentHashMap<>();

    @Override
    public T certificateClient(Supplier serverAddressSupplier, String namespace, Object... var) {
        return null;
    }

    @Override
    public T build(Supplier serverAddressSupplier, String namespace) {
        return null;
    }

    @Override
    public T describeClient(Supplier serverAddressSupplier, String namespace, boolean certificate, Object... var) {
        String key = getKey(serverAddressSupplier);
        registryMap.computeIfAbsent(key, rk -> {
            try {
                if (certificate && var != null) {
                    return certificateClient(serverAddressSupplier, namespace, var);
                } else {
                    return build(serverAddressSupplier, namespace);
                }
            } catch (Exception e) {
                log.error(String.format("build {} Registry failed"), e);
                return null;
            }
        });
        return registryMap.get(key);
    }

}
