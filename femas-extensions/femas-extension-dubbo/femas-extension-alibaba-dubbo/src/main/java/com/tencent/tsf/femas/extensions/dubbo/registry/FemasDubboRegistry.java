package com.tencent.tsf.femas.extensions.dubbo.registry;


import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.UrlUtils;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.support.FailbackRegistry;
import com.alibaba.dubbo.registry.support.FemasServiceNotifyListener;
import com.tencent.tsf.femas.api.ExtensionManager;
import com.tencent.tsf.femas.api.IExtensionLayer;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.util.AddressUtils;
import com.tencent.tsf.femas.extensions.dubbo.util.CommonUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * registry center implementation for femas
 */
public class FemasDubboRegistry extends FailbackRegistry {

    private static final Logger logger = LoggerFactory.getLogger(FemasDubboRegistry.class);
    private static volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();
    private static String namespace = Context.getSystemTag(contextConstant.getNamespaceId());
    private IExtensionLayer extensionLayer = ExtensionManager.getExtensionLayer();


    public FemasDubboRegistry(URL url) {
        super(url);
        // 类初始化的时候，以 dubbo application name 注册服务，以便获取主调方的治理规则
        Service service = new Service(namespace, url.getParameter("application"));
        extensionLayer.init(service, -1); // mock服务，端口号为 -1
        ServiceInstance instance = createServiceInstance(url);
        instance.setPort(-1);
        instance.setService(service);
        extensionLayer.register(instance);
    }

    @Override
    public void register(URL url) {
        if (isConsumerSide(url)) {
            return;
        }
        super.register(url);
    }

    @Override
    public synchronized void doRegister(URL url) {
        ServiceInstance instance = createServiceInstance(url);
        Service service = CommonUtils.buildService(url);
        instance.setService(service);
        extensionLayer.init(service, url.getPort());
        extensionLayer.register(instance);
    }

    private ServiceInstance createServiceInstance(URL url) {
        ServiceInstance instance = new ServiceInstance(toServiceId(url),
                AddressUtils.getValidLocalHost(), url.getPort());
        String category = url.getParameter(Constants.CATEGORY_KEY, Constants.DEFAULT_CATEGORY);
        URL newURL = url.addParameter(Constants.CATEGORY_KEY, category);
        newURL = newURL.addParameter(Constants.PROTOCOL_KEY, url.getProtocol());
        instance.setAllMetadata(new HashMap<>(newURL.getParameters()));
        return instance;
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

    private String toServiceId(URL url) {
        return url.getProtocol() + "#" + url.getHost() + "#" + url.getPort() + "#" + url.getServiceInterface();
    }


    public List<URL> convert(List<ServiceInstance> services, URL consumerURL) {
        return services.stream()
                .map(serviceInstance -> {
                    return new URL(serviceInstance.getAllMetadata().get(Constants.PROTOCOL_KEY),
                            serviceInstance.getHost(),
                            serviceInstance.getPort(),
                            serviceInstance.getAllMetadata());
                }).filter(url -> UrlUtils.isMatch(consumerURL, url))
                .collect(Collectors.toList());
    }

    @Override
    public void unsubscribe(URL url, NotifyListener listener) {
        if (isProviderSide(url)) {
            return;
        }
        super.unsubscribe(url, listener);
    }

    @Override
    public void doUnsubscribe(URL url, NotifyListener listener) {
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void destroy() {
        super.destroy();
    }


    private boolean isConsumerSide(URL url) {
        return url.getProtocol().equals(Constants.CONSUMER_PROTOCOL);
    }

    private boolean isProviderSide(URL url) {
        return url.getProtocol().equals(Constants.PROVIDER_PROTOCOL);
    }
}