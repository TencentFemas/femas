package com.tencent.tsf.femas.registry.impl.nacos.serviceregistry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.EndpointStatus;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistry;

import com.tencent.tsf.femas.registry.impl.nacos.NacosRegistryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.tencent.tsf.femas.common.RegistryConstants.*;
import static com.tencent.tsf.femas.common.util.CommonUtils.checkNotNull;

/**
 * @author leo
 */
public class NacosServiceRegistry extends AbstractServiceRegistry {
    private static final Logger logger = LoggerFactory.getLogger(NacosServiceRegistry.class);
    private static volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();
    private final NamingService nacosNamingService;
    private final static String default_namespace = "public";
    private final NacosRegistryBuilder builder;

    public NacosServiceRegistry(Map<String, String> configMap) {
        String host = checkNotNull(REGISTRY_HOST, configMap.get(REGISTRY_HOST));
        String portString = checkNotNull(REGISTRY_PORT, configMap.get(REGISTRY_PORT));
        Integer port = Integer.parseInt(portString);
        String namespace = Context.getSystemTag(contextConstant.getNamespaceId());
        this.builder = new NacosRegistryBuilder();
        this.nacosNamingService = builder.describeClient(() -> host.concat(":").concat(String.valueOf(port)), StringUtils.isEmpty(namespace) ? default_namespace : namespace, false, null);
    }

    @Override
    protected void doRegister(ServiceInstance serviceInstance) {
        logger.info("Registering service with nacos: " + serviceInstance);
        Instance instance = buildService(serviceInstance);
        try {
            nacosNamingService.registerInstance(serviceInstance.getService().getName(), instance);
        } catch (NacosException e) {
            logger.error("Error registering service with nacos: " + serviceInstance, e);
        }
        logger.info("Service " + serviceInstance + " registered.");
    }

    @Override
    protected void doDeregister(ServiceInstance serviceInstance) {
        Instance instance = buildService(serviceInstance);
        try {
            nacosNamingService.deregisterInstance(serviceInstance.getService().getName(), instance);
        } catch (NacosException e) {
            logger.error("Error deregisterInstance service with nacos:{} ", serviceInstance.toString(), e);
        }
        logger.info("Deregister service with nacos: " + serviceInstance.toString() + " success.");
    }

    public Instance buildService(ServiceInstance serviceInstance) {
        Instance instance = new Instance();
        instance.setIp(serviceInstance.getHost());
        instance.setPort(serviceInstance.getPort());
        instance.setServiceName(serviceInstance.getService().getName());
        instance.setHealthy(true);
        instance.setEnabled(true);
        instance.setEphemeral(true);
        instance.setMetadata(serviceInstance.getAllMetadata());
        return instance;
    }

    @Override
    public void setStatus(ServiceInstance serviceInstance, EndpointStatus status) {

    }

    @Override
    public EndpointStatus getStatus(ServiceInstance serviceInstance) {
        return null;
    }
}
