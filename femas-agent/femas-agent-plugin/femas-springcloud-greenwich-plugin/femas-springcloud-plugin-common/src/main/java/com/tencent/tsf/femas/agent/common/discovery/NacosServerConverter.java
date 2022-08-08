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

import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.netflix.loadbalancer.Server;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.EndpointStatus;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import org.apache.commons.collections.MapUtils;

import java.util.Map;

/**
 * @Author leoziltong
 * @Date: 2021/6/16 16:32
 */
public class NacosServerConverter implements DiscoveryServerConverter {

    private volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();

    @Override
    public ServiceInstance convert(Server server) {
        ServiceInstance instance = new ServiceInstance();
        if (server instanceof NacosServer) {
            NacosServer nacosServer = (NacosServer) server;
            Instance nacosInstance = nacosServer.getInstance();
            instance.setLastUpdateTime(nacosServer.getInstance().getInstanceHeartBeatInterval());
            instance.setAllMetadata(nacosServer.getMetadata());
            instance.setHost(nacosInstance.getIp());
            instance.setPort(nacosInstance.getPort());
            instance.setStatus(EndpointStatus.DOWN);
            if (nacosInstance.isEnabled() && nacosInstance.isHealthy()) {
                instance.setStatus(EndpointStatus.UP);
            }
            Service service = new Service();
            String nacosServiceName = nacosServer.getInstance().getServiceName();
            service.setName(nacosServiceName.substring(nacosServiceName.lastIndexOf("@") + 1));

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
    public Server getOrigin(ServiceInstance serviceInstance) {
        if (serviceInstance.getOrigin() instanceof NacosServer) {
            return (Server) serviceInstance.getOrigin();
        }
        return null;
    }

    @Override
    public Map<String, String> getServerMetadata(Server server) {
        if (server instanceof NacosServer) {
            NacosServer nacosServer = (NacosServer) server;
            return nacosServer.getMetadata();
        }
        return MapUtils.EMPTY_MAP;
    }

}
