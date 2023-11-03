package com.tencent.tsf.femas.extensions.dubbo.registry;


import com.tencent.tsf.femas.api.ExtensionManager;
import com.tencent.tsf.femas.api.IExtensionLayer;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.util.AddressUtils;
import com.tencent.tsf.femas.extensions.dubbo.util.CommonUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.constants.RegistryConstants;
import org.apache.dubbo.common.utils.UrlUtils;
import org.apache.dubbo.registry.Constants;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.support.FailbackRegistry;
import org.apache.dubbo.registry.support.FemasServiceNotifyListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * apache dubbo registry center implementation for femas
 * @author rottenmu
 */

public class FemasApacheDubboRegistry extends FailbackRegistry {
    private static volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();
    private IExtensionLayer extensionLayer = ExtensionManager.getExtensionLayer();

    public FemasApacheDubboRegistry(URL url) {
        super(url);
        ServiceInstance instance = createServiceInstance(url);
        Service service = CommonUtils.buildService(url);
        extensionLayer.init(service, url.getPort());
        instance.setPort(url.getPort());
        instance.setService(service);
        extensionLayer.register(instance);
    }

    private ServiceInstance createServiceInstance(URL url) {
        ServiceInstance instance = new ServiceInstance(toServiceId(url),
                AddressUtils.getValidLocalHost(), url.getPort());
        String category = url.getParameter(RegistryConstants.CATEGORY_KEY, RegistryConstants.DEFAULT_CATEGORY);
        URL newURL = url.addParameter(RegistryConstants.CATEGORY_KEY, category);
        newURL = newURL.addParameter(CommonConstants.PROTOCOL_KEY, url.getProtocol());
        instance.setAllMetadata(new HashMap<>(newURL.getParameters()));
        return instance;
    }


    private String toServiceId(URL url) {
        return url.getProtocol() + "#" + url.getHost() + "#" + url.getPort() + "#" + url.getServiceInterface();
    }

    public List<URL> convert(List<ServiceInstance> services, URL consumerURL) {
        return services.stream()
                .map(serviceInstance -> new URL(serviceInstance.getAllMetadata().get(CommonConstants.PROTOCOL_KEY),
                            serviceInstance.getHost(),
                            serviceInstance.getPort(),
                            serviceInstance.getAllMetadata())
                ).filter(url -> UrlUtils.isMatch(consumerURL, url))
                .collect(Collectors.toList());
    }

    @Override
    public void unregister(URL url) {
        if (isConsumerSide(url)) {
            return;
        }
        super.unregister(url);
    }

    @Override
    public void doUnregister(URL url) {
        ServiceInstance instance = new ServiceInstance(toServiceId(url), url.getHost(), url.getPort());
        Service service = CommonUtils.buildService(url);
        instance.setService(service);
        extensionLayer.deregister(instance);
    }

    @Override
    public void subscribe(URL url, NotifyListener listener) {
        if (isProviderSide(url)) {
            return;
        }
        super.subscribe(url, listener);
    }

    @Override
    public void doSubscribe(URL url, NotifyListener listener) {
        final Service targetService = CommonUtils.buildService(url);
        List<ServiceInstance> instances = extensionLayer.subscribe(targetService, Arrays.asList(
                new FemasServiceNotifyListener(this, targetService, url, listener)
        ));

        List<URL> urls = convert(instances, url);
        listener.notify(urls);
    }

    @Override
    public void doRegister(URL url) {
        ServiceInstance instance = createServiceInstance(url);
        Service service = CommonUtils.buildService(url);
        instance.setService(service);
        extensionLayer.init(service, url.getPort());
        extensionLayer.register(instance);
    }

    @Override
    public void doUnsubscribe(URL url, NotifyListener listener) {
        if (isProviderSide(url)) {
            return;
        }
        super.unsubscribe(url, listener);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private boolean isConsumerSide(URL url) {
        return url.getProtocol().equals(Constants.CONSUMER_PROTOCOL);
    }

    private boolean isProviderSide(URL url) {
        return url.getProtocol().equals(Constants.PROVIDER_PROTOCOL);
    }

    @Override
    public void destroy() {
        super.destroy();
    }

}
