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
package com.tencent.tsf.femas.extension.springcloud.ilford.discovery.kubernetes;

import java.util.Map;

import com.tencent.tsf.femas.extension.springcloud.ilford.discovery.loadbalancer.DiscoveryServerConverter;
import org.apache.commons.collections.MapUtils;

import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.EndpointStatus;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;

import org.springframework.cloud.kubernetes.commons.discovery.KubernetesServiceInstance;

/**
 * @Author leoziltong
 * @Date: 2021/6/16 16:32
 */

public class KubernetesServerConverter implements DiscoveryServerConverter {

    private volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();

    @Override
    public ServiceInstance convert(org.springframework.cloud.client.ServiceInstance server) {
        ServiceInstance instance = new ServiceInstance();
        if (server instanceof KubernetesServiceInstance) {
            KubernetesServiceInstance kubernetesServiceInstance = (KubernetesServiceInstance) server;
            instance.setAllMetadata(kubernetesServiceInstance.getMetadata());
            instance.setHost(kubernetesServiceInstance.getHost());
            instance.setPort(kubernetesServiceInstance.getPort());
            instance.setStatus(EndpointStatus.UP);
            Service service = new Service();
            String serviceName = kubernetesServiceInstance.getServiceId();
            service.setName(serviceName);
            if (MapUtils.isNotEmpty(kubernetesServiceInstance.getMetadata())) {
                Map<String, String> meta = kubernetesServiceInstance.getMetadata();
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
    public org.springframework.cloud.client.ServiceInstance getOrigin(ServiceInstance serviceInstance) {
        if (serviceInstance.getOrigin() instanceof org.springframework.cloud.client.ServiceInstance) {
            return (KubernetesServiceInstance) serviceInstance.getOrigin();
        }
        return null;
    }


    @Override
    public Map<String, String> getServerMetadata(org.springframework.cloud.client.ServiceInstance server) {
        if (server instanceof KubernetesServiceInstance) {
            KubernetesServiceInstance serviceInstance = (KubernetesServiceInstance) server;
            return serviceInstance.getMetadata();
        }
        return MapUtils.EMPTY_MAP;
    }

}
