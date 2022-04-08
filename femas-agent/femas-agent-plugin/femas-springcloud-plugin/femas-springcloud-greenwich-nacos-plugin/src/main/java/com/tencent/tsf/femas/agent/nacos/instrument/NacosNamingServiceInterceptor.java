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

import com.tencent.tsf.femas.agent.interceptor.Interceptor;
import com.tencent.tsf.femas.api.ExtensionManager;
import com.tencent.tsf.femas.api.IExtensionLayer;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistryMetadata;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistryMetadataFactory;
import com.tencent.tsf.femas.common.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2022/4/8 16:04
 */
public class NacosNamingServiceInterceptor implements Interceptor {

    private volatile AbstractServiceRegistryMetadata serviceRegistryMetadata = AbstractServiceRegistryMetadataFactory
            .getServiceRegistryMetadata();
    private IExtensionLayer extensionLayer = ExtensionManager.getExtensionLayer();
    private volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();

    /**
     * @param obj          serviceName  Instance ins
     * @param allArguments method args
     * @param zuper        callable
     * @param method       reflect method
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        try {
            String registryUrl = null;
            Integer port = null;
            String serviceName = (String) allArguments[0];
            com.alibaba.nacos.api.naming.pojo.Instance instance = (com.alibaba.nacos.api.naming.pojo.Instance) allArguments[1];
            String namespace = Context.getSystemTag(contextConstant.getNamespaceId());
            Service service = new Service(namespace, serviceName);
            if (StringUtils.isNotEmpty(registryUrl)) {
                extensionLayer.init(service, port, registryUrl);
            } else {
                extensionLayer.init(service, port);
            }
            Map<String, String> registerMetadataMap = serviceRegistryMetadata.getRegisterMetadataMap();
            instance.addMetadata("protocol", "spring-cloud-nacos");
            registerMetadataMap.entrySet().stream().forEach(m -> {
                instance.addMetadata(m.getKey(), m.getValue());
            });
            return zuper.call();
        } catch (Exception e) {
        }
        return zuper.call();
    }

}
