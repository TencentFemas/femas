package com.tencent.tsf.femas.springcloud.discovery.starter.discovery.registry;

import com.tencent.tsf.femas.common.entity.EndpointStatus;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.springcloud.discovery.starter.discovery.FemasDiscoveryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * 描述：
 * 创建日期：2022年05月11 19:44:18
 *
 * @author gong zhao
 **/
public class FemasServiceRegistry implements ServiceRegistry<Registration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FemasServiceRegistry.class);

    private FemasDiscoveryProperties femasDiscoveryProperties;

    public FemasServiceRegistry(FemasDiscoveryProperties femasDiscoveryProperties) {
        this.femasDiscoveryProperties = femasDiscoveryProperties;
    }

    @Override
    public void register(Registration registration) {
        if (StringUtils.isEmpty(registration.getServiceId())) {
            LOGGER.warn("No service to register for femas client...");
        } else {
            com.tencent.tsf.femas.common.entity.ServiceInstance serviceInstance = getFemasInstanceFromRegistration(registration);
            try {
                serviceRegistry().register(serviceInstance);
                LOGGER.info("femas registry, {} {} {}:{}  register finished", femasDiscoveryProperties.getNamespace(), registration.getServiceId(), registration.getHost(), registration.getPort());
            } catch (Exception var5) {
                LOGGER.error("femas registry, {} register failed...{},", registration.getServiceId(), registration.toString());
                ReflectionUtils.rethrowRuntimeException(var5);
            }
        }
    }

    @Override
    public void deregister(Registration registration) {
        LOGGER.info("De-registering from Femas Server now...");

        if (StringUtils.isEmpty(registration.getServiceId())) {
            LOGGER.warn("No dom to de-register for femas client...");
            return;
        }
        com.tencent.tsf.femas.common.entity.ServiceInstance serviceInstance = getFemasInstanceFromRegistration(registration);
        try {
            serviceRegistry().deregister(serviceInstance);
        } catch (Exception e) {
            LOGGER.error("ERR_FEMAS_DEREGISTER, de-register failed...{},",
                    registration.toString(), e);
        }
        LOGGER.info("De-registration finished.");
    }

    @Override
    public void close() {
        try {
            serviceRegistry().close();
        } catch (Exception e) {
            LOGGER.error("Femas registry shutDown failed", e);
        }
    }

    @Override
    public void setStatus(Registration registration, String status) {
        EndpointStatus endpointStatus = EndpointStatus.getTypeByName(status.toUpperCase());
        if (endpointStatus == null) {
            LOGGER.warn("can't support status {}", status);
            return;
        }
        try {
            com.tencent.tsf.femas.common.entity.ServiceInstance serviceInstance = getFemasInstanceFromRegistration(registration);
            serviceRegistry().setStatus(serviceInstance, endpointStatus);
        } catch (Exception e) {
            throw new RuntimeException("update femas instance status fail", e);
        }
    }

    @Override
    public Object getStatus(Registration registration) {
        com.tencent.tsf.femas.common.entity.ServiceInstance serviceInstance = getFemasInstanceFromRegistration(registration);
        try {
            EndpointStatus endpointStatus = serviceRegistry().getStatus(serviceInstance);
            return endpointStatus.equals(EndpointStatus.UP) ? "UP" : "DOWN";
        } catch (Exception e) {
            LOGGER.error("get status of {} error,", registration.getServiceId(), e);
        }
        return null;
    }

    private com.tencent.tsf.femas.common.entity.ServiceInstance getFemasInstanceFromRegistration(Registration registration) {
        com.tencent.tsf.femas.common.entity.ServiceInstance instance = new ServiceInstance();
        instance.setService(new Service(registration.getServiceId(), femasDiscoveryProperties.getNamespace()));
        instance.setHost(registration.getHost());
        instance.setPort(registration.getPort());
        instance.setAllMetadata(registration.getMetadata());
        instance.setStatus(EndpointStatus.UP);
        return instance;
    }

    private com.tencent.tsf.femas.common.serviceregistry.ServiceRegistry serviceRegistry() {
        return femasDiscoveryProperties.serviceRegistry();
    }
}
