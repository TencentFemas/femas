package com.tencent.tsf.femas.agent.tools;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class CacheProxy {
    private static final String CrossClassLoaderCacheClassName = "com.tencent.tsf.femas.agent.classloader.CrossClassLoaderCache.CrossClassLoaderCache";

    public static Map<String, Object> getCache(String cacheKey) {
        return (Map<String, Object>) ReflectionUtils.invokeStaticMethod(CrossClassLoaderCacheClassName, "getCache", cacheKey);
    }

    public static Object getCacheObject(String cacheKey, String subKey) {
        return ReflectionUtils.invokeStaticMethod(CrossClassLoaderCacheClassName, "getCacheObject", cacheKey, subKey);
    }

    public static void putCache(String cacheKey, Map<String, Object> map) {
        ReflectionUtils.invokeStaticMethod(CrossClassLoaderCacheClassName, "putCache", cacheKey, map);
    }

    public static void putObject(String cacheKey, String subKey, Object object) {
        ReflectionUtils.invokeStaticMethod(CrossClassLoaderCacheClassName, "putObject", cacheKey, subKey, object);
    }

    public static Set<Object> getCacheSet(String cacheKey) {
        return (Set<Object>) ReflectionUtils.invokeStaticMethod(CrossClassLoaderCacheClassName, "getCacheSet", cacheKey);
    }

    private static Method addMethod;
    private static Method removeMethod;
    private static Method containsMethod;

    public static void addCacheSet(String cacheKey, Object value) {
        try {
            if (addMethod == null){
                Class clazz = Thread.currentThread().getContextClassLoader().loadClass(CrossClassLoaderCacheClassName);
                addMethod = clazz.getMethod("addCacheSet",String.class,Object.class);
            }
            addMethod.invoke(null,cacheKey,value);
        }catch (Exception e){
           AgentLogger.getLogger().info(AgentLogger.getStackTraceString(e));
        }
    }

    public static void removeCacheSet(String cacheKey, Object value) {
        try {
            if (removeMethod == null){
                Class clazz = Thread.currentThread().getContextClassLoader().loadClass(CrossClassLoaderCacheClassName);
                removeMethod = clazz.getMethod("removeCacheSet",String.class,Object.class);
            }
            removeMethod.invoke(null,cacheKey,value);
        }catch (Exception e){
            AgentLogger.getLogger().info(AgentLogger.getStackTraceString(e));
        }
    }

    public static boolean isSetContains(String cacheKey, Object value) {
        boolean contains = false;
        try {
            if (containsMethod == null){
                Class clazz = Thread.currentThread().getContextClassLoader().loadClass(CrossClassLoaderCacheClassName);
                containsMethod = clazz.getMethod("isSetContains",String.class,Object.class);
            }
            contains = (boolean)containsMethod.invoke(null,cacheKey,value);
        }catch (Exception e){
            AgentLogger.getLogger().info(AgentLogger.getStackTraceString(e));
        }
        return contains;
    }
}
