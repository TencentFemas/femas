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

import com.tencent.tsf.femas.agent.config.InterceptPluginLoader;
import com.tencent.tsf.femas.agent.interceptor.InterceptorWrapper;
import com.tencent.tsf.femas.agent.logger.AgentLogger;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2022/3/29 15:18
 */
public class FemasAgent {
    public static void premain(String agentArgs, Instrumentation inst) {
        try {
            final ByteBuddy byteBuddy = new ByteBuddy().with(TypeValidation.of(TypeValidation.DISABLED.isEnabled()));
            AgentBuilder agentBuilder = new AgentBuilder.Default(byteBuddy);
            for (InterceptPluginLoader plugin : InterceptPluginLoader.getConfigs()) {
                agentBuilder = agentBuilder.type(ElementMatchers.named(plugin.getClassName())).transform((builder, typeDescription, classLoader, module) -> {
                    //按照参数长度处理匹配重载方法
                    if (plugin.getTakesArguments() != null) {
                        builder = builder.method(ElementMatchers.named(plugin.getMethodName()).and(ElementMatchers.takesArguments(plugin.getTakesArguments())))
                                .intercept(MethodDelegation.to(new InterceptorWrapper(plugin.getInterceptorClass())));
                    } else {
                        builder = builder.method(ElementMatchers.named(plugin.getMethodName()))
                                .intercept(MethodDelegation.to(new InterceptorWrapper(plugin.getInterceptorClass())));
                    }
                    return builder;
                });
            }
//            if(agentArgs == null || Boolean.valueOf(agentArgs)) {
//                inst.addTransformer(new FemasTransformer(), true);
//            }
            agentBuilder.with(new Listener()).installOn(inst);
        } catch (Throwable throwable) {
            AgentLogger.getLogger().severe("install agent exception: " + AgentLogger.getStackTraceString(throwable));
        } finally {
            AgentLogger.getLogger().info("femas agent install on finally !!!!!!");
        }
    }

    private static class Listener implements AgentBuilder.Listener {
        @Override
        public void onDiscovery(String s, ClassLoader classLoader, JavaModule javaModule, boolean b) {
        }

        @Override
        public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b, DynamicType dynamicType) {
            AgentLogger.getLogger().info("On Transformation class {}." + typeDescription.getName());
        }

        @Override
        public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b) {
        }

        @Override
        public void onError(String s, ClassLoader classLoader, JavaModule javaModule, boolean b, Throwable throwable) {
            AgentLogger.getLogger().severe("Enhance class: " + s + " error." + AgentLogger.getStackTraceString(throwable));
        }

        @Override
        public void onComplete(String s, ClassLoader classLoader, JavaModule javaModule, boolean b) {
        }
    }


}
