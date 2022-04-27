/*
 * Tencent is pleased to support the open source community by making Femas available.
 *
 * Copyright (C) 2021, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.tencent.tsf.femas.common.context.factory;

import com.tencent.tsf.femas.agent.classloader.AgentClassLoader;
import com.tencent.tsf.femas.agent.classloader.InterceptorClassLoaderCache;
import com.tencent.tsf.femas.common.context.AgentConfig;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.spi.SpiService;

import java.util.Iterator;
import java.util.ServiceLoader;

import static com.tencent.tsf.femas.common.context.ContextConstant.START_AGENT_FEMAS;

/**
 * <pre>
 * 文件名称：ContextFactory.java
 * 创建时间：Jul 29, 2021 5:50:34 PM
 * @author juanyinyang
 * 类说明：
 */
public class ContextFactory {

    public static Context getContextInstance() {
        return ContextFactoryHolder.contextInstance;
    }

    public static ContextConstant getContextConstantInstance() {
        return ContextFactoryHolder.contextConstantInstance;
    }

    private static class ContextFactoryHolder {

        static Context contextInstance = null;
        static ContextConstant contextConstantInstance = null;

        static {
            //spi加载器加载不到agent class的问题
            if (AgentConfig.doGetProperty(START_AGENT_FEMAS) != null && (Boolean) AgentConfig.doGetProperty(START_AGENT_FEMAS)) {
                AgentClassLoader agentClassLoader;
                try {
                    agentClassLoader = InterceptorClassLoaderCache.getAgentClassLoader(ContextFactory.class.getClassLoader());
                } catch (Exception e) {
                    agentClassLoader = InterceptorClassLoaderCache.getAgentClassLoader(Thread.currentThread().getContextClassLoader());
                }
                Thread.currentThread().setContextClassLoader(agentClassLoader);
            }
            // SPI加载并初始化实现类
            ServiceLoader<Context> contextServiceLoader = ServiceLoader.load(Context.class);
            Iterator<Context> contextIterator = contextServiceLoader.iterator();
            // 一般就一个实现类，如果有多个，那么加载的是最后一个
            while (contextIterator.hasNext()) {
                contextInstance = contextIterator.next();
            }
            // SPI加载并初始化实现类
            ServiceLoader<ContextConstant> constantServiceLoader = ServiceLoader.load(ContextConstant.class);
            Iterator<ContextConstant> constantIterator = constantServiceLoader.iterator();
            // 一般就一个实现类，如果有多个，那么加载的是最后一个
            while (constantIterator.hasNext()) {
                contextConstantInstance = constantIterator.next();
            }
        }
    }

}
  