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


import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.governance.plugin.context.AbstractSDKContext;
import com.tencent.tsf.femas.governance.plugin.context.ConfigContext;
import com.tencent.tsf.femas.governance.plugin.context.ConfigRefreshableContext;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author leoziltong
 * @Date: 2021/5/25 19:11
 */
public class DefaultConfigurablePluginHolder {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultConfigurablePluginHolder.class);

    private static volatile ConfigRefreshableContext context;


    /**
     * 标识别是否初始化完成，避免并发的情况下暴露还未初始化完的对象
     */
    private static volatile boolean init = false;

    public static AbstractSDKContext getSDKContext() {
        if (context == null) {
            synchronized (DefaultConfigurablePluginHolder.class) {
                if (context == null) {
                    context = new ConfigRefreshableContext();
                    initPluginContext();
                    init = true;
                }
            }
        }
        while (!init){
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {

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

}
