package com.tencent.tsf.femas.agent.consul.instrument;

import com.ecwid.consul.v1.agent.model.NewService;
import com.tencent.tsf.femas.agent.interceptor.InstanceMethodsAroundInterceptor;
import com.tencent.tsf.femas.agent.interceptor.wrapper.InterceptResult;
import com.tencent.tsf.femas.api.ExtensionManager;
import com.tencent.tsf.femas.api.IExtensionLayer;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistryMetadata;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistryMetadataFactory;
import com.tencent.tsf.femas.common.util.StringUtils;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistration;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author : MentosL
 * @date : 2022/5/2 23:48
 */
public class ConsulServiceRegistryInterceptor implements InstanceMethodsAroundInterceptor<InterceptResult> {

    private IExtensionLayer extensionLayer = ExtensionManager.getExtensionLayer();
    private volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();
    private volatile AbstractServiceRegistryMetadata serviceRegistryMetadata = AbstractServiceRegistryMetadataFactory.getServiceRegistryMetadata();


    @Override
    public InterceptResult beforeMethod(Method method, Object[] allArguments, Class[] argumentsTypes) throws Throwable {
        ConsulRegistration consulRegistration = (ConsulRegistration) allArguments[0];
        String namespace = Context.getSystemTag(contextConstant.getNamespaceId());
        NewService newService = consulRegistration.getService();
        Service service = new Service(namespace,newService.getName());
        String host = consulRegistration.getHost();
        if (!StringUtils.isEmpty(host)) {
            extensionLayer.init(service, consulRegistration.getPort(), host);
        } else {
            extensionLayer.init(service, consulRegistration.getPort());
        }
        Map<String, String> registerMetadataMap = serviceRegistryMetadata.getRegisterMetadataMap();
        registerMetadataMap.put("protocol", "spring-cloud-consul-plugin");
        consulRegistration.getMetadata().putAll(registerMetadataMap);
        return new InterceptResult();
    }

    @Override
    public Object afterMethod(Method method, Object[] allArguments, Class[] argumentsTypes, Object ret) throws Throwable {
        return ret;
    }

    @Override
    public void handleMethodException(Method method, Object[] allArguments, Class[] argumentsTypes, Throwable t) {

    }
}
