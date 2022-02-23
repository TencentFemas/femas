package com.tencent.tsf.femas.registry.impl.eureka.serviceregistry;


import com.netflix.appinfo.DataCenterInfo;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.LeaseInfo;
import com.netflix.appinfo.MyDataCenterInfo;
import com.tencent.tsf.femas.common.entity.EndpointStatus;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistry;
import com.tencent.tsf.femas.registry.impl.eureka.EurekaRegistryBuilder;
import com.tencent.tsf.femas.registry.impl.eureka.naming.EurekaNamingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.tencent.tsf.femas.common.RegistryConstants.*;
import static com.tencent.tsf.femas.common.RegistryConstants.REGISTRY_PORT;
import static com.tencent.tsf.femas.common.util.CommonUtils.checkNotNull;

/**
 * @author leo
 */
public class EurekaServiceRegistry extends AbstractServiceRegistry {
    private static final Logger logger = LoggerFactory.getLogger(EurekaServiceRegistry.class);

    private final EurekaNamingService eurekaNamingService;
    private final static String default_namespace = "application";

    private final EurekaRegistryBuilder builder;

    public EurekaServiceRegistry(Map<String, String> configMap) {
        String host = checkNotNull(REGISTRY_HOST, configMap.get(REGISTRY_HOST));
        String portString = checkNotNull(REGISTRY_PORT, configMap.get(REGISTRY_PORT));
        Integer port = Integer.parseInt(portString);
        this.builder = new EurekaRegistryBuilder();
        this.eurekaNamingService = builder.describeClient(() -> host.concat(":").concat(String.valueOf(port)), default_namespace, false, null);
    }

    @Override
    protected void doRegister(ServiceInstance serviceInstance) {
        logger.info("Registering service with consul: " + serviceInstance);
        InstanceInfo instance = buildService(serviceInstance);
        try {
            eurekaNamingService.registerInstance(instance);
        } catch (Exception e) {
            logger.error("Error registering service with eureka: " + serviceInstance, e);
        }
        logger.info("Service " + serviceInstance + " registered.");
    }

    @Override
    protected void doDeregister(ServiceInstance serviceInstance) {
        InstanceInfo instance = buildService(serviceInstance);
        try {
            eurekaNamingService.deregisterInstance(instance);
        } catch (Exception e) {
            logger.error("Error deregisterInstance service with nacos:{} ", serviceInstance.toString(), e);
        }
        logger.info("Deregister service with nacos: " + serviceInstance.toString() + " success.");
    }

    public InstanceInfo buildService(ServiceInstance instance) {
        return new InstanceInfo(
                instance.getHost() + ":" + Optional.ofNullable(instance).map(i -> i.getService()).map(s -> s.getName()).get() + ":" + instance.getPort(),
                Optional.ofNullable(instance).map(i -> i.getService()).map(s -> s.getName()).get(),
                null,
                instance.getHost(),
                null,
                new InstanceInfo.PortWrapper(true, instance.getPort()),
                null,
                "http://" + instance.getHost() + ":" + instance.getPort(),
                "http://" + instance.getHost() + ":" + instance.getPort() + "/info",
                "http://" + instance.getHost() + ":" + instance.getPort() + "/health",
                null,
                Optional.ofNullable(instance).map(i -> i.getService()).map(s -> s.getName()).get(),
                Optional.ofNullable(instance).map(i -> i.getService()).map(s -> s.getName()).get(),
                1,
                new MyDataCenterInfo(DataCenterInfo.Name.MyOwn),
                instance.getHost(),
                InstanceInfo.InstanceStatus.UP,
                InstanceInfo.InstanceStatus.UNKNOWN,
                null,
                new LeaseInfo(30, 90,
                        0L, 0L, 0L, 0L, 0L),
                false,
                (HashMap<String, String>) instance.getAllMetadata(),
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                null,
                null
        );
    }

    @Override
    public void setStatus(ServiceInstance serviceInstance, EndpointStatus status) {

    }

    @Override
    public EndpointStatus getStatus(ServiceInstance serviceInstance) {
        return null;
    }
}
