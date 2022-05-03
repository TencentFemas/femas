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

package com.tencent.tsf.femas.service.registry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tsf.femas.common.entity.EndpointStatus;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.serialize.JSONSerializer;
import com.tencent.tsf.femas.common.util.HttpResult;
import com.tencent.tsf.femas.entity.param.RegistryInstanceParam;
import com.tencent.tsf.femas.entity.registry.ClusterServer;
import com.tencent.tsf.femas.entity.registry.RegistryConfig;
import com.tencent.tsf.femas.entity.registry.RegistryPageService;
import com.tencent.tsf.femas.entity.registry.ServiceBriefInfo;
import com.tencent.tsf.femas.entity.registry.consul.ConsulServer;
import com.tencent.tsf.femas.entity.registry.consul.ConsulService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/4/29 21:59
 */
@Component
public class ConsulRegistryOpenApi extends RegistryOpenApiAdaptor {

    private static final Logger log = LoggerFactory.getLogger(ConsulRegistryOpenApi.class);
    private final static String FETCH_CLUSTER = "/v1/operator/raft/configuration";
    private final static String CATALOG_SERVICES = "/v1/catalog/services";
    private final static String CATALOG_SERVICE = "/v1/health/service";
    private static volatile ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<ClusterServer> clusterServers(RegistryConfig config) {
        List<ClusterServer> clusterServers = new ArrayList<>();
        try {
            String url = selectOne(config);
            HttpResult<String> result = httpClient.get(url.concat(FETCH_CLUSTER), null, null);
            ConsulServer consulServer = null;
            if (HttpStatus.SC_OK == NumberUtils.toInt(result.getCode())) {
                consulServer = JSONSerializer.deserializeStr(ConsulServer.class, result.getData());
            }
            List<ConsulServer.Server> servers = Optional.ofNullable(consulServer).map(c -> c.getServers()).get();
            if (!CollectionUtils.isEmpty(servers)) {
                servers.stream().forEach(s -> {
                    ClusterServer server = new ClusterServer();
                    server.setState("UP");
                    server.setServerAddr(s.getAddress());
                    server.setClusterRole(s.isLeader() ? "leader" : "follow");
                    clusterServers.add(server);
                });
            }
        } catch (Exception e) {
            log.error("ConsulRegistry fetch clusterServers failed", e);
        }
        return clusterServers;
    }

    @Override
    public ServerMetrics fetchServerMetrics(RegistryConfig config) {
        return null;
    }

    @Override
    public RegistryPageService fetchServices(RegistryConfig config, RegistryInstanceParam registryInstanceParam) {
        RegistryPageService registryPageService = new RegistryPageService();
        registryPageService.setPageNo(registryInstanceParam.getPageNo());
        registryPageService.setPageSize(registryInstanceParam.getPageSize());
        try {
            String url = selectOne(config);
            HttpResult<String> result = httpClient.get(url.concat(CATALOG_SERVICES), null, null);
            Map<String, Object> map = null;
            if (HttpStatus.SC_OK == NumberUtils.toInt(result.getCode())) {
                map = JSONSerializer.deserializeStr(Map.class, result.getData());
            }
            List<String> serviceStr = new ArrayList<>();
            if (!CollectionUtils.isEmpty(map)) {
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    serviceStr.add(entry.getKey());
                }
            }
            registryPageService.setCount(serviceStr.size());
            serviceStr = pageList(serviceStr, registryInstanceParam.getPageNo(), registryInstanceParam.getPageSize());
            if (serviceStr == null) {
                serviceStr = new ArrayList<>();
            }
            List<ServiceBriefInfo> serviceBriefInfos = new ArrayList<>(serviceStr.size());
            if (!CollectionUtils.isEmpty(serviceStr)) {
                serviceStr.stream().forEach(st -> {
                    RegistryInstanceParam instancesQueryParam = new RegistryInstanceParam();
                    instancesQueryParam.setNamespaceId(registryInstanceParam.getNamespaceId());
                    instancesQueryParam.setServiceName(st);
                    List<ServiceInstance> list = fetchServiceInstances(config, instancesQueryParam);
                    ServiceBriefInfo serviceBriefInfo = new ServiceBriefInfo();
                    serviceBriefInfo.setServiceName(st);
                    serviceBriefInfo.setInstanceNum(list == null ? 0 : list.size());
                    serviceBriefInfos.add(serviceBriefInfo);
                });
            }
            registryPageService.setServiceBriefInfos(serviceBriefInfos);
        } catch (Exception e) {
            log.error("ConsulRegistry fetchServices failed", e);
        }
        return registryPageService;
    }

    @Override
    public List<ServiceInstance> fetchServiceInstances(RegistryConfig config,
            RegistryInstanceParam registryInstanceParam) {
        List<ServiceInstance> serviceInstances = new ArrayList<>();
        try {
            String url = selectOne(config);
            HttpResult<String> result = httpClient
                    .get(url.concat(CATALOG_SERVICE).concat("/").concat(registryInstanceParam.getServiceName()), null,
                            null);
            List<ConsulService> services = new ArrayList<>();
            if (HttpStatus.SC_OK == NumberUtils.toInt(result.getCode())) {
                String data = result.getData();
                services = mapper.readValue(data, new TypeReference<List<ConsulService>>() {
                });
            }
            if (!CollectionUtils.isEmpty(services)) {
                services.stream().forEach(s -> {
                    ServiceInstance serviceInstance = new ServiceInstance();
                    ConsulService.Service service = s.getService();
                    Map<String, String> allMeta = new HashMap<>();
                    serviceInstance.setId(service.getId());
                    if (!CollectionUtils.isEmpty(service.getMeta())) {
                        allMeta.putAll(service.getMeta());
                    }
                    serviceInstance.setHost(service.getAddress());
                    serviceInstance.setPort(service.getPort());
                    List<String> strings = service.getTags();
                    Map<String, String> sm = new HashMap<>();
                    if (!CollectionUtils.isEmpty(strings)) {
                        strings.stream().forEach(str -> {
                            String[] ts = str.split("=");
                            if (ts.length > 1) {
                                sm.put(ts[0], ts[1]);
                            } else {
                                sm.put(ts[0], "");
                            }
                        });
                    }
                    allMeta.putAll(sm);
                    serviceInstance.setTags(sm);
                    serviceInstance.setAllMetadata(allMeta);
                    serviceInstance.setService(new Service(null, registryInstanceParam.getServiceName()));
//                    serviceInstance.setLastUpdateTime();
                    serviceInstance.setStatus(EndpointStatus.DOWN);
                    for (ConsulService.Check check : s.getChecks()) {
                        if (check.getServiceName().equalsIgnoreCase(
                                Optional.ofNullable(serviceInstance).map(sq -> sq.getService()).map(ss -> ss.getName())
                                        .orElse(""))) {
                            if (check.getStatus().equalsIgnoreCase("passing")) {
                                serviceInstance.setStatus(EndpointStatus.UP);
                                break;
                            }
                        }
                    }
                    serviceInstances.add(serviceInstance);
                });
            }
        } catch (Exception e) {
            log.error("ConsulRegistry fetchServiceInstances failed", e);
        }
        return serviceInstances;
    }

    @Override
    public void freshServiceMapCache(RegistryConfig config) {

    }

    @Override
    public RegistryPageService fetchNamespaceServices(RegistryConfig config, String namespaceId, int pageNo,
            int pageSize) {
        return null;
    }

    @Override
    public boolean healthCheck(String url) {
        return true;
    }
}
