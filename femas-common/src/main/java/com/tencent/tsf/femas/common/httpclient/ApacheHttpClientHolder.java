package com.tencent.tsf.femas.common.httpclient;

import com.tencent.tsf.femas.common.httpclient.client.AbstractHttpClient;
import com.tencent.tsf.femas.common.httpclient.factory.ApacheDefaultHttpClientFactory;
import com.tencent.tsf.femas.common.httpclient.factory.HttpClientFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/29 11:14
 * @Version 1.0
 */
public class ApacheHttpClientHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApacheHttpClientHolder.class);

    /**
     * key:http配置信息
     * value:根据配置初始化的client实例
     * 每种场景下的client配置不一样，read con timeout ，统一在此处管理
     */
    private static final Map<String, AbstractHttpClient> CLIENT_POOL = new ConcurrentHashMap<>(10);

    /**
     * shutdown hook是否触发
     */
    private static final AtomicBoolean isShutdown = new AtomicBoolean(false);

    static {
        Thread shutdownThread = new Thread(() -> {
            LOGGER.info("Shutting down apache http client  Pool for ApacheHttpClientHolder");
            shutdown();
        });
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }

    public static AbstractHttpClient getHttpClient() {
        return getHttpClient(new ApacheDefaultHttpClientFactory());
    }

    public static AbstractHttpClient getHttpClient(HttpClientFactory httpClientFactory) {
        if (httpClientFactory == null) {
            throw new NullPointerException("httpClientFactory is null");
        }
        String factoryName = httpClientFactory.getClass().getName();
        AbstractHttpClient httpClient = CLIENT_POOL.get(factoryName);
        if (httpClient == null) {
            synchronized (CLIENT_POOL) {
                httpClient = CLIENT_POOL.get(factoryName);
                if (httpClient != null) {
                    return httpClient;
                }
                httpClient = httpClientFactory.createHttpClient();
                CLIENT_POOL.put(factoryName, httpClient);
            }
        }
        return httpClient;
    }

    private static void shutdown() {
        if (isShutdown.compareAndSet(false, true)) {
            try {
                for (Map.Entry<String, AbstractHttpClient> map : CLIENT_POOL.entrySet()) {
                    map.getValue().close();
                }
                CLIENT_POOL.clear();
            } catch (Exception ex) {
                LOGGER.error("apacheHttpClientHolder shutdown http client error", ex);
            }
        }
    }

    public static void shutdown(String key) throws Exception {
        shutdownApacheHttpClient(key);
    }

    public static void shutdown(HttpClientFactory factory) throws Exception {
        shutdownApacheHttpClient(factory.getClass().getName());
    }

    public static void shutdownApacheHttpClient(String key) throws Exception {
        final AbstractHttpClient client = CLIENT_POOL.get(key);
        if (client != null) {
            client.close();
            CLIENT_POOL.remove(key);
        }
    }

}
