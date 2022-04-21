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
package com.tencent.tsf.femas.agent.nacos.instrument;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.tencent.tsf.femas.agent.interceptor.Interceptor;
import com.tencent.tsf.femas.agent.tools.AgentLogger;
import com.tencent.tsf.femas.api.ExtensionManager;
import com.tencent.tsf.femas.api.IExtensionLayer;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistryMetadata;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistryMetadataFactory;
import com.tencent.tsf.femas.common.util.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2022/4/8 16:04
 */
public class NacosServiceRegistryInterceptor implements Interceptor {

    private volatile AbstractServiceRegistryMetadata serviceRegistryMetadata = AbstractServiceRegistryMetadataFactory
            .getServiceRegistryMetadata();
    private IExtensionLayer extensionLayer = ExtensionManager.getExtensionLayer();
    private volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();

    /**
     * @param obj          Registration
     * @param allArguments method args
     * @param zuper        callable
     * @param method       reflect method
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        try {
            com.alibaba.cloud.nacos.registry.NacosRegistration registration = (com.alibaba.cloud.nacos.registry.NacosRegistration) allArguments[0];
            NacosDiscoveryProperties properties = registration.getNacosDiscoveryProperties();
            String namespace = Context.getSystemTag(contextConstant.getNamespaceId());
            Service service = new Service(namespace, properties.getService());
            String serverAddr = properties.getServerAddr();
            if (StringUtils.isNotEmpty(properties.getServerAddr())) {
                extensionLayer.init(service, NumberUtils.toInt(serverAddr.split(":")[0]), serverAddr.split(":")[1]);
            } else {
                extensionLayer.init(service, NumberUtils.toInt(serverAddr.split(":")[0]));
            }
            Map<String, String> registerMetadataMap = serviceRegistryMetadata.getRegisterMetadataMap();
            registerMetadataMap.put("protocol", "spring-cloud-nacos-plugin");
            registration.getMetadata().putAll(registerMetadataMap);
            return zuper.call();
        } catch (Exception e) {
        }
        return zuper.call();
    }

}
