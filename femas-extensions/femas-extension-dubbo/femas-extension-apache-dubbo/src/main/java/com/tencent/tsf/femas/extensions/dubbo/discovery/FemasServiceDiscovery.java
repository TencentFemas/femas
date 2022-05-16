package com.tencent.tsf.femas.extensions.dubbo.discovery;

import com.tencent.tsf.femas.api.ExtensionManager;
import com.tencent.tsf.femas.api.IExtensionLayer;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.EndpointStatus;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.function.ThrowableFunction;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.registry.client.AbstractServiceDiscovery;
import org.apache.dubbo.registry.client.DefaultServiceInstance;
import org.apache.dubbo.registry.client.ServiceInstance;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.apache.dubbo.rpc.model.ScopeModelUtil;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.dubbo.common.function.ThrowableConsumer.execute;

public class FemasServiceDiscovery extends AbstractServiceDiscovery {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();
    private static String namespace = Context.getSystemTag(contextConstant.getNamespaceId());
    private IExtensionLayer extensionLayer = ExtensionManager.getExtensionLayer();



    public FemasServiceDiscovery(ApplicationModel applicationModel, URL registryURL) {
        super(applicationModel, registryURL);
    }

    @Override
    protected void doRegister(ServiceInstance serviceInstance) throws RuntimeException {
        execute(extensionLayer, service -> {
            extensionLayer.init(new Service(namespace, serviceInstance.getServiceName()), serviceInstance.getPort());
            com.tencent.tsf.femas.common.entity.ServiceInstance instance = toFemasInstance(serviceInstance);
            service.register(instance);
        });
    }

    @Override
    protected void doUnregister(ServiceInstance serviceInstance) {
        execute(extensionLayer, service -> {
            service.deregister(toFemasInstance(serviceInstance));
        });
    }

    @Override
    protected void doDestroy() throws Exception {
        // noting to do
    }

    @Override
    public Set<String> getServices() {
        return ThrowableFunction.execute(extensionLayer, service ->
                service.getAllServices()
                        .stream()
                        .collect(Collectors.toSet())
        );
    }
    @Override
    public List<ServiceInstance> getInstances(String serviceName) throws NullPointerException {
        return ThrowableFunction.execute(extensionLayer, service -> {
            List<com.tencent.tsf.femas.common.entity.ServiceInstance> serviceInstances = service.getInstance(serviceName, namespace);
            if (CollectionUtil.isNotEmpty(serviceInstances)) {
                return serviceInstances
                        .stream()
                        .map(instance -> toServiceInstance(this.registryURL, instance))
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        });
    }

    private com.tencent.tsf.femas.common.entity.ServiceInstance toFemasInstance(ServiceInstance serviceInstance) {
        com.tencent.tsf.femas.common.entity.ServiceInstance instance =
                new com.tencent.tsf.femas.common.entity.ServiceInstance();

        instance.setAllMetadata(serviceInstance.getMetadata());
        instance.setPort(serviceInstance.getPort());
        instance.setHost(serviceInstance.getHost());
        instance.setStatus(EndpointStatus.UP);
        if (serviceInstance.getMetadata() != null) {
            Map<String, String> metadata = serviceInstance.getMetadata();
            instance.setId(metadata.get(contextConstant.getMetaInstanceIdKey()));
            instance.setService(new Service(metadata.get(contextConstant.getMetaNamespaceIdKey()), serviceInstance.getServiceName()));
            instance.setServiceVersion(metadata.get(contextConstant.getApplicationVersion()));
        }

        return instance;
    }

    private ServiceInstance toServiceInstance(URL registryUrl,com.tencent.tsf.femas.common.entity.ServiceInstance serviceInstance) {
        DefaultServiceInstance instance = new DefaultServiceInstance(serviceInstance.getService().getName(),serviceInstance.getHost(), serviceInstance.getPort() , ScopeModelUtil.getApplicationModel(registryUrl.getScopeModel()));
        instance.setPort(serviceInstance.getPort());
        instance.setMetadata(serviceInstance.getAllMetadata());
        instance.setHost(serviceInstance.getHost());
        instance.setHealthy(Objects.equals(EndpointStatus.UP, serviceInstance.getStatus()));
        instance.setServiceName(serviceInstance.getService().getName());
        instance.setRegistryCluster("DEFAULT_CLUSTER");
        instance.setEnabled(instance.isEnabled());
        return instance;
    }
}
