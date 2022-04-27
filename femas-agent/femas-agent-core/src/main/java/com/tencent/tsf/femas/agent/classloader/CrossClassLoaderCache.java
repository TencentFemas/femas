package com.tencent.tsf.femas.agent.classloader;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 跨类加载器缓存
 * <p>
 * femas-agent-plugin包的类虽然都由AgentClassLoader加载，但因为各个Interceptor拦截时机不一致，即所拦截的类不一致，其类加载器不一致。导致
 * 对应AgentClassLoader的父类加载器不一致，因此产生多个AgentClassLoader实例。当访问上下文时就不能做到上下文共享。如果想要做到上下文共享，
 * 就必须保证所有Interceptor访问的上下文由相同类加载器实例所加载。因此，所有plugin包中访问CrossClassLoader都通过反射访问Thread.currentThread().getContextClassLoader()
 * 所加载的上下文，以此达到目的。又因为Thread.currentThread().getContextClassLoader()默认获得的是AppClassLoader，所以将其放在femas-agent包中。
 *
 * @Author leoziltong@tencent.com
 */
public class CrossClassLoaderCache {
    private static Object lock = new Object();
    private static Map<String, Map<String, Object>> cache = new ConcurrentHashMap<>();

    private static Map<String, Set<Object>> cacheSet = new ConcurrentHashMap<>();

    public static Map<String, Object> getCache(String cacheKey) {
        return cache.get(cacheKey);
    }

    public static Object getCacheObject(String cacheKey, String subKey) {
        Map<String, Object> map = cache.get(cacheKey);
        if (map == null) {
            return null;
        } else {
            return map.get(subKey);
        }
    }

    public static void putCache(String cacheKey, Map<String, Object> map) {
        if (cacheKey != null) {
            cache.put(cacheKey, map);
        }
    }

    public static void putObject(String cacheKey, String subKey, Object object) {
        if (cacheKey != null && subKey != null) {
            if (cache.get(cacheKey) == null) {
                synchronized (lock) {
                    if (cache.get(cacheKey) == null) {
                        cache.put(cacheKey, new ConcurrentHashMap<>());
                    }
                }
            }
            cache.get(cacheKey).put(subKey, object);
        }
    }

    public static Set<Object> getCacheSet(String cacheKey) {
        return cacheSet.get(cacheKey);
    }

    public static void addCacheSet(String cacheKey, Object value) {
        Set<Object> set = cacheSet.get(cacheKey);
        if (cacheKey != null && set == null) {
            synchronized (cacheSet) {
                if (set == null) {
                    set = new CopyOnWriteArraySet<>();
                    cacheSet.put(cacheKey, set);
                }
            }
        }
        if (set != null) {
            set.add(value);
        }
    }

    public static void removeCacheSet(String cacheKey, Object value) {
        Set<Object> set = cacheSet.get(cacheKey);
        if (set != null) {
            set.remove(value);
        }
    }

    public static boolean isSetContains(String cacheKey, Object value) {
        Set<Object> set = cacheSet.get(cacheKey);
        if (set != null) {
            return set.contains(value);
        } else {
            return false;
        }
    }

}
