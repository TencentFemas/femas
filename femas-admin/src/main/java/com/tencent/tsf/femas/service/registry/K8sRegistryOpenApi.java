///*
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.tencent.tsf.femas.service.registry;
//
//import com.tencent.tsf.femas.common.entity.EndpointStatus;
//import com.tencent.tsf.femas.common.entity.Service;
//import com.tencent.tsf.femas.common.entity.ServiceInstance;
//import com.tencent.tsf.femas.entity.namespace.Namespace;
//import com.tencent.tsf.femas.entity.param.RegistryInstanceParam;
//import com.tencent.tsf.femas.entity.registry.ClusterServer;
//import com.tencent.tsf.femas.entity.registry.RegistryConfig;
//import com.tencent.tsf.femas.entity.registry.RegistryPageService;
//import com.tencent.tsf.femas.entity.registry.ServiceBriefInfo;
//import io.kubernetes.client.openapi.ApiClient;
//import io.kubernetes.client.openapi.ApiException;
//import io.kubernetes.client.openapi.Configuration;
//import io.kubernetes.client.openapi.apis.CoreV1Api;
//import io.kubernetes.client.openapi.apis.CustomObjectsApi;
//import io.kubernetes.client.openapi.models.*;
//import io.kubernetes.client.util.ClientBuilder;
//import io.kubernetes.client.util.KubeConfig;
//import io.kubernetes.client.util.credentials.AccessTokenAuthentication;
//import org.apache.commons.collections.CollectionUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.io.StringReader;
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// * @Author leoziltong@tencent.com
// * @Date: 2021/4/29 21:59
// */
//@Component
//public class K8sRegistryOpenApi extends RegistryOpenApiAdaptor {
//
//    private final static Logger log = LoggerFactory.getLogger("K8sRegistryOpenApi");
//
//    private static final String HTTPS_PORT_NAME = "https";
//
//    private static final String HTTP_PORT_NAME = "http";
//
//    private static final String PRIMARY_PORT_NAME_LABEL_KEY = "primary-port-name";
//
//    private static final KubernetesProperties properties = new KubernetesProperties();
//
//    private ApiClient getApiClient(RegistryConfig config) {
//        if (Optional.of(config).map(c -> c.getCertificateType()).isPresent()) {
//            if (config.getCertificateType().equalsIgnoreCase("config")) {
//                return generateClientByConf(config.getKubeConfig());
//            }
//            if (config.getCertificateType().equalsIgnoreCase("secret")) {
//                return generateClientBySecret(config.getSecret(), config.getApiServerAddr());
//            }
//        }
//        return null;
//    }
//
//    private boolean initConfiguration(RegistryConfig config) {
//        ApiClient apiClient = getApiClient(config);
//        if (apiClient == null) {
//            log.error("init k8s api client configuration error");
//            return false;
//        }
//        Configuration.setDefaultApiClient(apiClient);
//        return true;
//    }
//
//    public static void main(String[] args) {
//        ApiClient apiClient = new ClientBuilder().setBasePath("https://159.75.127.199:6443/").setVerifyingSsl(false)
//                .setAuthentication(new AccessTokenAuthentication("eyJhbGciOiJSUzI1NiIsImtpZCI6InVsOXUzTlh1TzV1ejY1WXVaUHZfVXg4ekdwem9ScEJfaGM5V05ZUjRYeUkifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJhZG1pbi10b2tlbi1tY2M4biIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJhZG1pbiIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjZlMWM5Y2E4LTUyZmMtNDE1YS1iYTBkLTQxN2VjYjg2MzVhYiIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDprdWJlLXN5c3RlbTphZG1pbiJ9.PWj3_6BrKNdKRGXTbAlntrxmzkGJ8p5-dFpVToWarQiVPDOieTzfaDO44Yk5ksboVB2xbyDPcDOhJZW8wxo_lvkaBXae4JOUt7H0-6z4bdhBIUQFm-gzxRzUncPv14xiWlA1f6t2LlJJKZhbbbbYy7-QUPvLgpnrRVpijykkglUG4U8bk5iGiSLW1BOxpJR8fVt3OhZ96vimqRXCVw0UcbALjadtB1mV10LTkwpWkgw38OaC8hkXdpwQLmG-pjXF1ZdgpeY_RTpRZigcjGYsKi7pMxxbqQi88fUR9Ju6q1sYPY2AXjs-Or4BVQjXDvFmcd7c7FTawR8rVe_qKSKucQ")).build();
//        Configuration.setDefaultApiClient(apiClient);
//        CoreV1Api api = new CoreV1Api();
//        CustomObjectsApi apiInstance = new CustomObjectsApi(apiClient);
//        V1NodeList v1NodeList = null;
//        V1ServiceList v1ServiceList = null;
//        V1Service v1Service = null;
//        V1EndpointsList endpointsList = null;
//        V1Endpoints endpoints = null;
//        try {
//            v1NodeList = api.listNode("true", null, null, null, null, null, null, null, null);
//            endpointsList = api.listNamespacedEndpoints("kubernetes-dashboard", "true", null, null, null, null, null, null, null, null);
//            endpoints = api.readNamespacedEndpoints("kubernetes-dashboard", "kubernetes-dashboard", null, null, null);
//
//            v1ServiceList = api.listNamespacedService("kubernetes-dashboard", "true", null, null, null, null, null, null, null, null);
//
//            v1Service = api.readNamespacedService("kubernetes-dashboard", "kubernetes-dashboard", "true", null, null);
//
//        } catch (ApiException e) {
//            e.printStackTrace();
//        }
//
//
//        // invokes the CoreV1Api client
//        try {
//            V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);
//            System.out.println(list);
//        } catch (ApiException e) {
//            log.error("获取podlist异常:" + e.getResponseBody(), e);
//        }
//    }
//
//    private ApiClient generateClientByConf(String conf) {
//        if (StringUtils.isEmpty(conf)) {
//            return null;
//        }
//        try {
//            return ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new StringReader(conf))).build();
//        } catch (IOException e) {
//            log.error("generate k8s Client By Conf error", e);
//        }
//        return null;
//    }
//
//    private ApiClient generateClientBySecret(String secret, String server) {
//        if (StringUtils.isEmpty(secret) || StringUtils.isEmpty(server)) {
//            return null;
//        }
//        return new ClientBuilder().setBasePath(server).setVerifyingSsl(false)
//                .setAuthentication(new AccessTokenAuthentication(secret)).build();
//    }
//
//    @Override
//    public List<ClusterServer> clusterServers(RegistryConfig config) {
//        if (!initConfiguration(config)) {
//            return Collections.emptyList();
//        }
//        List<ClusterServer> clusterServers = new ArrayList<>();
//        CoreV1Api api = new CoreV1Api();
//        V1NodeList v1NodeList = null;
//        try {
//            v1NodeList = api.listNode("true", null, null, null, null, null, null, null, null);
//        } catch (ApiException e) {
//            e.printStackTrace();
//        }
//        if (Optional.of(v1NodeList).map(v -> v.getItems()).isPresent()) {
//            List<V1Node> v1Nodes = v1NodeList.getItems();
//            v1Nodes.stream().forEach(v -> {
//                ClusterServer server = new ClusterServer();
//                V1NodeStatus v1NodeStatus = v.getStatus();
//                List<V1NodeAddress> v1NodeAddresses = v1NodeStatus.getAddresses();
//                v1NodeAddresses.stream().forEach(a -> {
//                    if ("InternalIP".equalsIgnoreCase(a.getType())) {
//                        server.setServerAddr(a.getAddress());
//                    }
//                });
//            });
//        }
//        return clusterServers;
//    }
//
//    @Override
//    public void freshServiceMapCache(RegistryConfig config) {
//
//    }
//
//    @Override
//    public ServerMetrics fetchServerMetrics(RegistryConfig config) {
//        return null;
//    }
//
//    @Override
//    public RegistryPageService fetchServices(RegistryConfig config, RegistryInstanceParam registryInstanceParam) {
//        CoreV1Api apiInstance = new CoreV1Api();
//        RegistryPageService registryPageService = new RegistryPageService();
//        int count = 0;
//        List<ServiceBriefInfo> serviceBriefInfos = new ArrayList<ServiceBriefInfo>();
//        try {
//            V1ServiceList v1ServiceList = apiInstance.listNamespacedService(registryInstanceParam.getNamespaceId(), "true", null, null, null, null, null, null, null, null);
//            List<V1Service> services = v1ServiceList.getItems();
//            if (!CollectionUtils.isEmpty(services)) {
//                count = services.size();
//                services.stream().forEach(s -> {
//                    V1ObjectMeta meta = s.getMetadata();
//                    ServiceBriefInfo briefInfo = new ServiceBriefInfo();
//                    briefInfo.setServiceName(meta.getName());
////                    briefInfo.setVersionNum("");
//                    briefInfo.setStatus("");
//                });
//            }
//        } catch (ApiException e) {
//            e.printStackTrace();
//        }
//        registryPageService.setPageNo(count);
//        registryPageService.setPageNo(registryInstanceParam.getPageNo());
//        registryPageService.setPageSize(registryInstanceParam.getPageSize());
//        registryPageService.setServiceBriefInfos(serviceBriefInfos);
//        return registryPageService;
//    }
//
//    @Override
//    public RegistryPageService fetchNamespaceServices(RegistryConfig config, String namespaceId, int pageNo, int pageSize) {
//        return null;
//    }
//
//    @Override
//    public List<ServiceInstance> fetchServiceInstances(RegistryConfig config, RegistryInstanceParam registryInstanceParam) {
//        CoreV1Api apiInstance = new CoreV1Api();
//        List<ServiceInstance> serviceInstances = new ArrayList<ServiceInstance>();
//        V1Endpoints endpoints = null;
//        try {
//            final V1Service service = apiInstance.readNamespacedService(registryInstanceParam.getServiceName(), registryInstanceParam.getNamespaceId(), "true", null, null);
//            if (service == null || !matchServiceLabels(service)) {
//                // no such service present in the cluster
//                return new ArrayList<>();
//            }
//            endpoints = apiInstance.readNamespacedEndpoints(registryInstanceParam.getServiceName(), registryInstanceParam.getNamespaceId(), "true", null, null);
//            endpoints.getSubsets();
//            Map<String, String> svcMetadata = new HashMap<>();
//            if (this.properties.getMetadata() != null) {
//                if (this.properties.getMetadata().isAddLabels()) {
//                    if (service.getMetadata() != null && service.getMetadata().getLabels() != null) {
//                        String labelPrefix = this.properties.getMetadata().getLabelsPrefix() != null
//                                ? this.properties.getMetadata().getLabelsPrefix() : "";
//                        service.getMetadata().getLabels().entrySet().stream()
//                                .filter(e -> e.getKey().startsWith(labelPrefix))
//                                .forEach(e -> svcMetadata.put(e.getKey(), e.getValue()));
//                    }
//                }
//                if (this.properties.getMetadata().isAddAnnotations()) {
//                    if (service.getMetadata() != null && service.getMetadata().getAnnotations() != null) {
//                        String annotationPrefix = this.properties.getMetadata().getAnnotationsPrefix() != null
//                                ? this.properties.getMetadata().getAnnotationsPrefix() : "";
//                        service.getMetadata().getAnnotations().entrySet().stream()
//                                .filter(e -> e.getKey().startsWith(annotationPrefix))
//                                .forEach(e -> svcMetadata.put(e.getKey(), e.getValue()));
//                    }
//                }
//            }
//            Optional<String> discoveredPrimaryPortName = Optional.empty();
//            if (service.getMetadata() != null && service.getMetadata().getLabels() != null) {
//                discoveredPrimaryPortName = Optional
//                        .ofNullable(service.getMetadata().getLabels().get(PRIMARY_PORT_NAME_LABEL_KEY));
//            }
//            final String primaryPortName = discoveredPrimaryPortName.orElse(this.properties.getPrimaryPortName());
//            serviceInstances = endpoints.getSubsets().stream().filter(subset -> subset.getPorts() != null && subset.getPorts().size() > 0)
//                    .flatMap(subset -> {
//                        Map<String, String> metadata = new HashMap<>(svcMetadata);
//                        List<V1EndpointPort> endpointPorts = subset.getPorts();
//                        if (this.properties.getMetadata() != null && this.properties.getMetadata().isAddPorts()) {
//                            endpointPorts.forEach(p -> metadata.put(p.getName(), Integer.toString(p.getPort())));
//                        }
//                        List<V1EndpointAddress> addresses = subset.getAddresses();
//                        if (addresses == null) {
//                            addresses = new ArrayList<>();
//                        }
//                        if (this.properties.isIncludeNotReadyAddresses()
//                                && !org.springframework.util.CollectionUtils.isEmpty(subset.getNotReadyAddresses())) {
//                            addresses.addAll(subset.getNotReadyAddresses());
//                        }
//                        final int port = findEndpointPort(endpointPorts, primaryPortName, registryInstanceParam.getServiceName());
//                        return addresses.stream()
//                                .map(addr -> new ServiceInstance(new Service(service.getMetadata().getNamespace(), registryInstanceParam.getServiceName()),
//                                        addr.getTargetRef() != null ? addr.getTargetRef().getUid() : "",
//                                        addr.getIp(), port, EndpointStatus.UP, metadata));
//                    }).collect(Collectors.toList());
//        } catch (ApiException e) {
//        } catch (Exception e) {
//        }
//        return serviceInstances;
//    }
//
//    private int findEndpointPort(List<V1EndpointPort> endpointPorts, String primaryPortName, String serviceId) {
//        if (endpointPorts.size() == 1) {
//            return endpointPorts.get(0).getPort();
//        } else {
//            Map<String, Integer> ports = endpointPorts.stream().filter(p -> org.springframework.util.StringUtils.hasText(p.getName()))
//                    .collect(Collectors.toMap(V1EndpointPort::getName, V1EndpointPort::getPort));
//            // This oneliner is looking for a port with a name equal to the primary port
//            // name specified in the service label
//            // or in spring.cloud.kubernetes.discovery.primary-port-name, equal to https,
//            // or equal to http.
//            // In case no port has been found return -1 to log a warning and fall back to
//            // the first port in the list.
//            int discoveredPort = ports.getOrDefault(primaryPortName,
//                    ports.getOrDefault(HTTPS_PORT_NAME, ports.getOrDefault(HTTP_PORT_NAME, -1)));
//
//            if (discoveredPort == -1) {
//                if (org.springframework.util.StringUtils.hasText(primaryPortName)) {
//                    log.warn("Could not find a port named '" + primaryPortName + "', 'https', or 'http' for service '"
//                            + serviceId + "'.");
//                } else {
//                    log.warn("Could not find a port named 'https' or 'http' for service '" + serviceId + "'.");
//                }
//                log.warn(
//                        "Make sure that either the primary-port-name label has been added to the service, or that spring.cloud.kubernetes.discovery.primary-port-name has been configured.");
//                log.warn("Alternatively name the primary port 'https' or 'http'");
//                log.warn("An incorrect configuration may result in non-deterministic behaviour.");
//                discoveredPort = endpointPorts.get(0).getPort();
//            }
//            return discoveredPort;
//        }
//    }
//
//    private boolean matchServiceLabels(V1Service service) {
//        if (log.isDebugEnabled()) {
//            log.debug("Kubernetes Service Label Properties:");
//            if (this.properties.getServiceLabels() != null) {
//                this.properties.getServiceLabels().forEach((key, value) -> log.debug(key + ":" + value));
//            }
//            log.debug("Service " + service.getMetadata().getName() + " labels:");
//            if (service.getMetadata() != null && service.getMetadata().getLabels() != null) {
//                service.getMetadata().getLabels().forEach((key, value) -> log.debug(key + ":" + value));
//            }
//        }
//        // safeguard
//        if (service.getMetadata() == null) {
//            return false;
//        }
//        if (properties.getServiceLabels() == null || properties.getServiceLabels().isEmpty()) {
//            return true;
//        }
//        return properties.getServiceLabels().keySet().stream()
//                .allMatch(k -> service.getMetadata().getLabels() != null
//                        && service.getMetadata().getLabels().containsKey(k)
//                        && service.getMetadata().getLabels().get(k).equals(properties.getServiceLabels().get(k)));
//    }
//
//    @Override
//    public boolean healthCheck(String url) {
//        return true;
//    }
//
//    @Override
//    public boolean createNamespace(RegistryConfig config, Namespace namespace) {
//        return true;
//    }
//
//    @Override
//    public boolean deleteNamespace(RegistryConfig config, Namespace namespace) {
//        return true;
//    }
//}
