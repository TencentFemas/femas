package com.tencent.tsf.femas.registry.impl.nacos.discovery;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.discovery.AbstractServiceDiscoveryClient;
import com.tencent.tsf.femas.common.discovery.SchedulePollingServerListUpdater;
import com.tencent.tsf.femas.common.discovery.ServerUpdater;
import com.tencent.tsf.femas.common.entity.EndpointStatus;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.registry.impl.nacos.NacosRegistryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.tencent.tsf.femas.common.RegistryConstants.REGISTRY_HOST;
import static com.tencent.tsf.femas.common.RegistryConstants.REGISTRY_PORT;
import static com.tencent.tsf.femas.common.util.CommonUtils.checkNotNull;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/24 15:24
 * @Version 1.0
 */
public class NacosServiceDiscoveryClient extends AbstractServiceDiscoveryClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(NacosServiceDiscoveryClient.class);

    private final NamingService nacosNamingService;

    private static volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();
    private static final String DEFAULT_NAMESPACE = "public";
    protected volatile ServerUpdater serverListUpdater;

    private final Map<Service, List<ServiceInstance>> instances = new ConcurrentHashMap<>();

    protected AtomicBoolean serverListUpdateInProgress;

    private final NacosServerList serverListImpl;

    private final Map<Service, Notifier> notifiers = new ConcurrentHashMap<>();

    public NacosServiceDiscoveryClient(Map<String, String> configMap) {
        String host = checkNotNull(REGISTRY_HOST, configMap.get(REGISTRY_HOST));
        String portString = checkNotNull(REGISTRY_PORT, configMap.get(REGISTRY_PORT));
        Integer port = Integer.parseInt(portString);
        this.serverListUpdateInProgress = new AtomicBoolean(false);
        NacosRegistryBuilder builder = new NacosRegistryBuilder();
        String namespace = Context.getSystemTag(contextConstant.getNamespaceId());
        this.nacosNamingService = builder.describeClient(() -> host.concat(":").concat(String.valueOf(port)), StringUtils.isEmpty(namespace) ? DEFAULT_NAMESPACE : namespace, false, null);
        this.serverListUpdater = new SchedulePollingServerListUpdater();
        this.serverListImpl = new NacosServerList();
    }

    public void updateListOfServers(Service service) {
        AtomicReference<List<Instance>> instanceList = new AtomicReference<>(new ArrayList<>());
        if (this.serverListImpl != null) {
            Optional.ofNullable(service)
                    .map(Service::getName)
                    .ifPresent(it -> instanceList.set(this.serverListImpl.getUpdatedListOfServers(it)));
        }
        this.updateAllServerList(service, instanceList.get());
    }

    protected void updateAllServerList(Service service, List<Instance> ls) {
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

    List<ServiceInstance> convert(Service service, List<Instance> ls) {
        List<ServiceInstance> serviceInstanceList = new ArrayList<>();
        ls.forEach(i -> {
            ServiceInstance instance = new ServiceInstance();
            instance.setAllMetadata(i.getMetadata());
            instance.setHost(i.getIp());
            instance.setPort(i.getPort());
            instance.setService(service);
            instance.setStatus(i.isEnabled() && i.isHealthy() ? EndpointStatus.UP : EndpointStatus.INITIALIZING);
            serviceInstanceList.add(instance);
        });
        return serviceInstanceList;
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
        List<Instance> instanceList = serverListImpl.getInitialListOfServers(service.getName());
        instancesList = convert(service, instanceList);
        refreshServiceCache(service, instancesList);
        return instancesList;
    }

    @Override
    public List<String> getAllServices() {
        ListView<String> view = new ListView<>();
        try {
            view = nacosNamingService.getServicesOfServer(0, Integer.MAX_VALUE, Constants.DEFAULT_GROUP);
        } catch (NacosException e) {
            e.printStackTrace();
        }

        if (view.getData().isEmpty()){
            return Collections.emptyList();
        }

        return view.getData();
    }

    class Action implements ServerUpdater.UpdateAction {

        private final Service service;

        Action(Service service) {
            this.service = service;
        }

        @Override
        public void doUpdate() {
            NacosServiceDiscoveryClient.this.updateListOfServers(service);
        }
    }

    /**
     * server级别监听
     */
    class NacosServerList {
        public List<Instance> getInitialListOfServers(String serviceId) {
            return getServers(serviceId);
        }

        public List<Instance> getUpdatedListOfServers(String serviceId) {
            return getServers(serviceId);
        }

        private List<Instance> getServers(String serviceId) {
            try {
                //默认group
//                String group = discoveryProperties.getGroup();
                String group = "DEFAULT_GROUP";
                return nacosNamingService.selectInstances(serviceId, group, true);
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Can not get service instances from nacos, serviceId=" + serviceId,
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