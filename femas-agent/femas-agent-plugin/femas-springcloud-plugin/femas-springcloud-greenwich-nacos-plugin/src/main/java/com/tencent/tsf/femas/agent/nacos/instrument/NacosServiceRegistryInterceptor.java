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
import com.alibaba.nacos.client.naming.NacosNamingService;
import com.tencent.tsf.femas.agent.interceptor.InstanceMethodsAroundInterceptor;
import com.tencent.tsf.femas.agent.interceptor.Interceptor;
import com.tencent.tsf.femas.agent.interceptor.wrapper.InterceptResult;
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

/**
 * @Author leoziltong@tencent.com
 * @Date: 2022/4/8 16:04
 */
public class NacosServiceRegistryInterceptor implements InstanceMethodsAroundInterceptor<InterceptResult> {

    private volatile AbstractServiceRegistryMetadata serviceRegistryMetadata = AbstractServiceRegistryMetadataFactory
            .getServiceRegistryMetadata();
    private IExtensionLayer extensionLayer = ExtensionManager.getExtensionLayer();
    private volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();


    @Override
    public InterceptResult beforeMethod(Method method, Object[] allArguments, Class[] argumentsTypes) throws Throwable {
        com.alibaba.cloud.nacos.registry.NacosRegistration registration = (com.alibaba.cloud.nacos.registry.NacosRegistration) allArguments[0];
        NacosDiscoveryProperties properties = registration.getNacosDiscoveryProperties();
        String namespace = Context.getSystemTag(contextConstant.getNamespaceId());
        properties.setNamespace(namespace);
        Service service = new Service(namespace, properties.getService());
        String serverAddr = properties.getServerAddr();
        if (StringUtils.isNotEmpty(properties.getServerAddr())) {
            extensionLayer.init(service, NumberUtils.toInt(serverAddr.split(":")[1]), serverAddr.split(":")[0]);
        } else {
            extensionLayer.init(service, NumberUtils.toInt(serverAddr.split(":")[1]));
        }
        Map<String, String> registerMetadataMap = serviceRegistryMetadata.getRegisterMetadataMap();
        registerMetadataMap.put("protocol", "spring-cloud-nacos-plugin");
        registration.getMetadata().putAll(registerMetadataMap);
        return new InterceptResult();
    }

    @Override
    public Object afterMethod(Method method, Object[] allArguments, Class[] argumentsTypes, Object ret) throws Throwable {
        return ret;
    }

    @Override
    public void handleMethodException(Method method, Object[] allArguments, Class[] argumentsTypes, Throwable t) {

    }


}
