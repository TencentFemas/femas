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

package com.tencent.tsf.femas.config;

import com.tencent.tsf.femas.agent.classloader.AgentClassLoader;
import com.tencent.tsf.femas.agent.classloader.InterceptorClassLoaderCache;
import com.tencent.tsf.femas.common.constant.FemasConstant;
import com.tencent.tsf.femas.common.context.AgentConfig;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.util.StringUtils;

import java.util.Iterator;
import java.util.ServiceLoader;

import static com.tencent.tsf.femas.common.context.ContextConstant.START_AGENT_FEMAS;

/**
 * <pre>
 * 文件名称：AbstractConfigHttpClientManagerFactory.java
 * 创建时间：Jul 29, 2021 5:50:34 PM
 * @author juanyinyang
 * 类说明：
 */
public class AbstractConfigHttpClientManagerFactory {

    public static AbstractConfigHttpClientManager getConfigHttpClientManager() {
        return AbstractConfigHttpClientManagerHolder.configHttpClientManager;
    }

    private static class AbstractConfigHttpClientManagerHolder {

        static AbstractConfigHttpClientManager configHttpClientManager = null;

        static {
            //spi加载器加载不到agent class的问题
            if (AgentConfig.doGetProperty(START_AGENT_FEMAS) != null && (Boolean) AgentConfig.doGetProperty(START_AGENT_FEMAS)) {
                AgentClassLoader agentClassLoader;
                try {
                    agentClassLoader = InterceptorClassLoaderCache.getAgentClassLoader(AbstractConfigHttpClientManagerFactory.class.getClassLoader());
                } catch (Exception e) {
                    agentClassLoader = InterceptorClassLoaderCache.getAgentClassLoader(Thread.currentThread().getContextClassLoader());
                }
                Thread.currentThread().setContextClassLoader(agentClassLoader);
            }
            // SPI加载并初始化实现类
            ServiceLoader<AbstractConfigHttpClientManager> configHttpClientManagerServiceLoader = ServiceLoader
                    .load(AbstractConfigHttpClientManager.class);
            Iterator<AbstractConfigHttpClientManager> configHttpClientManagerIterator = configHttpClientManagerServiceLoader
                    .iterator();

            String pollingType = FemasConfig.getProperty(FemasConstant.FEMAS_PAAS_POLLING_TYPE);
            if(StringUtils.isEmpty(pollingType)){
                pollingType = AbstractConfigHttpClientManager.PollingType.http.name();
            }
            while (configHttpClientManagerIterator.hasNext()) {
                configHttpClientManager = configHttpClientManagerIterator.next();
                if(StringUtils.equals(configHttpClientManager.getType(),pollingType)){
                    break;
                }
            }
        }
    }

}
  