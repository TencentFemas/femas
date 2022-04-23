/*
 * Tencent is pleased to support the open source community by making Femas available.
 *
 * Copyright (C) 2021, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */   
package com.tencent.tsf.femas.service.registry;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.tencent.tsf.femas.common.entity.EndpointStatus;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.serialize.JSONSerializer;
import com.tencent.tsf.femas.common.util.HttpHeaderKeys;
import com.tencent.tsf.femas.common.util.HttpResult;
import com.tencent.tsf.femas.config.util.parser.ParserException;
import com.tencent.tsf.femas.config.util.parser.Parsers;
import com.tencent.tsf.femas.constant.AdminConstants;
import com.tencent.tsf.femas.entity.namespace.Namespace;
import com.tencent.tsf.femas.entity.param.RegistryInstanceParam;
import com.tencent.tsf.femas.entity.registry.ClusterServer;
import com.tencent.tsf.femas.entity.registry.RegistryConfig;
import com.tencent.tsf.femas.entity.registry.RegistryPageService;
import com.tencent.tsf.femas.entity.registry.ServiceBriefInfo;
import com.tencent.tsf.femas.entity.registry.polaris.PolarisServer;
import com.tencent.tsf.femas.entity.registry.polaris.PolarisServer.PolarisInstance;
import com.tencent.tsf.femas.entity.registry.polaris.PolarisService;

/**
* <pre>  
* 文件名称：PolarisRegistryOpenApi.java  
* 创建时间：Dec 28, 2021 10:55:58 AM   
* @author juanyinyang  
* 类说明：  
*/
@Component
public class PolarisRegistryOpenApi extends RegistryOpenApiAdaptor {
    
    private static final Logger log = LoggerFactory.getLogger(PolarisRegistryOpenApi.class);
    private final static String NAMESPACE_URL = "/naming/v1/namespaces";
    private final static String NAMESPACE_DELETE_URL = NAMESPACE_URL + "/delete";
    private final static String SERVICE_URL = "/naming/v1/services";
    private final static String SERVICE_INSTANCE_URL = "/naming/v1/instances";
    private final static String CLUSTER_SERVER_LOCAL_HOST = "127.0.0.1";
    
    // token暂时先存在内存里，后续可能要考虑写到db里
    private final Map<String, String> namespaceTokenMap = new ConcurrentHashMap<>();

    @Override
    public List<ClusterServer> clusterServers(RegistryConfig config) {
        try {
            Map<String, Object> queryMap = new HashMap<>();
            // 查询北极星server集群，北极星server集群也会注册到北极星注册中心上作为服务实例供查询
            queryMap.put("namespace", "Polaris");
            // 北极星server  
            // v1.3.0 开始是 polaris.checker，目前暂时只支持v1.3.0以后的版本
            // v1.3.0 之前是 polaris.healthcheck，但是v1.3.0 之前貌似有不少版本查不到
            queryMap.put("service", "polaris.checker");
            queryMap.put("offset", 0);
            String url = selectOne(config);
            HttpResult<String> result = httpClient.get(url.concat(SERVICE_INSTANCE_URL), null, queryMap);
            PolarisServer polarisServer = null;
            if (HttpStatus.SC_OK == NumberUtils.toInt(result.getCode())) {
                polarisServer = JSONSerializer.deserializeStr(PolarisServer.class, result.getData());
            }
            if (polarisServer != null && !CollectionUtils.isEmpty(polarisServer.getInstances())) {
                List<ClusterServer> clusterServers = new ArrayList<>(polarisServer.getInstances().size());
                List<PolarisInstance> instances = polarisServer.getInstances();
                instances.stream().forEach(i -> {
                    ClusterServer server = new ClusterServer();
                    String serverHost = i.getHost();
                    // 北极星取到的集群地址如果是127.0.0.1，替换成请求url所在集群ip地址
                    if(CLUSTER_SERVER_LOCAL_HOST.equals(i.getHost())) {
                        try {
                            URI uri = new URI(url);
                            String host = uri.getHost();
                            if(!CLUSTER_SERVER_LOCAL_HOST.equals(host)) {
                                serverHost = host;
                            }
                        } catch (Exception e) {
                            log.warn("PolarisRegistryOpenApi clusterServers new URI Exception, host:{}", i.getHost(), e);
                        }
                    }
                    server.setServerAddr(serverHost+":"+i.getPort());
                    server.setClusterRole("leader");
                    server.setState(i.isHealthy() ? EndpointStatus.UP.toString() : EndpointStatus.DOWN.toString());
                    try {
                        server.setLastRefreshTime(Parsers.forDate().parse(i.getMtime(), Parsers.DateParser.MEDIUM_DATE_FORMAT).getTime());
                    } catch (ParserException e) {
                        log.warn("PolarisRegistryOpenApi clusterServers parserException, mtime:{}", i.getMtime(), e);
                    }
                    clusterServers.add(server);
                });
                return clusterServers;
            }
        } catch (Exception e) {
            log.error("PolarisRegistryOpenApi clusterServers failed:", e);
        }
        return Collections.emptyList();
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
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("namespace", registryInstanceParam.getNamespaceId());
            String url = selectOne(config);
            HttpResult<String> result = httpClient.get(url.concat(SERVICE_URL), null, queryMap);
            PolarisService service = null;
            if (HttpStatus.SC_OK == NumberUtils.toInt(result.getCode())) {
                service = JSONSerializer.deserializeStr(PolarisService.class, result.getData());
            }
            if (service != null && !CollectionUtils.isEmpty(service.getServices())) {
                List<PolarisService.Service> serviceList = service.getServices();
                List<ServiceBriefInfo> briefInfos = new ArrayList<>();
                serviceList.stream().forEach(s -> {
                    ServiceBriefInfo serviceBriefInfo = new ServiceBriefInfo();
                    serviceBriefInfo.setServiceName(s.getName());
                    serviceBriefInfo.setInstanceNum(s.getTotalInstanceCount());
                    briefInfos.add(serviceBriefInfo);
                });
                registryPageService.setServiceBriefInfos(briefInfos);
                registryPageService.setCount(service.getAmount());
            }
        } catch (Exception e) {
            log.error("PolarisRegistryOpenApi fetchServices failed:", e);
        }
        return registryPageService;
    }
    
    @Override
    public List<ServiceInstance> fetchServiceInstances(RegistryConfig config,
            RegistryInstanceParam registryInstanceParam) {
        String url = selectOne(config);
        try {
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("service", registryInstanceParam.getServiceName());
            queryMap.put("offset", 0);
            queryMap.put("limit", 100);
            queryMap.put("namespace", registryInstanceParam.getNamespaceId());
            HttpResult<String> result = httpClient.get(url.concat(SERVICE_INSTANCE_URL), null, queryMap);
            PolarisServer polarisServer = null;
            if (HttpStatus.SC_OK == NumberUtils.toInt(result.getCode())) {
                polarisServer = JSONSerializer.deserializeStr(PolarisServer.class, result.getData());
            }
            if (polarisServer != null && !CollectionUtils.isEmpty(polarisServer.getInstances())) {
                List<ServiceInstance> serviceInstances = new ArrayList<>(polarisServer.getInstances().size());
                List<PolarisInstance> instances = polarisServer.getInstances();
                instances.stream().forEach(i -> {
                    ServiceInstance instance = new ServiceInstance();
                    instance.setAllMetadata(i.getMetadata());
                    instance.setHost(i.getHost());
                    instance.setPort(i.getPort());
                    instance.setId(i.getId());
                    instance.setStatus(i.isHealthy() ? EndpointStatus.UP : EndpointStatus.DOWN);
                    try {
                        instance.setLastUpdateTime(Parsers.forDate().parse(i.getMtime(), Parsers.DateParser.MEDIUM_DATE_FORMAT).getTime());
                    } catch (ParserException e) {
                        log.warn("PolarisRegistryOpenApi fetchServiceInstances parserException, mtime:{}", i.getMtime(), e);
                    }
                    instance.setService(new Service(i.getNamespace(), i.getService()));
                    serviceInstances.add(instance);
                });
                return serviceInstances;
            }
        } catch (Exception e) {
            log.error("PolarisRegistryOpenApi fetchServiceInstances exception:", e);
        }
        return Collections.emptyList();
    }


    /** 
     * @see com.tencent.tsf.femas.service.registry.RegistryOpenApiAdaptor#freshServiceMapCache(com.tencent.tsf.femas.entity.registry.RegistryConfig)
     */
    @Override
    public void freshServiceMapCache(RegistryConfig config) {
    }
    
    /** 
     * @see com.tencent.tsf.femas.service.registry.RegistryOpenApiAdaptor#fetchNamespaceServices(com.tencent.tsf.femas.entity.registry.RegistryConfig, java.lang.String, int, int)
     */
    @Override
    public RegistryPageService fetchNamespaceServices(RegistryConfig config, String namespaceId,
                                                      int pageNo, int pageSize) {
        return null;
    }

    /** 
     * @see com.tencent.tsf.femas.service.registry.RegistryOpenApiAdaptor#healthCheck(java.lang.String)
     */
    @Override
    public boolean healthCheck(String url) {
        return true;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean createNamespace(RegistryConfig config, Namespace namespace) {
        String url = selectOne(config);
        try {
            List<Map<String, Object>> requestBody = new ArrayList<>();
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("name", namespace.getNamespaceId());
            queryMap.put("comment", namespace.getDesc());
            queryMap.put("owners", AdminConstants.USERNAME);
            requestBody.add(queryMap);
            String requestJsonBody = JSONSerializer.serializeStr(requestBody);
            
            String namespaceUrl = url.concat(NAMESPACE_URL);
            HttpResult<String> result = httpClient.postJson(namespaceUrl, buildHeader(), requestJsonBody, null);
            log.info("PolarisRegistryOpenApi createNamespace req url:{}, requestJsonBody:{}, result:{}", namespaceUrl, requestJsonBody, result);
            Map<String, Object> mapResult = null;
            if (HttpStatus.SC_OK != NumberUtils.toInt(result.getCode())) {
                return false;
            }
            mapResult = JSONSerializer.deserializeStr(Map.class, result.getData());
            if(mapResult == null) {
                log.warn("PolarisRegistryOpenApi createNamespace mapResult is null");
                return false;
            }
            List<Map<String, Object>> responsesMap = (List<Map<String, Object>>) mapResult.get("responses");
            if(CollectionUtils.isEmpty(responsesMap)) {
                log.warn("PolarisRegistryOpenApi createNamespace responsesMap is null"); 
                return false;
            }
            Map<String, String> namespaceMap = (Map<String, String>)responsesMap.get(0).get("namespace");
            String token = namespaceMap.get("token");
            namespaceTokenMap.put(namespace.getNamespaceId(), token);
            return true;
        } catch (Exception e) {
            log.error("PolarisRegistryOpenApi createNamespace exception:", e);
            return false;
        }
    }

    @Override
    public boolean modifyNamespace(RegistryConfig config, Namespace namespace) {
        //不熟悉polaris，沿用原先的逻辑
        return createNamespace(config, namespace);
    }

    @Override
    public boolean deleteNamespace(RegistryConfig config, Namespace namespace) {
        String url = selectOne(config);
        try {
            List<Map<String, Object>> requestBody = new ArrayList<>();
            Map<String, Object> queryMap = new HashMap<>();
            String token = namespaceTokenMap.get(namespace.getNamespaceId());
            queryMap.put("name", namespace.getNamespaceId());
            queryMap.put("token", token);
            requestBody.add(queryMap);
            String requestJsonBody = JSONSerializer.serializeStr(requestBody);
            
            String deleteNamespaceUrl = url.concat(NAMESPACE_DELETE_URL);
            HttpResult<String> result = httpClient.postJson(deleteNamespaceUrl, buildHeader(), requestJsonBody, null);
            log.info("PolarisRegistryOpenApi deleteNamespace req url:{}, queryMap:{}, result:{}", deleteNamespaceUrl, queryMap, result);
            if (HttpStatus.SC_OK != NumberUtils.toInt(result.getCode())) {
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("PolarisRegistryOpenApi deleteNamespace exception:", e);
            return false;
        }
    }
    
    private Map<String, String> buildHeader() {
        Map<String, String> header = new HashMap<>();
        header.put(HttpHeaderKeys.ACCEPT_ENCODING, "gzip,deflate,sdch");
        header.put(HttpHeaderKeys.CONNECTION, "Keep-Alive");
        header.put(HttpHeaderKeys.CONTENT_TYPE, "application/json;charset=UTF-8");
        return header;
    }

}
  