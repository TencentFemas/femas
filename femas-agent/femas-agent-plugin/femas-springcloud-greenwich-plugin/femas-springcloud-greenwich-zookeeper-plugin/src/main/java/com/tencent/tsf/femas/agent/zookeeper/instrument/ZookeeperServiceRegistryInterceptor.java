package com.tencent.tsf.femas.agent.zookeeper.instrument;

import com.tencent.tsf.femas.agent.interceptor.InstanceMethodsAroundInterceptor;
import com.tencent.tsf.femas.agent.interceptor.wrapper.InterceptResult;
import com.tencent.tsf.femas.api.ExtensionManager;
import com.tencent.tsf.femas.api.IExtensionLayer;
import com.tencent.tsf.femas.common.RegistryEnum;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistryMetadata;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistryMetadataFactory;
import com.tencent.tsf.femas.common.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Map;

import static com.tencent.tsf.femas.common.context.ContextConstant.AGENT_REGISTER_TYPE_KEY;

/**
 * @author huyuanxin
 */
public class ZookeeperServiceRegistryInterceptor implements InstanceMethodsAroundInterceptor<InterceptResult> {


    private final AbstractServiceRegistryMetadata serviceRegistryMetadata = AbstractServiceRegistryMetadataFactory.getServiceRegistryMetadata();

    private final IExtensionLayer extensionLayer = ExtensionManager.getExtensionLayer();

    private volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();

    @Override
    public InterceptResult beforeMethod(Method method, Object[] allArguments, Class<?>[] argumentsTypes) throws Throwable {
        org.springframework.cloud.zookeeper.serviceregistry.ZookeeperRegistration registration = (org.springframework.cloud.zookeeper.serviceregistry.ZookeeperRegistration) allArguments[0];
        Context.putSystemTag(AGENT_REGISTER_TYPE_KEY, RegistryEnum.CONSUL.getAlias());
        String namespace = Context.getSystemTag(contextConstant.getNamespaceId());
        String name = registration.getServiceInstance().getName();
        Service service = new Service(namespace, name);
        String address = registration.getServiceInstance().getAddress();
        Integer port = registration.getServiceInstance().getPort();
        if (StringUtils.isNotEmpty(address) && port != null) {
            extensionLayer.init(service, port, address);
        } else if (port != null) {
            extensionLayer.init(service, port);
        }
        Map<String, String> registerMetadataMap = serviceRegistryMetadata.getRegisterMetadataMap();
        registerMetadataMap.put("protocol", "spring-cloud-zookeeper-plugin");
        registration.getMetadata().putAll(registerMetadataMap);
        return new InterceptResult();
    }


    @Override
    public Object afterMethod(Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        return ret;
    }

}