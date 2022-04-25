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

package com.tencent.tsf.femas.governance.plugin;


import com.tencent.tsf.femas.agent.classloader.AgentClassLoader;
import com.tencent.tsf.femas.agent.classloader.InterceptorClassLoaderCache;
import com.tencent.tsf.femas.common.context.AgentConfig;
import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.governance.plugin.context.AbstractSDKContext;
import com.tencent.tsf.femas.governance.plugin.context.ConfigContext;
import com.tencent.tsf.femas.governance.plugin.context.ConfigRefreshableContext;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.tencent.tsf.femas.common.context.ContextConstant.START_AGENT_FEMAS;

/**
 * @Author leoziltong
 * @Date: 2021/5/25 19:11
 */
public class DefaultConfigurablePluginHolder {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultConfigurablePluginHolder.class);

    private static volatile ConfigRefreshableContext context;


    /**
     * 标识别是否初始化完成，避免并发的情况下暴露还未初始化完的对象
     * 0 代表 未初始化
     * 1 代表 已经初始化
     * -1 代表 初始化失败
     */
    private static volatile int init = 0;

    private static final Lock LOCK = new ReentrantLock();

    public static AbstractSDKContext getSDKContext() {
        if (context == null) {
            LOCK.lock();
            try {
                if (context == null) {
                    context = new ConfigRefreshableContext();
                    initPluginContext();
                    init = InitStatus.INIT_SUCCESS.getStatus();
                }
            } catch (Exception e) {
                init = InitStatus.ERROR.getStatus();
                throw e;
            } finally {
                LOCK.unlock();
            }

        }
        while (init != InitStatus.INIT_SUCCESS.getStatus()) {
            LOCK.lock();
            try {
                if (init == InitStatus.ERROR.getStatus()) {
                    throw new RuntimeException("initPluginContext error");
                }
            } finally {
                LOCK.unlock();
            }
        }
        return context;
    }

    /**
     * 通过配置对象初始化SDK上下文
     *
     * @throws FemasRuntimeException 初始化过程的异常
     */
    public static void initPluginContext() throws FemasRuntimeException {
        ConfigContext initContext = null;
        //插件具体配置
        if (AgentConfig.doGetProperty(START_AGENT_FEMAS) != null && (Boolean) AgentConfig.doGetProperty(START_AGENT_FEMAS)) {
            AgentClassLoader agentClassLoader = InterceptorClassLoaderCache.getAgentClassLoader(Thread.currentThread().getContextClassLoader());
            Thread.currentThread().setContextClassLoader(agentClassLoader);
        }
        ServiceLoader<ConfigProvider> configProviders = ServiceLoader.load(ConfigProvider.class);
        //插件列表
        ServiceLoader<PluginProvider> providers = ServiceLoader.load(PluginProvider.class);
        List<Class<? extends Plugin>> types = new ArrayList<>();
        for (PluginProvider provider : providers) {
            Attribute attribute = provider.getAttr();
            for (ConfigProvider configProvider : configProviders) {
                if (attribute.getType().name().equalsIgnoreCase(configProvider.getAttr().getType().name())) {
                    initContext = configProvider.getPluginConfigs();
                }
            }
            types.addAll(provider.getPluginTypes());
        }
        if (initContext == null) {
            throw new FemasRuntimeException("init Plugin Context failed，please check your plugin config");
        }
        initContext.setFactory(context);
        context.initPlugins(initContext, types);
    }

    private enum InitStatus {

        /**
         * 初始化失败
         */
        ERROR(-1),
        /**
         * 未初始化
         */
        UN_INIT(0),
        /**
         * 已经初始化
         */
        INIT_SUCCESS(1);

        private int status;

        InitStatus(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    }
}
