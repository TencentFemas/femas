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

package com.tencent.tsf.femas.springcloud.gateway.discovery.nacos;

import com.alibaba.cloud.nacos.NacosServiceInstance;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.EndpointStatus;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.springcloud.gateway.discovery.DiscoveryServerConverter;
import org.apache.commons.collections.MapUtils;
import org.springframework.cloud.client.ServiceInstance;

import java.util.Map;

/**
 * @Author juanyinyang
 */
public class NacosServerConverter implements DiscoveryServerConverter {

    private volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();

    @Override
    public com.tencent.tsf.femas.common.entity.ServiceInstance convert(ServiceInstance server) {
        com.tencent.tsf.femas.common.entity.ServiceInstance instance = new com.tencent.tsf.femas.common.entity.ServiceInstance();
        if (server instanceof NacosServiceInstance) {
            NacosServiceInstance nacosServer = (NacosServiceInstance) server;
            instance.setAllMetadata(nacosServer.getMetadata());
            instance.setHost(nacosServer.getHost());
            instance.setPort(nacosServer.getPort());
            instance.setStatus(EndpointStatus.UP);
            Service service = new Service();
            service.setName(nacosServer.getServiceId());

            if (nacosServer.getMetadata() != null) {
                Map<String, String> meta = nacosServer.getMetadata();
                instance.setId(meta.get(contextConstant.getMetaInstanceIdKey()));
                service.setNamespace(meta.get(contextConstant.getMetaNamespaceIdKey()));
                instance.setServiceVersion(meta.get(contextConstant.getApplicationVersion()));
            }
            instance.setService(service);
            instance.setOrigin(server);
            return instance;
        }
        return null;
    }

    @Override
    public ServiceInstance getOrigin(com.tencent.tsf.femas.common.entity.ServiceInstance serviceInstance) {
        if (serviceInstance.getOrigin() instanceof NacosServiceInstance) {
            return (NacosServiceInstance) serviceInstance.getOrigin();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String> getServerMetadata(ServiceInstance server) {
        if (server instanceof NacosServiceInstance) {
            NacosServiceInstance nacosServer = (NacosServiceInstance) server;
            return nacosServer.getMetadata();
        }
        return MapUtils.EMPTY_MAP;
    }

}
