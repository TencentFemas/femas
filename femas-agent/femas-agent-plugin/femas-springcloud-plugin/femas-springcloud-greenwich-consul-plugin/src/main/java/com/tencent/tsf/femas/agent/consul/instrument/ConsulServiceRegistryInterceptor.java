package com.tencent.tsf.femas.agent.consul.instrument;

import com.tencent.tsf.femas.agent.interceptor.InstanceMethodsAroundInterceptor;
import com.tencent.tsf.femas.agent.interceptor.wrapper.InterceptResult;
import com.tencent.tsf.femas.agent.tools.ReflectionUtils;
import com.tencent.tsf.femas.api.ExtensionManager;
import com.tencent.tsf.femas.api.IExtensionLayer;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistryMetadata;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistryMetadataFactory;
import com.tencent.tsf.femas.common.util.StringUtils;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author huyuanxin
 */
public class ConsulServiceRegistryInterceptor implements InstanceMethodsAroundInterceptor<InterceptResult> {

    private final AbstractServiceRegistryMetadata serviceRegistryMetadata = AbstractServiceRegistryMetadataFactory.getServiceRegistryMetadata();

    private final IExtensionLayer extensionLayer = ExtensionManager.getExtensionLayer();

    private volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();

    @Override
    public InterceptResult beforeMethod(Method method, Object[] allArguments, Class<?>[] argumentsTypes) throws Throwable {
        ConsulRegistration consulRegistration = (ConsulRegistration) allArguments[0];
        ConsulDiscoveryProperties consulDiscoveryProperties = getConsulDiscoveryProperties(consulRegistration);
        String namespace = Context.getSystemTag(contextConstant.getNamespaceId());
        Service service = new Service(namespace, consulDiscoveryProperties.getServiceName());
        if (StringUtils.isNotEmpty(consulDiscoveryProperties.getIpAddress()) && consulDiscoveryProperties.getPort() != null) {
            extensionLayer.init(service, consulDiscoveryProperties.getPort(), consulDiscoveryProperties.getIpAddress());
        } else if (consulDiscoveryProperties.getPort() != null) {
            extensionLayer.init(service, consulDiscoveryProperties.getPort());
        }
        Map<String, String> registerMetadataMap = serviceRegistryMetadata.getRegisterMetadataMap();
        registerMetadataMap.put("protocol", "spring-cloud-consul-plugin");
        if (consulRegistration.getService().getMeta() == null) {
            consulRegistration.getService().setMeta(new LinkedHashMap<>());
        }
        consulRegistration.getService().getMeta().putAll(registerMetadataMap);
        return new InterceptResult();
    }

    @Override
    public Object afterMethod(Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        return ret;
    }

    @Override
    public void handleMethodException(Method method, Object[] allArguments, Class[] argumentsTypes, Throwable t) {
        // do nothing
    }


    private ConsulDiscoveryProperties getConsulDiscoveryProperties(ConsulRegistration consulRegistration) {
        Field properties = ReflectionUtils.findField(ConsulRegistration.class, "properties", ConsulDiscoveryProperties.class);
        if (properties != null) {
            ReflectionUtils.makeAccessible(properties);
            return (ConsulDiscoveryProperties) ReflectionUtils.getField(properties, consulRegistration);
        }
        throw new NullPointerException("ConsulRegistration don't have a properties");
    }

}
