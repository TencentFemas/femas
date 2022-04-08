package com.tencent.tsf.femas.agent.config;

import java.util.ArrayList;
import java.util.List;

/**
 * 插件加载
 */
public class InterceptPluginLoader {

    private static List<InterceptPluginLoader> configs = new ArrayList<>();

    static {
        List<InterceptPluginLoader> list = AgentConfig.getInterceptConfig();
        if (list != null && list.size() > 0) {
            list.stream().forEach(item -> InterceptPluginLoader.getConfigs().add(item));
        }
    }

    public static void loadConfig(String file) {

    }

    public static List<InterceptPluginLoader> getConfigs() {
        return configs;
    }

    /**
     * 类名
     */
    private String className;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 匹配类型  MatcherType
     */
    private String matcherType;
    /**
     * 字节码拦截interceptor
     */
    private String interceptorClass;
    /**
     * 方法参数长度
     */
    private Integer takesArguments;

    public InterceptPluginLoader() {

    }

    public InterceptPluginLoader(String className, String methodName, String matcherType, String interceptorClass, Integer takesArguments) {
        this.className = className;
        this.methodName = methodName;
        this.matcherType = matcherType;
        this.interceptorClass = interceptorClass;
        this.takesArguments = takesArguments;
    }

    public String getMatcherType() {
        return matcherType;
    }

    public void setMatcherType(String matcherType) {
        this.matcherType = matcherType;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getInterceptorClass() {
        return interceptorClass;
    }

    public void setInterceptorClass(String interceptorClass) {
        this.interceptorClass = interceptorClass;
    }

    public Integer getTakesArguments() {
        return takesArguments;
    }

    public void setTakesArguments(Integer takesArguments) {
        this.takesArguments = takesArguments;
    }
}
