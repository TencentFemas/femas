package com.tencent.tsf.femas.registry.impl.consul.discovery;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.HealthServicesRequest;
import com.ecwid.consul.v1.health.model.HealthService;
import com.tencent.tsf.femas.common.discovery.AbstractServiceDiscoveryClient;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.common.util.NamedThreadFactory;
import com.tencent.tsf.femas.common.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static com.tencent.tsf.femas.common.RegistryConstants.REGISTRY_HOST;
import static com.tencent.tsf.femas.common.RegistryConstants.REGISTRY_PORT;
import static com.tencent.tsf.femas.common.util.CommonUtils.checkNotNull;
import static com.tencent.tsf.femas.registry.impl.consul.ConsulConstants.CONSUL_ACCESS_TOKEN;
import static java.util.concurrent.Executors.newCachedThreadPool;

/**
 * @author zhixinzxliu
 */
public class ConsulServiceDiscoveryClient extends AbstractServiceDiscoveryClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsulServiceDiscoveryClient.class);
    /**
     * default watch timeout in second
     */
    private static final int DEFAULT_WATCH_TIMEOUT = 55;
    private final ConsulClient client;
    private final String token;
    private ExecutorService notifierExecutor = newCachedThreadPool(
            new NamedThreadFactory("femas-service-com.tencent.tsf.femas.registry.impl.nacos.discovery-consul-notifier",
                    true));
    private volatile Map<Service, ConsulNotifier> notifiers = new ConcurrentHashMap<>();
    private volatile Map<Service, List<ServiceInstance>> instances = new ConcurrentHashMap<>();

    public ConsulServiceDiscoveryClient(Map<String, String> configMap) {

        String host = checkNotNull(REGISTRY_HOST, configMap.get(REGISTRY_HOST));
        String portString = checkNotNull(REGISTRY_PORT, configMap.get(REGISTRY_PORT));
        Integer port = Integer.parseInt(portString);

        this.token = configMap.get(CONSUL_ACCESS_TOKEN);
        this.client = new ConsulClient(host, port);
    }

    @Override
    protected void doSubscribe(Service service) {
        ConsulNotifier notifier = new ConsulNotifier(service, this);
        notifierExecutor.submit(notifier);
        notifiers.put(service, notifier);

        LOGGER.info("Successfully subscribe Service : " + service);
    }

    @Override
    protected void doUnSubscribe(Service service) {
        ConsulNotifier notifier = notifiers.remove(service);
        notifier.stop();

        LOGGER.info("Successfully unsubscribe Service : " + service);
    }

    @Override
    public List<ServiceInstance> getInstances(Service service) {
        List<ServiceInstance> instancesList = instances.get(service);
        if (instancesList != null) {
            return instancesList;
        }

        /**
         * 可能consul初始化较慢，这个时候可以避免每次都从consul那边读取数据
         */
        if (subscribed.contains(service) && initialized.contains(service)) {
            return Collections.emptyList();
        }

        // Fetch 传递的index为-1，不需要timeout，期待直接返回
        Response<List<HealthService>> response = getHealthServices(service.getName(), -1, -1);

        List<HealthService> healthServices = response.getValue();
        instancesList = convert(service, healthServices);

        refreshServiceCache(service, instancesList);
        return instancesList;
    }

    @Override
    public List<String> getAllServices() {
        Response<List<String>> catalogDatacenters = client.getCatalogDatacenters();
        return catalogDatacenters.getValue();
    }

    /**
     * TODO 考虑加上TAG
     *
     * @param service
     * @param index
     * @param watchTimeout
     * @return
     */
    private Response<List<HealthService>> getHealthServices(String service, long index, int watchTimeout) {
        HealthServicesRequest request = HealthServicesRequest.newBuilder()
//                .setTag(SERVICE_TAG)
                .setQueryParams(new QueryParams(watchTimeout, index))
                .setPassing(true)
                .setToken(token)
                .build();
        return client.getHealthServices(service, request);
    }

    private void refreshServiceCache(Service service, List<ServiceInstance> instances) {
        this.instances.put(service, instances);
    }

    /**
     * Tag这里暂时不反序列化，在 getServiceInstance 时用Tag进行过滤
     *
     * @param service
     * @param healthServices
     * @return
     */
    private List<ServiceInstance> convert(Service service, List<HealthService> healthServices) {
        List<ServiceInstance> serviceInstances = new ArrayList<>();

        for (HealthService healthService : healthServices) {
            HealthService.Service hs = healthService.getService();

            ServiceInstance instance = new ServiceInstance();
            instance.setService(service);
            instance.setId(hs.getId());
            instance.setHost(hs.getAddress());
            instance.setPort(hs.getPort());
            Map<String, String> metadata = hs.getMeta();
            instance.setAllMetadata(metadata);
            instance.setTags(parseTags(hs.getTags()));

            serviceInstances.add(instance);
        }

        return serviceInstances;
    }

    private Map<String, String> parseTags(List<String> tagList) {
        Map<String, String> tags = new HashMap<>();

        for (String tag : tagList) {
            String[] entry = tag.split("=");
            if (entry.length == 2) {
                tags.put(entry[0], entry[1]);
            }

        }

        return tags;
    }

    private class ConsulNotifier implements Runnable {

        private Service service;
        private long consulIndex = -1;
        private volatile boolean running;
        private ConsulServiceDiscoveryClient consulServiceDiscoveryClient;


        ConsulNotifier(Service service, ConsulServiceDiscoveryClient consulServiceDiscoveryClient) {
            this.service = service;
            this.running = true;
            this.consulServiceDiscoveryClient = consulServiceDiscoveryClient;
        }

        @Override
        public void run() {
            while (this.running) {
                try {
                    processService();
                    TimeUtil.silentlySleep(500);
                } catch (Exception e) {
                    LOGGER.error("Process Consul service com.tencent.tsf.femas.registry.impl.nacos.discovery failed.",
                            e);
                }
            }
        }

        private void processService() {
            Response<List<HealthService>> response = getHealthServices(service.getName(), consulIndex,
                    DEFAULT_WATCH_TIMEOUT);
            Long currentIndex = response.getConsulIndex();
            if (currentIndex != null && currentIndex != consulIndex) {
                LOGGER.info(
                        "Consul service com.tencent.tsf.femas.registry.impl.nacos.discovery client index changed. Current index = "
                                + currentIndex + ", last index = " + consulIndex);

                consulIndex = currentIndex;
                List<HealthService> healthServices = response.getValue();
                List<ServiceInstance> serviceInstances = Collections
                        .unmodifiableList(consulServiceDiscoveryClient.convert(service, healthServices));

                if (CollectionUtil.isEmpty(serviceInstances)) {
                    LOGGER.warn(
                            "Consul service com.tencent.tsf.femas.registry.impl.nacos.discovery client fetch emptyList. Use cached instances list.");
                    return;
                }

                List<ServiceInstance> oldServiceInstances = instances.get(service);
                consulServiceDiscoveryClient.refreshServiceCache(service, serviceInstances);
                consulServiceDiscoveryClient.notifyListeners(service, serviceInstances, oldServiceInstances);
                LOGGER.info(
                        "Consul service com.tencent.tsf.femas.registry.impl.nacos.discovery client instances changed. Current instances = "
                                + serviceInstances + ", old instances = " + oldServiceInstances);

                /**
                 * 第一次需要设置初始化成功标志位
                 */
                if (!initialized.contains(service)) {
                    initialized.add(service);
                }
            }
        }

        void stop() {
            this.running = false;
        }
    }

}