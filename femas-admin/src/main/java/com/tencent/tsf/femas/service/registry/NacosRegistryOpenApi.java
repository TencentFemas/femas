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

import com.tencent.tsf.femas.common.entity.EndpointStatus;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.serialize.JSONSerializer;
import com.tencent.tsf.femas.common.util.HttpResult;
import com.tencent.tsf.femas.entity.namespace.Namespace;
import com.tencent.tsf.femas.entity.param.RegistryInstanceParam;
import com.tencent.tsf.femas.entity.registry.ClusterServer;
import com.tencent.tsf.femas.entity.registry.RegistryConfig;
import com.tencent.tsf.femas.entity.registry.RegistryPageService;
import com.tencent.tsf.femas.entity.registry.ServiceBriefInfo;
import com.tencent.tsf.femas.entity.registry.nacos.NacosInstance;
import com.tencent.tsf.femas.entity.registry.nacos.NacosServer;
import com.tencent.tsf.femas.entity.registry.nacos.NacosService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/4/29 21:59
 */
@Component
public class NacosRegistryOpenApi extends RegistryOpenApiAdaptor {

    /**
     * nacos 版本1.4.1
     * https://nacos.io/zh-cn/docs/open-api.html
     */
    private final static String CLUSTER_LIST = "/nacos/v1/ns/operator/servers";
    private final static String FETCH_SERVICE_LIST = "/nacos/v1/ns/catalog/services";
    private final static String FETCH_SERVICE_INSTANCE_LIST = "/nacos/v1/ns/catalog/instances";
    private final static String NAMESPCE_URL = "/nacos/v1/console/namespaces";

    @Override
    public List<ClusterServer> clusterServers(RegistryConfig config) {
        String url = selectOne(config);
        try {
            HttpResult<String> result = httpClient.get(url.concat(CLUSTER_LIST), null, null);
            NacosServer nacosServer = null;
            Map map = null;
            if (HttpStatus.SC_OK == NumberUtils.toInt(result.getCode())) {
                nacosServer = JSONSerializer.deserializeStr(NacosServer.class, result.getData());
            }
            if (nacosServer != null) {
                List<NacosServer.Server> serverList = nacosServer.getServers();
                List<ClusterServer> clusterServers = new ArrayList<>(serverList.size());
                serverList.stream().forEach(s -> {
                    ClusterServer server = new ClusterServer();
                    server.setServerAddr(s.getAddress());
                    server.setState(s.getState());
                    server.setLastRefreshTime(
                            Optional.ofNullable(s).map(s1 -> s1.getExtendInfo()).map(e -> e.getLastRefreshTime())
                                    .get());
                    Map<String, Object> nacosConf = Optional.ofNullable(s.getExtendInfo()).map(m -> m.getRaftMetaData())
                            .map(rm -> rm.getMetaDataMap()).map(md -> md.getNamingPersistentService())
                            .orElse(Collections.EMPTY_MAP);
                    server.setClusterRole("follow");
                    if (!CollectionUtils.isEmpty(nacosConf)) {
                        String leader = (String) nacosConf.get("leader");
                        String leaderIp = leader.split(":")[0];
                        String leaderPort = leader.split(":")[1];
                        if (leaderIp.equalsIgnoreCase(s.getIp())
                                && leaderPort.equalsIgnoreCase(s.getExtendInfo() != null ? s.getExtendInfo().getRaftPort(): "")) {
                            server.setClusterRole("leader");
                        }
                    }
                    clusterServers.add(server);
                });
                return clusterServers;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Override
    public void freshServiceMapCache(RegistryConfig config) {

    }

    @Override
    public ServerMetrics fetchServerMetrics(RegistryConfig config) {
        return null;
    }

    @Override
    public RegistryPageService fetchServices(RegistryConfig config, RegistryInstanceParam registryInstanceParam) {
        String url = selectOne(config);
        RegistryPageService registryPageService = new RegistryPageService();
        registryPageService.setPageNo(registryInstanceParam.getPageNo());
        registryPageService.setPageSize(registryInstanceParam.getPageSize());
        try {
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("pageNo", registryInstanceParam.getPageNo());
            queryMap.put("pageSize", registryInstanceParam.getPageSize());
            queryMap.put("namespaceId", registryInstanceParam.getNamespaceId());
            HttpResult<String> result = httpClient.get(url.concat(FETCH_SERVICE_LIST), null, queryMap);
            NacosService service = null;
            if (HttpStatus.SC_OK == NumberUtils.toInt(result.getCode())) {
                service = JSONSerializer.deserializeStr(NacosService.class, result.getData());
            }
            if (service != null && !CollectionUtils.isEmpty(service.getServiceList())) {
                List<NacosService.Service> serviceList = service.getServiceList();
                List<ServiceBriefInfo> briefInfos = new ArrayList<>();
                serviceList.stream().forEach(s -> {
                    ServiceBriefInfo serviceBriefInfo = new ServiceBriefInfo();
                    serviceBriefInfo.setServiceName(s.getName());
                    serviceBriefInfo.setInstanceNum(s.getIpCount());
                    briefInfos.add(serviceBriefInfo);
                });
                registryPageService.setServiceBriefInfos(briefInfos);
                registryPageService.setCount(service.getCount());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return registryPageService;
    }

    @Override
    public RegistryPageService fetchNamespaceServices(RegistryConfig config, String namespaceId, int pageNo,
            int pageSize) {
        return null;
    }

    @Override
    public List<ServiceInstance> fetchServiceInstances(RegistryConfig config,
            RegistryInstanceParam registryInstanceParam) {
        String url = selectOne(config);
        try {
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("serviceName", registryInstanceParam.getServiceName());
            queryMap.put("clusterName", "DEFAULT");
            queryMap.put("pageNo", 1);
            queryMap.put("pageSize", 100);
            queryMap.put("namespaceId", registryInstanceParam.getNamespaceId());
            HttpResult<String> result = httpClient.get(url.concat(FETCH_SERVICE_INSTANCE_LIST), null, queryMap);
            NacosInstance nacosInstance = null;
            if (HttpStatus.SC_OK == NumberUtils.toInt(result.getCode())) {
                nacosInstance = JSONSerializer.deserializeStr(NacosInstance.class, result.getData());
            }
            if (nacosInstance != null && !CollectionUtils.isEmpty(nacosInstance.getList())) {
                List<ServiceInstance> serviceInstances = new ArrayList<>(nacosInstance.getList().size());
                List<NacosInstance.Instance> instances = nacosInstance.getList();
                instances.stream().forEach(i -> {
                    ServiceInstance instance = new ServiceInstance();
                    instance.setAllMetadata(i.getMetadata());
                    instance.setHost(i.getIp());
                    instance.setPort(NumberUtils.toInt(i.getPort()));
                    instance.setId(i.getInstanceId());
                    instance.setStatus(i.isEnabled() && i.isHealthy() ? EndpointStatus.UP : EndpointStatus.DOWN);
                    instance.setLastUpdateTime(i.getLastBeat());
                    instance.setService(new Service(i.getClusterName(), i.getServiceName()));
                    serviceInstances.add(instance);
                });
                return serviceInstances;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public boolean healthCheck(String url) {
        return true;
    }

    @Override
    public boolean createNamespace(RegistryConfig config, Namespace namespace) {
        String url = selectOne(config);
        try {
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("customNamespaceId", namespace.getNamespaceId());
            queryMap.put("namespaceName", namespace.getName());
            queryMap.put("namespaceDesc", namespace.getDesc());
            HttpResult<String> result = httpClient.post(url.concat(NAMESPCE_URL), null, queryMap, null);
            return Boolean.TRUE.toString().equals(result.getData());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteNamespace(RegistryConfig config, Namespace namespace) {
        String url = selectOne(config);
        try {
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("namespaceId", namespace.getNamespaceId());
            HttpResult<String> result = httpClient.delete(url.concat(NAMESPCE_URL), null, queryMap);
            return Boolean.TRUE.toString().equals(result.getData());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
