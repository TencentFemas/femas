package com.tencent.tsf.femas.registry.impl.zookeeper.discovery;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tsf.femas.common.RegistryConstants;
import com.tencent.tsf.femas.common.discovery.AbstractServiceDiscoveryClient;
import com.tencent.tsf.femas.common.discovery.SchedulePollingServerListUpdater;
import com.tencent.tsf.femas.common.discovery.ServerUpdater;
import com.tencent.tsf.femas.common.entity.EndpointStatus;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author huyuanxin
 */
public class ZookeeperServiceDiscoveryClient extends AbstractServiceDiscoveryClient {


    private static final Logger logger = LoggerFactory.getLogger(ZookeeperServiceDiscoveryClient.class);
    protected volatile ServerUpdater serverListUpdater;
    private final PolarisServerList serverListImpl;
    private final Map<Service, List<ServiceInstance>> instances = new ConcurrentHashMap<>();
    private final Map<Service, Notifier> notifiers = new ConcurrentHashMap<>();
    protected AtomicBoolean serverListUpdateInProgress;
    private final ZooKeeper zooKeeper;

    private final ObjectMapper objectMapper;


    public ZookeeperServiceDiscoveryClient(Map<String, String> configMap) {
        ZooKeeper zooKeeperTemp = null;
        try {
            String connectString = configMap.get(RegistryConstants.REGISTRY_HOST) + ":" + configMap.get(RegistryConstants.REGISTRY_PORT);
            zooKeeperTemp = new ZooKeeper(connectString, 3000, watchedEvent -> {
                if (Watcher.Event.KeeperState.SyncConnected == watchedEvent.getState() && Watcher.Event.EventType.None == watchedEvent.getType()) {
                    logger.info("zookeeper server connect success!");
                }
            });
        } catch (IOException e) {
            logger.error("Error create zookeeper registry with:{0}", e);
        }
        this.serverListUpdateInProgress = new AtomicBoolean(false);
        this.serverListUpdater = new SchedulePollingServerListUpdater();
        this.serverListImpl = new PolarisServerList();
        zooKeeper = zooKeeperTemp;
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * @see com.tencent.tsf.femas.common.discovery.ServiceDiscoveryClient#getInstances(com.tencent.tsf.femas.common.entity.Service)
     */
    @Override
    public List<ServiceInstance> getInstances(Service service) {
        List<ServiceInstance> instancesList = instances.get(service);
        if (instancesList != null) {
            return instancesList;
        }
        List<ServiceInstance> instanceList = serverListImpl.getInitialListOfServers(service.getNamespace(), service.getName());
        instancesList = convert(service, instanceList);
        refreshServiceCache(service, instancesList);
        return instancesList;
    }

    @Override
    public List<String> getAllServices() {
        try {
            List<String> list = zooKeeper.getChildren("/femas", false);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            logger.error("Error with get all services", e);
            return new ArrayList<>();
        }
    }

    /**
     * @see com.tencent.tsf.femas.common.discovery.AbstractServiceDiscoveryClient#doSubscribe(com.tencent.tsf.femas.common.entity.Service)
     */
    @Override
    protected void doSubscribe(Service service) {
        Notifier notifier = new Notifier(service);
        notifier.run();
        notifiers.putIfAbsent(service, notifier);
    }

    /**
     * @see com.tencent.tsf.femas.common.discovery.AbstractServiceDiscoveryClient#doUnSubscribe(com.tencent.tsf.femas.common.entity.Service)
     */
    @Override
    protected void doUnSubscribe(Service service) {
        serverListUpdater.stop(notifiers.get(service).scheduledFuture);
        notifiers.remove(service);
    }

    public ScheduledFuture<?> enableAndInitLearnNewServersFeature(Service service) {
        logger.info("Using serverListUpdater {}", this.serverListUpdater.getClass().getSimpleName());
        return this.serverListUpdater.start(new Action(service));
    }

    public void updateListOfServers(Service service) {
        List<ServiceInstance> instanceList = new ArrayList<>();
        if (this.serverListImpl != null) {
            instanceList = this.serverListImpl.getUpdatedListOfServers(service.getNamespace(), service.getName());
        }

        this.updateAllServerList(service, instanceList);
    }

    protected void updateAllServerList(Service service, List<ServiceInstance> ls) {
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

    /**
     * server级别监听
     */
    class PolarisServerList {

        public List<ServiceInstance> getInitialListOfServers(String namespace, String serviceName) {
            return getServers(namespace, serviceName);
        }

        public List<ServiceInstance> getUpdatedListOfServers(String namespace, String serviceName) {
            return getServers(namespace, serviceName);
        }

        private List<ServiceInstance> getServers(String namespace, String serviceName) {
            try {
                //拉取所有服务实例 GetAllInstancesRequest
                String prefix = "/femas/" + namespace + "/" + serviceName;
                List<String> list = zooKeeper.getChildren(prefix, false);
                List<ServiceInstance> serviceInstanceList = new ArrayList<>();
                for (String s : list) {
                    byte[] data = zooKeeper.getData(prefix + "/" + s, false, null);
                    serviceInstanceList.add(objectMapper.readValue(new String(data), ServiceInstance.class));
                }
                return serviceInstanceList;
            } catch (Exception e) {
                throw new IllegalStateException("Can not get service instances from zookeeper, namespace=" + namespace + ",serviceName=" + serviceName, e);
            }
        }
    }

    class Action implements ServerUpdater.UpdateAction {

        private final Service service;

        Action(Service service) {
            this.service = service;
        }

        @Override
        public void doUpdate() {
            ZookeeperServiceDiscoveryClient.this.updateListOfServers(service);
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

    List<ServiceInstance> convert(Service service, List<ServiceInstance> ls) {
        List<ServiceInstance> serviceInstanceList = new ArrayList<>();
        ls.forEach(i -> {
            ServiceInstance instance = new ServiceInstance();
            instance.setAllMetadata(i.getAllMetadata());
            instance.setHost(i.getHost());
            instance.setPort(i.getPort());
            instance.setService(service);
            instance.setStatus(EndpointStatus.UP);
            serviceInstanceList.add(instance);
        });
        return serviceInstanceList;
    }

}
