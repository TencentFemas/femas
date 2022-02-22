package com.tencent.tsf.femas.registry.impl.k8s.discovery;


import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.discovery.AbstractServiceDiscoveryClient;
import com.tencent.tsf.femas.common.discovery.SchedulePollingServerListUpdater;
import com.tencent.tsf.femas.common.discovery.ServerUpdater;
import com.tencent.tsf.femas.common.entity.EndpointStatus;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.kubernetes.*;
import com.tencent.tsf.femas.common.serialize.JSONSerializer;
import com.tencent.tsf.femas.registry.impl.k8s.K8sRegistryBuilder;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/24 15:24
 * @Version 1.0
 */
public class K8sServiceDiscoveryClient extends AbstractServiceDiscoveryClient {

    private final static Logger LOGGER = LoggerFactory.getLogger(K8sServiceDiscoveryClient.class);
    private final KubernetesClient client;
    private static volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();
    private final static String default_namespace = "public";
    protected volatile ServerUpdater serverListUpdater;
    private final Map<Service, List<ServiceInstance>> instances = new ConcurrentHashMap<>();
    protected AtomicBoolean serverListUpdateInProgress;
    private final K8sRegistryBuilder builder;

    public static final String FEMAS_META_K8S_KEY = "femas-service-metadata";

    public static final String FEMAS_K8S_SELECT_LABEL_KEY = "femas-service-app=";
    private static final String PRIMARY_PORT_NAME_LABEL_KEY = "primary-port-name";
    public static final String NAMESPACE_METADATA_KEY = "k8s_namespace";

    private static final String HTTPS_PORT_NAME = "https";

    private static final String HTTP_PORT_NAME = "http";

    private final KubernetesDiscoveryProperties properties;

    private ServicePortSecureResolver servicePortSecureResolver;

    private final KubernetesServerList serverListImpl;

    private Map<Service, Notifier> notifiers = new ConcurrentHashMap<>();

    public K8sServiceDiscoveryClient(Map<String, String> configMap) {
        this.serverListUpdateInProgress = new AtomicBoolean(false);
        this.builder = new K8sRegistryBuilder();
        String namespace = Context.getSystemTag(contextConstant.getNamespaceId());
        this.client = builder.build(null, null);
        this.serverListUpdater = new SchedulePollingServerListUpdater();
        this.serverListImpl = new KubernetesServerList();
        this.properties = builder.getKubernetesDiscoveryProperties();
        this.servicePortSecureResolver = new ServicePortSecureResolver(properties);
    }

    public void updateListOfServers(Service service) {
        List<org.springframework.cloud.client.ServiceInstance> instances = new ArrayList();
        if (this.serverListImpl != null) {
            instances = this.serverListImpl.getUpdatedListOfServers(Optional.ofNullable(service).map(s -> s.getName()).get());
//            LOGGER.debug("List of Servers for {} obtained from Discovery client: {}", this.getIdentifier(), servers);
        }

        this.updateAllServerList(service, instances);
    }

    protected void updateAllServerList(Service service, List<org.springframework.cloud.client.ServiceInstance> ls) {
        if (this.serverListUpdateInProgress.compareAndSet(false, true)) {
            try {
                List<ServiceInstance> newInstances = convert(service, ls);
                List<ServiceInstance> oldInstances = instances.get(service);
                this.refreshServiceCache(service, newInstances);
                this.notifyListeners(service, newInstances, oldInstances);
            } finally {
                this.serverListUpdateInProgress.set(false);
            }
        }
    }

    private void refreshServiceCache(Service service, List<ServiceInstance> instances) {
        this.instances.put(service, instances);
    }

    List<ServiceInstance> convert(Service service, List<org.springframework.cloud.client.ServiceInstance> ls) {
        List<ServiceInstance> instances = new ArrayList<>();
        ls.stream().forEach(i -> {
            ServiceInstance instance = new ServiceInstance();
            instance.setAllMetadata(i.getMetadata());
            instance.setHost(i.getHost());
            instance.setPort(i.getPort());
            instance.setService(service);
            instance.setStatus(EndpointStatus.UP);
            instances.add(instance);
        });
        return instances;
    }

    public ScheduledFuture enableAndInitLearnNewServersFeature(Service service) {
        LOGGER.info("Using serverListUpdater {}", this.serverListUpdater.getClass().getSimpleName());
        ScheduledFuture scheduledFuture = this.serverListUpdater.start(new Action(service));
        return scheduledFuture;
    }

    @Override
    protected void doSubscribe(Service service) {
        Notifier notifier = new Notifier(service);
        notifier.run();
        notifiers.putIfAbsent(service, notifier);
    }

    @Override
    protected void doUnSubscribe(Service service) {
        serverListUpdater.stop(notifiers.get(service).scheduledFuture);
        notifiers.remove(service);
    }

    @Override
    public List<ServiceInstance> getInstances(Service service) {
        List<ServiceInstance> instancesList = instances.get(service);
        if (instancesList != null) {
            return instancesList;
        }
        List<org.springframework.cloud.client.ServiceInstance> instances = serverListImpl.getInitialListOfServers(service.getName());
        instancesList = convert(service, instances);
        refreshServiceCache(service, instancesList);
        return instancesList;
    }

    class Action implements ServerUpdater.UpdateAction {

        private final Service service;

        Action(Service service) {
            this.service = service;
        }

        public void doUpdate() {
            K8sServiceDiscoveryClient.this.updateListOfServers(service);
        }
    }

    //server级别监听
    class KubernetesServerList {

        private final Logger log = LoggerFactory.getLogger(KubernetesServerList.class);

        public List<org.springframework.cloud.client.ServiceInstance> getInitialListOfServers(String serviceId) {
            return getServers(serviceId);
        }

        public List<org.springframework.cloud.client.ServiceInstance> getUpdatedListOfServers(String serviceId) {
            return getServers(serviceId);
        }

        private List<org.springframework.cloud.client.ServiceInstance> getServers(String serviceId) {
            try {
                Assert.notNull(serviceId, "[Assertion failed] - the object argument must not be null");
                List<EndpointSubsetNS> subsetsNS = this.getEndPointsList(serviceId).stream().map(this::getSubsetsFromEndpoints)
                        .collect(Collectors.toList());
                List<org.springframework.cloud.client.ServiceInstance> instances = new ArrayList<>();
                if (!subsetsNS.isEmpty()) {
                    for (EndpointSubsetNS es : subsetsNS) {
                        instances.addAll(this.getNamespaceServiceInstances(es, serviceId));
                    }
                }
                return instances;
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Can not get service instances from nacos, serviceId=" + serviceId,
                        e);
            }
        }

        public List<Endpoints> getEndPointsList(String serviceId) {
            return properties.isAllNamespaces()
                    ? client.endpoints().inAnyNamespace().withField("metadata.name", serviceId)
                    .withLabels(properties.getServiceLabels()).list().getItems()
                    : client.endpoints().withField("metadata.name", serviceId)
                    .withLabels(properties.getServiceLabels()).list().getItems();
        }

        private EndpointSubsetNS getSubsetsFromEndpoints(Endpoints endpoints) {
            EndpointSubsetNS es = new EndpointSubsetNS();
            es.setNamespace(client.getNamespace()); // start with the default that comes
            // with the client

            if (endpoints != null && endpoints.getSubsets() != null) {
                es.setNamespace(endpoints.getMetadata().getNamespace());
                es.setEndpointSubset(endpoints.getSubsets());
            }

            return es;
        }

        private List<org.springframework.cloud.client.ServiceInstance> getNamespaceServiceInstances(EndpointSubsetNS es, String serviceId) {
            String namespace = es.getNamespace();
            List<EndpointSubset> subsets = es.getEndpointSubset();
            List<org.springframework.cloud.client.ServiceInstance> instances = new ArrayList<>();
            if (!subsets.isEmpty()) {
                final io.fabric8.kubernetes.api.model.Service service = client.services().inNamespace(namespace).withName(serviceId).get();
                final Map<String, String> serviceMetadata = this.getServiceMetadata(service);
                KubernetesDiscoveryProperties.Metadata metadataProps = properties.getMetadata();
                //补齐endpoint的元数据
                ListOptions options = new ListOptions();
                StringBuffer label = new StringBuffer(FEMAS_K8S_SELECT_LABEL_KEY);
                label.append(serviceId);
                options.setLabelSelector(label.toString());
                final PodList podList = client.pods().list(options);
                String primaryPortName = properties.getPrimaryPortName();
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
                    if (properties.isAllNamespaces()) {
                        endpointMetadata.put(NAMESPACE_METADATA_KEY, namespace);
                    }
                    List<EndpointAddress> addresses = s.getAddresses();
                    if (properties.isIncludeNotReadyAddresses()
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
                                servicePortSecureResolver.resolve(new ServicePortSecureResolver.Input(endpointPort,
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


        private Map<String, String> getServiceMetadata(io.fabric8.kubernetes.api.model.Service service) {
            final Map<String, String> serviceMetadata = new HashMap<>();
            KubernetesDiscoveryProperties.Metadata metadataProps = properties.getMetadata();
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


    private class Notifier {
        private final Service service;
        private ScheduledFuture<?> scheduledFuture;

        public Notifier(Service service) {
            this.service = service;
        }

        public void run() {
            this.scheduledFuture = enableAndInitLearnNewServersFeature(service);

        }
    }
}