package com.tencent.tsf.femas.agent.classloader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * JVM委派机制
 *
 * 返回对应的AgentClassLoader
 *
 * femas-agent-plugin包中的Interceptor虽然是由AgentClassLoader所加载，但实际的拦截方法里却引用了类似Fegin、Ribbon等相关的类，这是AgentClassLoader
 * 所无法加载的，因此需要委托给其上层类加载器，其上层类加载器的选择不可随便指定为AppClassLoader，因为类似LoadBalancerFeignClientInterceptor中的
 *     feign.Request request = (feign.Request) allArguments[0];
 * Feign.Request并不一定是由AppClassLoader所加载，可能是由Tomcat相关的类加载器所加载，因此如果直接把AppClassLoader设为AgentCLassLoader的父类加载器，
 * 则就算AppClassLoader可以加载，=两边是由不同类加载器加载的类，无法完成转换（类加载器和带包的类名决定类的唯一性）。因此需要传入执行到对应拦截方法时
 * 的对应类加载器作为父类加载器，以此完成转换。而由于每个父类加载可能不同，因此针对每个父类加载器都有个AgentClassLoader,从而衍生出这个Map。
 */
public class ClassLoaderCache {
    private static volatile Map<ClassLoader, AgentClassLoader> agentClassLoaderMap = new ConcurrentHashMap<>();

    public static AgentClassLoader getAgentClassLoader(ClassLoader classLoader) {
        if (!agentClassLoaderMap.containsKey(classLoader)) {
            synchronized (agentClassLoaderMap) {
                if (!agentClassLoaderMap.containsKey(classLoader)) {
                    AgentClassLoader agentClassLoader = new AgentClassLoader(classLoader);
                    agentClassLoaderMap.put(classLoader, agentClassLoader);
                }
            }
        }
        return agentClassLoaderMap.get(classLoader);
    }

}
