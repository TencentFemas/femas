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
import com.tencent.tsf.femas.entity.param.RegistryInstanceParam;
import com.tencent.tsf.femas.entity.registry.ClusterServer;
import com.tencent.tsf.femas.entity.registry.RegistryConfig;
import com.tencent.tsf.femas.entity.registry.RegistryPageService;
import com.tencent.tsf.femas.entity.registry.ServiceBriefInfo;
import com.tencent.tsf.femas.entity.registry.eureka.EurekaApplication;
import com.tencent.tsf.femas.entity.registry.eureka.EurekaInstance;
import com.tencent.tsf.femas.entity.registry.eureka.EurekaServer;
import com.tencent.tsf.femas.entity.registry.eureka.EurekaService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/4/29 21:59
 */
@Component
public class EurekaRegistryOpenApi extends RegistryOpenApiAdaptor {

    private final static String FETCH_APPS_URL = "/eureka/apps";

    private final static String FETCH_CLUSTER_STATUS = "/eureka/status";

    private final AtomicBoolean listen = new AtomicBoolean(true);

    @Override
    public List<ClusterServer> clusterServers(RegistryConfig config) {
        String url = selectOne(config);
        try {
            List<ClusterServer> servers = new ArrayList<>();
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json;charset=UTF-8");
            headers.put("Accept", "application/json;charset=UTF-8");
            HttpResult<String> result = httpClient.get(url.concat(FETCH_CLUSTER_STATUS), headers, null);
            EurekaServer eurekaServer = null;
            if (HttpStatus.SC_OK == NumberUtils.toInt(result.getCode())) {
                eurekaServer = JSONSerializer.deserializeStr(EurekaServer.class, result.getData());
                if (eurekaServer == null) {
                    //eureka 返回json缺省
                    eurekaServer = JSONSerializer.deserializeStr(EurekaServer.class, result.getData().concat("}"));
                }
            }
            String rrs = Optional.ofNullable(eurekaServer).map(s -> s.getApplicationStats())
                    .map(a -> a.getAvailableReplicas()).get();
            String unRrs = Optional.ofNullable(eurekaServer).map(s -> s.getApplicationStats())
                    .map(a -> a.getUnavailableReplicas()).get();
            String homeUrl = Optional.ofNullable(eurekaServer).map(s -> s.getInstanceInfo())
                    .map(a -> a.getHomePageUrl()).get();
            Long updateTime = Optional.ofNullable(eurekaServer).map(s -> s.getInstanceInfo())
                    .map(a -> a.getLastUpdatedTimestamp()).get();
            if (StringUtils.isNotEmpty(rrs)) {
                String[] rra = rrs.split(",");
                if (rra.length > 0) {
                    for (String s : Arrays.asList(rra)) {
                        ClusterServer server = new ClusterServer();
                        server.setServerAddr(s);
                        server.setLastRefreshTime(updateTime);
                        server.setState("UP");
                        servers.add(server);
                    }
                }
            }
            if (StringUtils.isNotEmpty(unRrs)) {
                String[] rra = unRrs.split(",");
                if (rra.length > 0) {
                    for (String s : Arrays.asList(rra)) {
                        ClusterServer server = new ClusterServer();
                        server.setServerAddr(s);
                        server.setLastRefreshTime(updateTime);
                        server.setState("DOWN");
                        servers.add(server);
                    }
                }
            }
            servers.add(new ClusterServer(homeUrl, updateTime, "UP"));
            return servers;
        } catch (Exception e) {

        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public ServerMetrics fetchServerMetrics(RegistryConfig config) {
        return super.fetchServerMetrics(config);
    }

    @Override
    public RegistryPageService fetchServices(RegistryConfig config, RegistryInstanceParam registryInstanceParam) {
        return retryFetchServices(config, registryInstanceParam.getPageNo(), registryInstanceParam.getPageSize(), 1);
    }


    public RegistryPageService retryFetchServices(RegistryConfig config, int pageNo, int pageSize, int times) {
        if (listen.compareAndSet(true, false)) {
            freshServiceMapCache(config);
            listen(config);
        }
        RegistryPageService registryPageService = new RegistryPageService();
        registryPageService.setPageNo(pageNo);
        registryPageService.setPageSize(pageSize);
        //如果注册中心就是没有服务可能陷入死循环
        if (!CollectionUtils.isEmpty(serviceMapCache)) {
            List<ServiceBriefInfo> serviceBriefInfos = new ArrayList<>();
            for (Map.Entry<String, List<ServiceInstance>> entry : serviceMapCache.entrySet()) {
                ServiceBriefInfo briefInfo = new ServiceBriefInfo();
                briefInfo.setServiceName(JSONSerializer.deserializeStr(Service.class, entry.getKey()).getName());
                briefInfo.setInstanceNum(entry.getValue().size());
                serviceBriefInfos.add(briefInfo);
            }
            registryPageService.setCount(serviceBriefInfos.size());
            registryPageService.setServiceBriefInfos(pageList(serviceBriefInfos, pageNo, pageSize));
        } else {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (times < 5) {
                times++;
                freshServiceMapCache(config);
                return retryFetchServices(config, pageNo, pageSize, times);
            }
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
        return reTryFetchServiceInstances(config, registryInstanceParam.getServiceName(), 1);
    }

    public List<ServiceInstance> reTryFetchServiceInstances(RegistryConfig config, String serviceName, int times) {
        if (listen.compareAndSet(true, false)) {
            freshServiceMapCache(config);
            listen(config);
        }
        List<ServiceInstance> serviceInstances = new ArrayList<>();
        //如果注册中心就是没有服务可能陷入死循环
        if (!CollectionUtils.isEmpty(serviceMapCache)) {
            serviceInstances = serviceMapCache
                    .get(JSONSerializer.serializeStr(new Service(null, serviceName.toUpperCase())));
        } else {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (times > 5) {
                return serviceInstances;
            } else {
                times++;
                freshServiceMapCache(config);
                reTryFetchServiceInstances(config, serviceName, times);
            }
        }
        return serviceInstances;
    }

    @Override
    public void freshServiceMapCache(RegistryConfig config) {
        String url = selectOne(config);
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json;charset=UTF-8");
            headers.put("Accept", "application/json;charset=UTF-8");
            HttpResult<String> result = httpClient.get(url.concat(FETCH_APPS_URL), headers, null);
            EurekaApplication bean = null;
            if (HttpStatus.SC_OK == NumberUtils.toInt(result.getCode())) {
                bean = JSONSerializer.deserializeStr(EurekaApplication.class, result.getData());
                if (bean == null) {
                    //eureka 返回json缺省
                    bean = JSONSerializer.deserializeStr(EurekaApplication.class, result.getData().concat("}"));
                }
            }
            serviceMapCache.clear();
            if (bean != null) {
                List<EurekaService> eurekaServices = bean.getApplications().getApplication();
                refresh(eurekaServices, serviceMapCache);
            }
        } catch (Exception e) {

        }
    }

    private void refresh(List<EurekaService> eurekaServices, final Map<String, List<ServiceInstance>> serviceMapCache) {
        if (CollectionUtils.isEmpty(eurekaServices)) {
            return;
        }
        eurekaServices.stream().forEach(es -> {
            Service service = new Service();
            service.setName(es.getName());
            List<EurekaInstance> eurekaInstanceList = es.getInstance();
            List<ServiceInstance> serviceInstances = Collections.EMPTY_LIST;
            if (!CollectionUtils.isEmpty(eurekaInstanceList)) {
                final List<ServiceInstance> serviceInstanceList = new ArrayList<>(eurekaInstanceList.size());
                eurekaInstanceList.stream().forEach(ei -> {
                    ServiceInstance instance = new ServiceInstance();
                    instance.setService(service);
                    instance.setLastUpdateTime(ei.getLastUpdatedTimestamp());
                    instance.setStatus(EndpointStatus.getTypeByName(ei.getStatus()));
                    instance.setPort(NumberUtils.toInt(ei.getPort().entrySet().iterator().next().getValue()));
                    instance.setHost(ei.getIpAddr());
                    instance.setAllMetadata(ei.getMetadata());
                    serviceInstanceList.add(instance);
                });
                serviceInstances = serviceInstanceList;
            }
            serviceMapCache.put(JSONSerializer.serializeStr(service), serviceInstances);
        });
    }

    @Override
    public boolean healthCheck(String url) {
        return true;
    }
}