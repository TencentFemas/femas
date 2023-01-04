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
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.kubernetes.*;
import com.tencent.tsf.femas.common.serialize.JSONSerializer;
import com.tencent.tsf.femas.entity.param.RegistryInstanceParam;
import com.tencent.tsf.femas.entity.registry.ClusterServer;
import com.tencent.tsf.femas.entity.registry.RegistryConfig;
import com.tencent.tsf.femas.entity.registry.RegistryPageService;
import com.tencent.tsf.femas.entity.registry.ServiceBriefInfo;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.VersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.tencent.tsf.femas.constant.AdminConstants.*;
import static java.util.stream.Collectors.toMap;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/11/15 19:14
 */
@Component
public class KubernetesFabricRegistryOpenApi extends RegistryOpenApiAdaptor {

    private static final Logger log = LoggerFactory.getLogger(KubernetesFabricRegistryOpenApi.class);
    private final static int maximumSize = 6;
    //限制大小的FIFO队列，防止变更过多内存爆掉
    private final LinkedHashMap<RegistryConfig, KubernetesClient> clientMap = new LinkedHashMap<RegistryConfig, KubernetesClient>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<RegistryConfig, KubernetesClient> eldest) {
            return size() > maximumSize;
        }
    };
    private final KubernetesDiscoveryProperties properties;
    private static final String PRIMARY_PORT_NAME_LABEL_KEY = "primary-port-name";
    public static final String NAMESPACE_METADATA_KEY = "k8s_namespace";

    private final KubernetesClient defaultClient;

    private static final String HTTPS_PORT_NAME = "https";

    private static final String HTTP_PORT_NAME = "http";

    private ServicePortSecureResolver servicePortSecureResolver;
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final KubernetesClientServicesFunction kubernetesClientServicesFunction;
    private final SimpleEvaluationContext evalCtxt = SimpleEvaluationContext.forReadOnlyDataBinding()
            .withInstanceMethods().build();

    public KubernetesFabricRegistryOpenApi(KubernetesClient defaultClient, KubernetesDiscoveryProperties properties, KubernetesClientServicesFunction kubernetesClientServicesFunction) {
        this.defaultClient = defaultClient;
        this.properties = properties;
        this.servicePortSecureResolver = new ServicePortSecureResolver(properties);
        this.kubernetesClientServicesFunction = kubernetesClientServicesFunction;
    }


    private KubernetesClient generateClientByConf(String conf) {
        if (StringUtils.isEmpty(conf)) {
            return null;
        }
        try {
            Config config = Config.fromKubeconfig(conf);
            return new DefaultKubernetesClient(config);
        } catch (IOException e) {
            log.error("generate Kubernetes Client By Conf failed", e);
        }
        return null;
    }

    private KubernetesClient generateClientBySecret(String secret, String server) {
        if (StringUtils.isEmpty(secret) || StringUtils.isEmpty(server)) {
            return null;
        }
        return null;
    }

    private KubernetesClient getApiClient(RegistryConfig config) {
        KubernetesClient client = clientMap.get(config);
        if (client != null) {
            return client;
        }
        if (Optional.of(config).map(c -> c.getCertificateType()).isPresent()) {
            if (config.getCertificateType().equalsIgnoreCase("config")) {
                client = generateClientByConf(config.getKubeConfig());
            }
            if (config.getCertificateType().equalsIgnoreCase("account")) {
                client = generateClientBySecret(config.getSecret(), config.getApiServerAddr());
            }
        }
        clientMap.put(config, client == null ? defaultClient : client);
        return getApiClient(config);
    }

    @Override
    public List<ClusterServer> clusterServers(RegistryConfig config) {
        List<ClusterServer> clusterServers = new ArrayList<>();
        NodeList nodeList = getApiClient(config).nodes().list();
        if (Optional.of(nodeList).map(v -> v.getItems()).isPresent()) {
            List<Node> v1Nodes = nodeList.getItems();
            v1Nodes.stream().forEach(v -> {
                ClusterServer server = new ClusterServer();
                NodeStatus v1NodeStatus = v.getStatus();
                List<NodeAddress> v1NodeAddresses = v1NodeStatus.getAddresses();
                v1NodeAddresses.stream().forEach(a -> {
                    if ("InternalIP".equalsIgnoreCase(a.getType())) {
                        server.setServerAddr(a.getAddress());
                    }
                });
                server.setState("UP");
                clusterServers.add(server);
            });
        }
        return clusterServers;
    }


    @Override
    public boolean healthCheck(RegistryConfig config) {
        VersionInfo versionInfo = getApiClient(config).getVersion();
        return Optional.of(versionInfo).map(VersionInfo::getMajor).isPresent();
    }

    @Override
    public RegistryPageService fetchServices(RegistryConfig config, RegistryInstanceParam registryInstanceParam) {
        RegistryPageService registryPageService = new RegistryPageService();
        registryPageService.setPageNo(registryInstanceParam.getPageNo());
        registryPageService.setPageSize(registryInstanceParam.getPageSize());
        String spelExpression = this.properties.getFilter();
        Predicate<Service> filteredServices;
        if (spelExpression == null || spelExpression.isEmpty()) {
            filteredServices = (Service instance) -> true;
        } else {
            Expression filterExpr = this.parser.parseExpression(spelExpression);
            filteredServices = (Service instance) -> {
                Boolean include = filterExpr.getValue(this.evalCtxt, instance, Boolean.class);
                if (include == null) {
                    return false;
                }
                return include;
            };
        }
        List<ServiceBriefInfo> serviceNames = getServices(filteredServices, config);
        registryPageService.setCount(serviceNames.size());
        registryPageService.setServiceBriefInfos(pageList(serviceNames, registryInstanceParam.getPageNo(), registryInstanceParam.getPageSize()));
        return registryPageService;
    }

    public List<ServiceBriefInfo> getServices(Predicate<Service> filter, RegistryConfig config) {
        return this.kubernetesClientServicesFunction.apply(getApiClient(config)).list().getItems().stream().filter(filter)
                .map(s -> new ServiceBriefInfo(s.getMetadata().getName(), "up")).collect(Collectors.toList());
    }

    @Override
    public RegistryPageService fetchNamespaceServices(RegistryConfig config, String namespaceId, int pageNo, int pageSize) {
        return null;
    }

    @Override
    public List<ServiceInstance> fetchServiceInstances(RegistryConfig config, RegistryInstanceParam registryInstanceParam) {
        List<ServiceInstance> serviceInstances = new ArrayList<ServiceInstance>();
        Assert.notNull(registryInstanceParam.getServiceName(), "[Assertion failed] - the object argument must not be null");
        List<EndpointSubsetNS> subsetsNS = this.getEndPointsList(registryInstanceParam.getServiceName(), config).stream().map((Endpoints endpoints) -> getSubsetsFromEndpoints(endpoints, config))
                .collect(Collectors.toList());
        List<org.springframework.cloud.client.ServiceInstance> instances = new ArrayList<>();
        if (!subsetsNS.isEmpty()) {
            for (EndpointSubsetNS es : subsetsNS) {
                instances.addAll(this.getNamespaceServiceInstances(es, registryInstanceParam.getServiceName(), config));
            }
        }
        instances.stream().forEach(i -> {
            ServiceInstance instance = new ServiceInstance();
            instance.setAllMetadata(i.getMetadata());
            instance.setId(i.getInstanceId());
            instance.setHost(i.getHost());
            instance.setPort(i.getPort());
            instance.setStatus(EndpointStatus.UP);
            instance.setServiceVersion(i.getMetadata().get(FEMAS_META_APPLICATION_VERSION_KEY));
            instance.setService(new com.tencent.tsf.femas.common.entity.Service(i.getMetadata().get(FEMAS_META_NAMESPACE_ID_KEY), registryInstanceParam.getServiceName()));
            serviceInstances.add(instance);
        });
        return serviceInstances;
    }

    @Override
    public void freshServiceMapCache(RegistryConfig config) {

    }

    @Override
    public boolean healthCheck(String url) {
        return false;
    }

    public List<Endpoints> getEndPointsList(String serviceId, RegistryConfig config) {
        return this.properties.isAllNamespaces()
                ? getApiClient(config).endpoints().inAnyNamespace().withField("metadata.name", serviceId)
                .withLabels(properties.getServiceLabels()).list().getItems()
                : getApiClient(config).endpoints().withField("metadata.name", serviceId)
                .withLabels(properties.getServiceLabels()).list().getItems();
    }

    private EndpointSubsetNS getSubsetsFromEndpoints(Endpoints endpoints, RegistryConfig config) {
        EndpointSubsetNS es = new EndpointSubsetNS();
        es.setNamespace(getApiClient(config).getNamespace()); // start with the default that comes
        // with the client

        if (endpoints != null && endpoints.getSubsets() != null) {
            es.setNamespace(endpoints.getMetadata().getNamespace());
            es.setEndpointSubset(endpoints.getSubsets());
        }

        return es;
    }

    private List<org.springframework.cloud.client.ServiceInstance> getNamespaceServiceInstances(EndpointSubsetNS es, String serviceId, RegistryConfig config) {
        String namespace = es.getNamespace();
        List<EndpointSubset> subsets = es.getEndpointSubset();
        List<org.springframework.cloud.client.ServiceInstance> instances = new ArrayList<>();
        if (!subsets.isEmpty()) {
            final Service service = getApiClient(config).services().inNamespace(namespace).withName(serviceId).get();
            //补齐endpoint的元数据
            ListOptions options = new ListOptions();
            StringBuffer label = new StringBuffer(FEMAS_K8S_SELECT_LABEL_KEY);
            label.append(serviceId);
            options.setLabelSelector(label.toString());
            final PodList podList = getApiClient(config).pods().list(options);

            final Map<String, String> serviceMetadata = this.getServiceMetadata(service);
            KubernetesDiscoveryProperties.Metadata metadataProps = this.properties.getMetadata();

            String primaryPortName = this.properties.getPrimaryPortName();
            Map<String, String> labels = service.getMetadata().getLabels();
            if (labels != null && labels.containsKey(PRIMARY_PORT_NAME_LABEL_KEY)) {
                primaryPortName = labels.get(PRIMARY_PORT_NAME_LABEL_KEY);
            }

            for (EndpointSubset s : subsets) {
                // Extend the service metadata map with per-endpoint port information (if
                // requested)
                Map<String, String> endpointMetadata = new HashMap<>(serviceMetadata);
                if (metadataProps.isAddPorts()) {
                    Map<String, String> ports = s.getPorts().stream()
                            .filter(port -> StringUtils.hasText(port.getName()))
                            .collect(toMap(EndpointPort::getName, port -> Integer.toString(port.getPort())));
                    Map<String, String> portMetadata = getMapWithPrefixedKeys(ports, metadataProps.getPortsPrefix());
                    if (log.isDebugEnabled()) {
                        log.debug("Adding port metadata: " + portMetadata);
                    }
                    endpointMetadata.putAll(portMetadata);
                }

                if (this.properties.isAllNamespaces()) {
                    endpointMetadata.put(NAMESPACE_METADATA_KEY, namespace);
                }
                List<EndpointAddress> addresses = s.getAddresses();
                if (this.properties.isIncludeNotReadyAddresses()
                        && !CollectionUtils.isEmpty(s.getNotReadyAddresses())) {
                    if (addresses == null) {
                        addresses = new ArrayList<>();
                    }
                    addresses.addAll(s.getNotReadyAddresses());
                }

                for (EndpointAddress endpointAddress : addresses) {
                    int endpointPort = findEndpointPort(s, serviceId, primaryPortName);
                    String instanceId = null;
                    if (endpointAddress.getTargetRef() != null) {
                        instanceId = endpointAddress.getTargetRef().getUid();
                    }
                    final Map<String, String> metadata = new HashMap<>();
                    if (Optional.of(podList).map(p -> p.getItems()).isPresent()) {
                        List<Pod> pods = podList.getItems();
                        pods.stream().forEach(p -> {
                            String podId = Optional.of(p.getStatus()).map(sa -> sa.getPodIP()).get();
                            if (!StringUtils.isEmpty(podId) && podId.equalsIgnoreCase(endpointAddress.getIp())) {
                                ObjectMeta objectMeta = p.getMetadata();
                                Map<String, String> metaMap = objectMeta.getAnnotations();
                                String metaStr = metaMap.get(FEMAS_META_K8S_KEY);
                                if (!StringUtils.isEmpty(metaStr)) {
                                    Map<String, String> stringMap = JSONSerializer.deserializeStr(Map.class, metaStr);
                                    metadata.putAll(stringMap);
                                }
                                metadata.putAll(endpointMetadata);
                            }
                        });
                    }
                    instances.add(new KubernetesServiceInstance(instanceId, serviceId, endpointAddress.getIp(),
                            endpointPort, metadata,
                            this.servicePortSecureResolver.resolve(new ServicePortSecureResolver.Input(endpointPort,
                                    service.getMetadata().getName(), service.getMetadata().getLabels(),
                                    service.getMetadata().getAnnotations()))));
                }
            }
        }
        return instances;
    }

    private Map<String, String> getMapWithPrefixedKeys(Map<String, String> map, String prefix) {
        if (map == null) {
            return new HashMap<>();
        }

        // when the prefix is empty just return an map with the same entries
        if (!StringUtils.hasText(prefix)) {
            return map;
        }

        final Map<String, String> result = new HashMap<>();
        map.forEach((k, v) -> result.put(prefix + k, v));

        return result;
    }


    private Map<String, String> getServiceMetadata(Service service) {
        final Map<String, String> serviceMetadata = new HashMap<>();
        KubernetesDiscoveryProperties.Metadata metadataProps = this.properties.getMetadata();
        if (metadataProps.isAddLabels()) {
            Map<String, String> labelMetadata = getMapWithPrefixedKeys(service.getMetadata().getLabels(),
                    metadataProps.getLabelsPrefix());
            if (log.isDebugEnabled()) {
                log.debug("Adding label metadata: " + labelMetadata);
            }
            serviceMetadata.putAll(labelMetadata);
        }
        if (metadataProps.isAddAnnotations()) {
            Map<String, String> annotationMetadata = getMapWithPrefixedKeys(service.getMetadata().getAnnotations(),
                    metadataProps.getAnnotationsPrefix());
            if (log.isDebugEnabled()) {
                log.debug("Adding annotation metadata: " + annotationMetadata);
            }
            serviceMetadata.putAll(annotationMetadata);
        }

        return serviceMetadata;
    }

    private int findEndpointPort(EndpointSubset s, String serviceId, String primaryPortName) {
        List<EndpointPort> endpointPorts = s.getPorts();
        if (endpointPorts.size() == 1) {
            return endpointPorts.get(0).getPort();
        } else {
            Map<String, Integer> ports = endpointPorts.stream().filter(p -> StringUtils.hasText(p.getName()))
                    .collect(Collectors.toMap(EndpointPort::getName, EndpointPort::getPort));
            // This oneliner is looking for a port with a name equal to the primary port
            // name specified in the service label
            // or in spring.cloud.kubernetes.discovery.primary-port-name, equal to https,
            // or equal to http.
            // In case no port has been found return -1 to log a warning and fall back to
            // the first port in the list.
            int discoveredPort = ports.getOrDefault(primaryPortName,
                    ports.getOrDefault(HTTPS_PORT_NAME, ports.getOrDefault(HTTP_PORT_NAME, -1)));

            if (discoveredPort == -1) {
                if (StringUtils.hasText(primaryPortName)) {
                    log.warn("Could not find a port named '" + primaryPortName + "', 'https', or 'http' for service '"
                            + serviceId + "'.");
                } else {
                    log.warn("Could not find a port named 'https' or 'http' for service '" + serviceId + "'.");
                }
                log.warn(
                        "Make sure that either the primary-port-name label has been added to the service, or that spring.cloud.kubernetes.discovery.primary-port-name has been configured.");
                log.warn("Alternatively name the primary port 'https' or 'http'");
                log.warn("An incorrect configuration may result in non-deterministic behaviour.");
                discoveredPort = endpointPorts.get(0).getPort();
            }
            return discoveredPort;
        }
    }

}
