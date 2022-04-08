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

package com.tencent.tsf.femas.agent.common.discovery;

import com.netflix.loadbalancer.Server;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import org.apache.commons.collections.MapUtils;
import org.springframework.cloud.consul.discovery.ConsulServer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author leoziltong
 * @Date: 2021/6/16 16:32
 */
public class ConsulServerConverter implements DiscoveryServerConverter {

    private volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();

    @Override
    public ServiceInstance convert(Server server) {
        ServiceInstance instance = new ServiceInstance();
        if (server instanceof ConsulServer) {
            ConsulServer consulServer = (ConsulServer) server;
            com.ecwid.consul.v1.health.model.HealthService.Service hs = consulServer.getHealthService().getService();
            instance.setAllMetadata(hs.getMeta());
            instance.setTags(parseTags(hs.getTags()));
            instance.setHost(hs.getAddress());
            instance.setPort(hs.getPort());

            Service service = new Service();
            service.setName(hs.getService());

            if (consulServer.getMetadata() != null) {
                Map<String, String> meta = consulServer.getMetadata();
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
    public Server getOrigin(ServiceInstance serviceInstance) {
        if (serviceInstance.getOrigin() instanceof ConsulServer) {
            return (Server) serviceInstance.getOrigin();
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

    @Override
    public Map<String, String> getServerMetadata(Server server) {
        if (server instanceof ConsulServer) {
            ConsulServer consulServer = (ConsulServer) server;
            return consulServer.getMetadata();
        }
        return MapUtils.EMPTY_MAP;
    }
}
