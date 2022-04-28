package com.tencent.tsf.femas.agent.eureka.instrument;


import com.netflix.discovery.EurekaClientConfig;
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
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaRegistration;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @Author mentosL
 * @Date: 2022/4/27 10:29
 */
public class EurekaServiceRegistryInterceptor implements InstanceMethodsAroundInterceptor<InterceptResult> {

    private static final String EUREKA_SUFFIX = "/eureka";
    private static final String EUREKA_PREFIX = "http://";

    private IExtensionLayer extensionLayer = ExtensionManager.getExtensionLayer();
    private volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();
    private volatile AbstractServiceRegistryMetadata serviceRegistryMetadata = AbstractServiceRegistryMetadataFactory.getServiceRegistryMetadata();


    @Override
    public InterceptResult beforeMethod(Method method, Object[] allArguments, Class[] argumentsTypes) throws Throwable {
        EurekaRegistration eurekaRegistration = (EurekaRegistration) allArguments[0];
        String namespace = Context.getSystemTag(contextConstant.getNamespaceId());
        EurekaClientConfig eurekaClientConfig = eurekaRegistration.getEurekaClient().getEurekaClientConfig();
        String region = eurekaClientConfig.getRegion();
        List<String> eurekaServerServiceUrls = eurekaClientConfig.getEurekaServerServiceUrls(region);
        Map<Integer, String> portAndServerAddress = getPortAndServerAddress(eurekaServerServiceUrls);
        Service service = new Service(namespace, eurekaRegistration.getInstanceConfig().getAppname().toUpperCase(Locale.ROOT));
        if (!CollectionUtils.isEmpty(portAndServerAddress)) {
            for (Map.Entry<Integer, String> entry : portAndServerAddress.entrySet()) {
                if (StringUtils.isNotBlank(entry.getValue())) {
                    extensionLayer.init(service, entry.getKey(), entry.getValue());
                } else {
                    extensionLayer.init(service, entry.getKey());
                }
            }
        }
        Map<String, String> registerMetadataMap = serviceRegistryMetadata.getRegisterMetadataMap();
        registerMetadataMap.put("protocol", "spring-cloud-eureka-plugin");
        eurekaRegistration.getMetadata().putAll(registerMetadataMap);
        return new InterceptResult();
    }

    @Override
    public Object afterMethod(Method method, Object[] allArguments, Class[] argumentsTypes, Object ret) throws Throwable {
        return ret;
    }

    @Override
    public void handleMethodException(Method method, Object[] allArguments, Class[] argumentsTypes, Throwable t) {

    }

    /**
     * split port and address
     *
     * @param eurekaServerServiceUrls
     * @return
     */
    private Map<Integer/** port **/, String/** registryUrl **/> getPortAndServerAddress(List<String> eurekaServerServiceUrls) {
        HashMap<Integer, String> result = new HashMap<>();
        if (!CollectionUtils.isEmpty(eurekaServerServiceUrls)) {
            for (String serviceUrl : eurekaServerServiceUrls) {
                int begin = EUREKA_PREFIX.length();
                int end = serviceUrl.indexOf(EUREKA_SUFFIX);
                serviceUrl = serviceUrl.substring(begin, end);
                result.put(NumberUtils.toInt(serviceUrl.split(":")[1]), serviceUrl);
            }
        }
        return result;
    }
}