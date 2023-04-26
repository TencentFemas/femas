/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tencent.tsf.femas.agent;

import java.lang.instrument.Instrumentation;
import java.security.AllPermission;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import com.tencent.tsf.femas.agent.config.*;
import com.tencent.tsf.femas.agent.interceptor.wrapper.*;
import com.tencent.tsf.femas.agent.tools.AgentLogger;
import com.tencent.tsf.femas.agent.tools.JvmRuntimeInfo;
import com.tencent.tsf.femas.agent.transformer.CompositeTransformer;
import com.tencent.tsf.femas.agent.transformer.async.transformer.ExecutorTransformer;
import com.tencent.tsf.femas.agent.transformer.async.transformer.ForkJoinTransformer;
import com.tencent.tsf.femas.agent.transformer.async.transformer.TimerTaskTransformer;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.apache.commons.lang3.StringUtils;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2022/3/29 15:18
 */
public class FemasAgent {

    private static final AgentLogger LOG = AgentLogger.getLogger(FemasAgent.class);

    private static ResettableClassFileTransformer rct;
    private static final String TARGET_JAR = "targetJar";
    private static final String ACTIVATE_CROSS_THREAD_TRANSFORMER = "activateCrossThread";

    public static void premain(String agentArgs, Instrumentation inst) {
        init(agentArgs, inst, true);
    }

    /**
     * agent 监听器
     */
    private static class Listener implements AgentBuilder.Listener {
        @Override
        public void onDiscovery(String s, ClassLoader classLoader, JavaModule javaModule, boolean b) {
        }

        @Override
        public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b, DynamicType dynamicType) {
            LOG.info("[femas-agent] On Transformation class :" + typeDescription.getName());
        }

        @Override
        public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b) {
        }

        @Override
        public void onError(String s, ClassLoader classLoader, JavaModule javaModule, boolean b, Throwable throwable) {
            LOG.error(" [femas-agent] Enhance class: " + s + " error.", throwable);
        }

        @Override
        public void onComplete(String s, ClassLoader classLoader, JavaModule javaModule, boolean b) {
        }
    }

    public synchronized static void init(String agentArg, Instrumentation instrumentation, boolean premain) {
        securityManagerCheck();
        List<Map<String, String>> mapList = parseArgs(agentArg);
        AtomicReference<String> crossThread = new AtomicReference<>();
        if (mapList != null && mapList.size() > 0) {
            mapList.stream().forEach(m -> {
                String targetJarPath = m.get(TARGET_JAR);
                if (StringUtils.isNotEmpty(targetJarPath)) {
                    setAgentContext(targetJarPath);
                }
                crossThread.set(m.get(ACTIVATE_CROSS_THREAD_TRANSFORMER));
            });
        }
        long delayInitMs = -1L;
        String delayAgentInitMsProperty = System.getProperty("delay_agent_premain_ms");
        if (delayAgentInitMsProperty != null) {
            try {
                delayInitMs = Long.parseLong(delayAgentInitMsProperty.trim());
            } catch (NumberFormatException numberFormatException) {
                LOG.info("[femas-agent] WARN The value of the delay_agent_premain_ms must be a number");
            }
        }
        if (premain && shouldDelayOnPremain()) {
            delayInitMs = Math.max(delayInitMs, 3000L);
        }
        if (delayInitMs > 0) {
            delayInitAgentAsync(crossThread.get(), instrumentation, premain, delayInitMs);
        } else {
            String startAgentAsyncProperty = System.getProperty("agent.start.async");
            if (startAgentAsyncProperty != null) {
                delayInitAgentAsync(crossThread.get(), instrumentation, premain, 0);
            } else {
                initializeAgent(crossThread.get(), instrumentation, premain);
            }
        }
    }

    /**
     * Returns whether agent initialization should be delayed when occurring through the {@code premain} route.
     * This works around a JVM bug (https://bugs.openjdk.java.net/browse/JDK-8041920) causing JIT fatal error if
     * agent code causes the loading of MethodHandles prior to JIT compiler initialization.
     *
     * @return {@code true} for any Java 7 and early Java 8 HotSpot JVMs, {@code false} for all others
     */
    static boolean shouldDelayOnPremain() {
        JvmRuntimeInfo runtimeInfo = JvmRuntimeInfo.ofCurrentVM();
        int majorVersion = runtimeInfo.getMajorVersion();
        return
                (majorVersion == 7) ||
                        // In case bootstrap checks were disabled
                        (majorVersion == 8 && runtimeInfo.isHotSpot() && runtimeInfo.getUpdateVersion() < 2) ||
                        (majorVersion == 8 && runtimeInfo.isHotSpot() && runtimeInfo.getUpdateVersion() < 40);
    }

    /**
     * 延迟初始化agent
     *
     * @param agentArguments
     * @param instrumentation
     * @param premain
     * @param delayAgentInitMs
     */
    private static void delayInitAgentAsync(final String agentArguments, final Instrumentation instrumentation,
                                            final boolean premain, final long delayAgentInitMs) {
        LOG.info("[femas-agent] INFO Delaying  Agent initialization by " + delayAgentInitMs + " milliseconds.");
        Thread initThread = new Thread("[femas-agent] initialization thread") {
            @Override
            public void run() {
                try {
                    synchronized (FemasAgent.class) {
                        if (delayAgentInitMs > 0) {
                            Thread.sleep(delayAgentInitMs);
                        }
                        initializeAgent(agentArguments, instrumentation, premain);
                    }
                } catch (InterruptedException e) {
                    LOG.info("[femas-agent] ERROR " + getName() + " thread was interrupted, the agent will not be attached to this JVM.");
                    e.printStackTrace();
                } catch (Throwable throwable) {
                    LOG.info("[femas-agent] ERROR  Agent initialization failed: " + throwable.getMessage());
                    throwable.printStackTrace();
                }
            }
        };
        initThread.setDaemon(true);
        initThread.start();
    }

    static void setAgentContext(String arg) {
        String dirs = MechaRuntimeModule.getPluginModuleByTag(arg);
        AgentContext.setAvailablePluginsDir(dirs);
    }

    static List<Map<String, String>> parseArgs(String arg) {
        List<Map<String, String>> mapList = new ArrayList<>();
        if (StringUtils.isEmpty(arg)) {
            LOG.warn("[femas-agent-starter] no agent starter args present...");
            return mapList;
        }

        try {
            String[] pairs = arg.split(",");
            for (String a :
                    pairs) {
                String[] pairsKeys = a.split("=");
                Map<String, String> map = new HashMap<String, String>(2);
                map.put(pairsKeys[0], pairsKeys[1]);
                mapList.add(map);
            }
        } catch (Exception e) {
            LOG.error("[femas-agent-starter] parse premain Args failed", e);
        }

        return mapList;
    }

    /**
     * 初始化agent
     *
     * @param agentArguments
     * @param instrumentation
     * @param premain
     */
    private synchronized static void initializeAgent(String agentArguments, Instrumentation instrumentation, boolean premain) {
        try {
            final ByteBuddy byteBuddy = new ByteBuddy().with(TypeValidation.of(TypeValidation.DISABLED.isEnabled()));
            AgentBuilder agentBuilder = new AgentBuilder.Default(byteBuddy)
                    .ignore(agentIgnoreElement()
                            //忽略编译器自动生成的方法
                            .or(ElementMatchers.isSynthetic()));
            for (InterceptPluginConfig plugin : MechaRuntimePluginsSniffer.sniffRuntimeAvailablePlugins()) {
                InterceptPlugin interceptPlugin = plugin.getPlugin();
                agentBuilder = pluginAgentBuilder(agentBuilder, interceptPlugin);
            }
            if (ACTIVATE_CROSS_THREAD_TRANSFORMER.equalsIgnoreCase(agentArguments)) {
                instrumentation.addTransformer(new CompositeTransformer(new ExecutorTransformer(), new TimerTaskTransformer(), new ForkJoinTransformer()), true);
            }
            rct = agentBuilder.with(new Listener()).installOn(instrumentation);
//            rft.reset(instrumentation, AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);
        } catch (Throwable throwable) {
            LOG.error("[femas-agent] install agent exception: ", throwable);
        } finally {
            LOG.info("[femas-agent] install on instrumentation success !!!!!!");
        }
    }

    /**
     * 组装transformer
     * 这里可以根据不同拦截的类型在Plugin做统一封装，放在一个新的transformer里面，第一期为了让读者更清晰直观，先这样写吧 TODO
     *
     * @param agentBuilder
     * @param interceptPlugin
     * @return
     */
    private static AgentBuilder pluginAgentBuilder(AgentBuilder agentBuilder, InterceptPlugin interceptPlugin) {
        if (!validateInterceptPlugin(interceptPlugin))
            return agentBuilder;
        //原始的改写方式
        if (interceptPlugin.getOriginAround() != null && interceptPlugin.getOriginAround()) {
            agentBuilder = agentBuilder.type(ElementMatchers.named(interceptPlugin.getClassName())).transform((builder, typeDescription, classLoader, module) -> {
                ElementMatcher.Junction<MethodDescription> junction = not(isStatic()).and(interceptPlugin.getPluginMatcher());
                builder = builder.method(junction)
                        .intercept(MethodDelegation.withDefaultConfiguration()
                                .to(new OriginalInterceptorWrapper(interceptPlugin
                                        .getInterceptorClass(), classLoader)));
                return builder;
            });
            return agentBuilder;
        }
        //改写构造方法
        if (MethodType.CONSTRUCTOR.getType().equalsIgnoreCase(interceptPlugin.getMethodType())) {
            agentBuilder = agentBuilder.type(ElementMatchers.named(interceptPlugin.getClassName())).transform((builder, typeDescription, classLoader, module) -> {
                builder = builder.constructor(interceptPlugin.getPluginMatcher())
                        .intercept(SuperMethodCall.INSTANCE.andThen(MethodDelegation.withDefaultConfiguration()
                                .to(new ConstructorInterceptorWrapper(interceptPlugin
                                        .getInterceptorClass(), classLoader))));
                return builder;
            });
            return agentBuilder;
        }
        //改写实例方法,默认是实例方法
        if (StringUtils.isEmpty(interceptPlugin.getMethodType()) || interceptPlugin.getMethodType().equalsIgnoreCase(MethodType.INSTANCE.getType())) {
            ElementMatcher.Junction<MethodDescription> junction = not(isStatic()).and(interceptPlugin.getPluginMatcher());
            if (interceptPlugin.getOverrideArgs() != null && interceptPlugin.getOverrideArgs()) {
                agentBuilder = agentBuilder.type(ElementMatchers.named(interceptPlugin.getClassName())).transform((builder, typeDescription, classLoader, module) -> {
                    builder = builder.method(junction)
                            .intercept(MethodDelegation.withDefaultConfiguration()
                                    .withBinders(Morph.Binder.install(OverrideArgsCallable.class))
                                    .to(new InstanceMethodsInterceptOverrideArgsWrapper(interceptPlugin
                                            .getInterceptorClass(), classLoader) {
                                    }));
                    return builder;
                });
            } else {
                agentBuilder = agentBuilder.type(ElementMatchers.named(interceptPlugin.getClassName())).transform((builder, typeDescription, classLoader, module) -> {
                    builder = builder.method(junction)
                            .intercept(MethodDelegation.withDefaultConfiguration()
                                    .to(new InstanceMethodsInterceptorWrapper(interceptPlugin
                                            .getInterceptorClass(), classLoader)));
                    return builder;
                });
            }
            return agentBuilder;
        }
        //改写静态方法
        if (MethodType.STATIC.getType().equalsIgnoreCase(interceptPlugin.getMethodType())) {
            ElementMatcher.Junction<MethodDescription> junction = isStatic().and(interceptPlugin.getPluginMatcher());
            if (interceptPlugin.getOverrideArgs() != null && interceptPlugin.getOverrideArgs()) {
                agentBuilder = agentBuilder.type(ElementMatchers.named(interceptPlugin.getClassName())).transform((builder, typeDescription, classLoader, module) -> {
                    builder = builder.method(junction)
                            .intercept(MethodDelegation.withDefaultConfiguration()
                                    .withBinders(Morph.Binder.install(OverrideArgsCallable.class))
                                    .to(new StaticMethodsInterceptOverrideArgsWrapper(interceptPlugin.getInterceptorClass())));
                    return builder;
                });
            } else {
                agentBuilder = agentBuilder.type(ElementMatchers.named(interceptPlugin.getClassName())).transform((builder, typeDescription, classLoader, module) -> {
                    builder = builder.method(junction)
                            .intercept(MethodDelegation.withDefaultConfiguration()
                                    .to(new StaticMethodsInterceptorWrapper(interceptPlugin.getInterceptorClass())));
                    return builder;
                });
            }
            return agentBuilder;
        }
        return agentBuilder;
    }

    private static boolean validateInterceptPlugin(InterceptPlugin plugin) {
        boolean classNameIsValid = Optional.of(plugin).map(i -> i.getClassName()).isPresent();
        boolean InterClassNameIsValid = Optional.of(plugin).map(i -> i.getInterceptorClass()).isPresent();
        boolean methodNameIsValid = Optional.of(plugin).map(i -> i.getMethodName()).isPresent();
        return classNameIsValid && InterClassNameIsValid && methodNameIsValid;
    }

    private static ElementMatcher.Junction<NamedElement> agentIgnoreElement() {
        //可以放在一个公共配置里面，拼接Junction
        return nameStartsWith("net.bytebuddy.")
                .or(nameContains("javassist"));
    }

    private static void securityManagerCheck() {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            return;
        }
        try {
            sm.checkPermission(new AllPermission());
        } catch (SecurityException e) {
            LOG.info("[femas-agent] WARN  permission java.security.AllPermission;");
        }
    }

}
