package com.tencent.tsf.femas.registry.impl.eureka.discovery;


import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.tencent.tsf.femas.common.discovery.AbstractServiceDiscoveryClient;
import com.tencent.tsf.femas.common.discovery.SchedulePollingServerListUpdater;
import com.tencent.tsf.femas.common.discovery.ServerUpdater;
import com.tencent.tsf.femas.common.entity.EndpointStatus;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.registry.impl.eureka.EurekaRegistryBuilder;
import com.tencent.tsf.femas.registry.impl.eureka.naming.EurekaNamingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.tencent.tsf.femas.common.RegistryConstants.REGISTRY_HOST;
import static com.tencent.tsf.femas.common.RegistryConstants.REGISTRY_PORT;
import static com.tencent.tsf.femas.common.util.CommonUtils.checkNotNull;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/24 15:24
 * @Version 1.0
 */
public class EurekaServiceDiscoveryClient extends AbstractServiceDiscoveryClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(EurekaServiceDiscoveryClient.class);

    private final EurekaNamingService eurekaNamingService;

    protected volatile ServerUpdater serverListUpdater;

    private final Map<Service, List<ServiceInstance>> instances = new ConcurrentHashMap<>();

    protected AtomicBoolean serverListUpdateInProgress;

    private final EurekaServerList serverListImpl;

    private final Map<Service, Notifier> notifiers = new ConcurrentHashMap<>();

    public EurekaServiceDiscoveryClient(Map<String, String> configMap) {
        String host = checkNotNull(REGISTRY_HOST, configMap.get(REGISTRY_HOST));
        String portString = checkNotNull(REGISTRY_PORT, configMap.get(REGISTRY_PORT));
        Integer port = Integer.parseInt(portString);
        this.serverListUpdateInProgress = new AtomicBoolean(false);
        EurekaRegistryBuilder builder = new EurekaRegistryBuilder();
        this.eurekaNamingService = builder.describeClient(() -> host.concat(":").concat(String.valueOf(port)), "application", false, null);
        this.serverListUpdater = new SchedulePollingServerListUpdater();
        this.serverListImpl = new EurekaServerList();
    }

    public void updateListOfServers(Service service) {
        AtomicReference<List<InstanceInfo>> instancesReference = new AtomicReference<>();
        if (this.serverListImpl != null) {
            Optional.ofNullable(service)
                    .map(Service::getName)
                    .ifPresent(it -> instancesReference.set(this.serverListImpl.getUpdatedListOfServers(it)));
        }
        instancesReference.compareAndSet(null, new ArrayList<>());
        this.updateAllServerList(service, instancesReference.get());
    }

    protected void updateAllServerList(Service service, List<InstanceInfo> ls) {
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

    List<ServiceInstance> convert(Service service, List<InstanceInfo> ls) {
        List<ServiceInstance> serviceInstances = new ArrayList<>();
        ls.forEach(i -> {
            ServiceInstance instance = new ServiceInstance();
            instance.setAllMetadata(i.getMetadata());
            instance.setHost(i.getIPAddr());
            instance.setPort(i.getPort());
            instance.setService(service);
            instance.setStatus(EndpointStatus.getTypeByName(i.getStatus().name()));
            serviceInstances.add(instance);
        });
        return serviceInstances;
    }

    public ScheduledFuture<?> enableAndInitLearnNewServersFeature(Service service) {
        LOGGER.info("Using serverListUpdater {}", this.serverListUpdater.getClass().getSimpleName());
        return this.serverListUpdater.start(new Action(service));
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
        List<InstanceInfo> instanceInfoList = serverListImpl.getInitialListOfServers(service.getName());
        if (instanceInfoList == null) {
            instanceInfoList = new ArrayList<>();
        }
        instancesList = convert(service, instanceInfoList);
        refreshServiceCache(service, instancesList);
        return instancesList;
    }

    @Override
    public List<String> getAllServices() {
        List<Application> applications = eurekaNamingService.getAllApplications();
        if (CollectionUtil.isNotEmpty(applications)) {
            List<InstanceInfo> instanceInfos = new ArrayList<>();
            applications.forEach(application -> instanceInfos.addAll(application.getInstances()));
            return instanceInfos
                    .stream()
                    .map(InstanceInfo::getAppName)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    class Action implements ServerUpdater.UpdateAction {

        private final Service service;

        Action(Service service) {
            this.service = service;
        }

        @Override
        public void doUpdate() {
            EurekaServiceDiscoveryClient.this.updateListOfServers(service);
        }
    }

    /**
     * server级别监听
     */
    class EurekaServerList {
        public List<InstanceInfo> getInitialListOfServers(String serviceId) {
            return getServers(serviceId);
        }

        public List<InstanceInfo> getUpdatedListOfServers(String serviceId) {
            return getServers(serviceId);
        }

        private List<InstanceInfo> getServers(String serviceId) {
            try {
                return eurekaNamingService.getApplications(serviceId);
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Can not get service instances from eureka, serviceId=" + serviceId,
                        e);
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