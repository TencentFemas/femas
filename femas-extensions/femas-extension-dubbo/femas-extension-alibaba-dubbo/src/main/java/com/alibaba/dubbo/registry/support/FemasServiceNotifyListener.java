package com.alibaba.dubbo.registry.support;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.NotifyListener;
import com.tencent.tsf.femas.common.discovery.ServiceNotifyListener;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.extensions.dubbo.registry.FemasDubboRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 由于 com.alibaba.dubbo.registry.support.FailbackRegistry#notify 是 protected，因此需要放在此 package
 */
public class FemasServiceNotifyListener implements ServiceNotifyListener {

    private static final Logger logger = LoggerFactory.getLogger(FemasServiceNotifyListener.class);
    private FemasDubboRegistry registry;
    private URL url;
    private NotifyListener listener;
    private Service targetService;

    public FemasServiceNotifyListener(FemasDubboRegistry registry,
                                      Service targetService, URL url, NotifyListener listener) {
        this.registry = registry;
        this.targetService = targetService;
        this.url = url;
        this.listener = listener;
    }

    @Override
    public void notify(Service service, List<ServiceInstance> instances) {
        if (service != targetService) {
            return;
        }
        List<URL> urls = registry.convert(instances, url);
        registry.notify(url, listener, urls);
    }

    @Override
    public void notifyOnRemoved(Service service, List<ServiceInstance> instances) {
    }

    @Override
    public void notifyOnAdded(Service service, List<ServiceInstance> instances) {
    }
}
