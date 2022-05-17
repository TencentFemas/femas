package com.tencent.tsf.femas.registry.impl.etcd.discovery;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tsf.femas.common.RegistryConstants;
import com.tencent.tsf.femas.common.discovery.AbstractServiceDiscoveryClient;
import com.tencent.tsf.femas.common.discovery.SchedulePollingServerListUpdater;
import com.tencent.tsf.femas.common.discovery.ServerUpdater;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author huyuanxin
 */
public class EtcdServiceDiscoveryClient extends AbstractServiceDiscoveryClient {

    private static final Logger logger = LoggerFactory.getLogger(EtcdServiceDiscoveryClient.class);

    private static final String KEY_STRING_PREFIX_FORMAT = "%s-%s";

    private final Map<Service, List<ServiceInstance>> instances = new ConcurrentHashMap<>();

    private final KV kvClient;

    private final ObjectMapper objectMapper;

    private final AtomicBoolean serverListUpdateInProgress = new AtomicBoolean(false);

    private final EtcdServerList etcdServerList;

    private final Map<Service, Notifier> notifiers = new ConcurrentHashMap<>();

    protected final ServerUpdater serverListUpdater;

    public EtcdServiceDiscoveryClient(Map<String, String> configMap) {
        Client client = Client.builder().endpoints(configMap.get(RegistryConstants.REGISTRY_HOST)).build();
        this.kvClient = client.getKVClient();
        this.objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.serverListUpdater = new SchedulePollingServerListUpdater();
        this.etcdServerList = new EtcdServerList();
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

    /**
     * Get all ServiceInstances associated with a particular service
     *
     * @param service the service to query
     * @return a List of ServiceInstance
     */
    @Override
    public List<ServiceInstance> getInstances(Service service) {
        List<ServiceInstance> instancesList = instances.get(service);
        if (instancesList != null) {
            return instancesList;
        }
        instancesList = etcdServerList.getInitialListOfServers(service);
        refreshServiceCache(service, instancesList);
        return instancesList;
    }

    @Override
    public List<String> getAllServices() {
        CompletableFuture<GetResponse> future = kvClient.get(ByteSequence.EMPTY);
        List<String> services = new ArrayList<>();

        try {
            List<KeyValue> keyValues = future.get().getKvs();

            for (KeyValue keyValue : keyValues) {
                ServiceInstance serviceInstance = objectMapper.readValue(keyValue.getValue().toString(), ServiceInstance.class);
                services.add(serviceInstance.getService().getName());
            }

        } catch (InterruptedException | ExecutionException | JsonProcessingException e) {
            e.printStackTrace();
        }

        return services;
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

    class Action implements ServerUpdater.UpdateAction {

        private final Service service;

        Action(Service service) {
            this.service = service;
        }

        public void doUpdate() {
            EtcdServiceDiscoveryClient.this.updateListOfServers(service);
        }
    }

    protected void updateAllServerList(Service service, List<ServiceInstance> serviceInstances) {
        if (this.serverListUpdateInProgress.compareAndSet(false, true)) {
            try {
                List<ServiceInstance> oldInstances = instances.get(service);
                this.refreshServiceCache(service, serviceInstances);
                this.notifyListeners(service, serviceInstances, oldInstances);
            } finally {
                this.serverListUpdateInProgress.set(false);
            }
        }
    }

    class EtcdServerList {
        public List<ServiceInstance> getInitialListOfServers(Service service) {
            return getServers(service);
        }

        public List<ServiceInstance> getUpdatedListOfServers(Service service) {
            return getServers(service);
        }

        private List<ServiceInstance> getServers(Service service) {
            try {
                String keyString = String.format(
                        KEY_STRING_PREFIX_FORMAT,
                        service.getNamespace(),
                        service.getName()
                );
                CompletableFuture<GetResponse> getFuture = kvClient.get(
                        ByteSequence.from(keyString, StandardCharsets.UTF_8),
                        GetOption.newBuilder().isPrefix(true).build()
                );
                List<ServiceInstance> serviceInstances = new ArrayList<>();
                for (KeyValue kv : getFuture.get().getKvs()) {
                    serviceInstances.add(objectMapper.readValue(kv.getValue().toString(), ServiceInstance.class));
                }
                return serviceInstances;
            } catch (Exception e) {
                logger.error("Error with get instances:", e);
            }
            return new ArrayList<>();
        }
    }

    public ScheduledFuture<?> enableAndInitLearnNewServersFeature(Service service) {
        logger.info("Using serverListUpdater {}", this.serverListUpdater.getClass().getSimpleName());
        return this.serverListUpdater.start(new Action(service));
    }

    public void updateListOfServers(Service service) {
        if (this.etcdServerList != null) {
            updateAllServerList(service, this.etcdServerList.getUpdatedListOfServers(service));
        }
    }

    private void refreshServiceCache(Service service, List<ServiceInstance> instances) {
        this.instances.put(service, instances);
    }

}
