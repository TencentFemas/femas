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

package com.tencent.tsf.femas.extension.springcloud.ilford.discovery.consul;

import com.ecwid.consul.v1.health.model.HealthService;
import com.tencent.tsf.femas.extension.springcloud.ilford.discovery.loadbalancer.DiscoveryServerConverter;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.consul.discovery.ConsulServiceInstance;

/**
 * @Author juanyinyang
 */
public class ConsulServerConverter implements DiscoveryServerConverter {

    private volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();

    @Override
    public com.tencent.tsf.femas.common.entity.ServiceInstance convert(ServiceInstance server) {
        com.tencent.tsf.femas.common.entity.ServiceInstance instance = new com.tencent.tsf.femas.common.entity.ServiceInstance();
        if (server instanceof ConsulServiceInstance) {
            ConsulServiceInstance consulServer = (ConsulServiceInstance) server;
            HealthService service = consulServer.getHealthService();
            HealthService.Service hs = service.getService();
            hs.getTags();
            com.ecwid.consul.v1.health.model.HealthService.Service healthService = consulServer.getHealthService()
                    .getService();
            String namespace = consulServer.getMetadata().get(contextConstant.getMetaNamespaceIdKey());
            String serviceName = healthService.getService();
            instance.setService(new Service(namespace, serviceName));
            instance.setId(hs.getId());
            instance.setHost(hs.getAddress());
            instance.setPort(hs.getPort());
            Map<String, String> metadata = hs.getMeta();
            instance.setAllMetadata(metadata);
            instance.setTags(parseTags(hs.getTags()));
            instance.setOrigin(server);
            return instance;
        }
        return null;
    }

    @Override
    public ServiceInstance getOrigin(com.tencent.tsf.femas.common.entity.ServiceInstance serviceInstance) {
        if (serviceInstance.getOrigin() instanceof ConsulServiceInstance) {
            return (ServiceInstance) serviceInstance.getOrigin();
        }
        return null;
    }

    private Map<String, String> parseTags(List<String> tagList) {
        Map<String, String> tags = new HashMap<>();

        for (String tag : tagList) {
            String[] entry = tag.split("=");
            if (entry.length == 2) {
                tags.put(entry[0], entry[1]);
            }
        }
        return tags;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String> getServerMetadata(ServiceInstance server) {
        if (server instanceof ConsulServiceInstance) {
            ConsulServiceInstance consulServer = (ConsulServiceInstance) server;
            return consulServer.getMetadata();
        }
        return MapUtils.EMPTY_MAP;
    }
}
