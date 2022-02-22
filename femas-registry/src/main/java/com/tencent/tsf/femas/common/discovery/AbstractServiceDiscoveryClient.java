package com.tencent.tsf.femas.common.discovery;

import com.google.common.collect.Sets;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.common.util.NamedThreadFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhixinzxliu
 */
public abstract class AbstractServiceDiscoveryClient implements ServiceDiscoveryClient {

    private static final Logger logger = LoggerFactory.getLogger(AbstractServiceDiscoveryClient.class);

    protected volatile Set<Service> subscribed = Sets.newConcurrentHashSet();
    protected volatile Set<Service> initialized = Sets.newConcurrentHashSet();

    private volatile Set<ServiceNotifyListener> listeners = Sets.newConcurrentHashSet();
    private volatile ExecutorService notifyExecutor;

    public AbstractServiceDiscoveryClient() {
        this.notifyExecutor = Executors.newCachedThreadPool(new NamedThreadFactory("ServiceNotifyListener", true));
    }

    @Override
    public synchronized void subscribe(Service service) {
        if (service == null) {
            throw new IllegalArgumentException("service can not be null");
        }

        if (!subscribed.contains(service)) {
            doSubscribe(service);
            subscribed.add(service);
            logger.info("Subscribe service:" + service);
        }
    }

    @Override
    public void addNotifyListener(ServiceNotifyListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("subscribe listener can not be null");
        }

        if (!listeners.contains(listener)) {
            listeners.add(listener);
            logger.info("Add ServiceNotifyListener : " + listener);
        }
    }

    protected abstract void doSubscribe(Service service);

    /**
     * 异步通知listeners
     *
     * @param service
     * @param instances
     */
    protected void notifyListeners(Service service, List<ServiceInstance> instances,
            List<ServiceInstance> oldInstances) {
        notifyExecutor.submit(new Runnable() {
            @Override
            public void run() {
                List<ServiceInstance> removed = diffServiceInstances(oldInstances, instances, true);
                List<ServiceInstance> added = diffServiceInstances(oldInstances, instances, false);

                logger.info("Service Discovery Client on changed. Removed instances : " + removed);
                logger.info("Service Discovery Client on changed. Added instances : " + added);

                for (ServiceNotifyListener listener : listeners) {
                    listener.notify(service, instances);
                    listener.notifyOnRemoved(service, removed);
                    listener.notifyOnAdded(service, added);
                }
            }
        });
    }


    protected List<ServiceInstance> diffServiceInstances(List<ServiceInstance> oldList, List<ServiceInstance> newList,
            boolean removed) {
        List<ServiceInstance> newServiceInstance = new ArrayList<>();

        Set<ServiceInstance> set = new HashSet();

        if (removed) {
            if (!CollectionUtil.isEmpty(oldList)) {
                set.addAll(oldList);
            }

            if (!CollectionUtil.isEmpty(newList)) {
                set.removeAll(newList);
            }
        } else {
            if (!CollectionUtil.isEmpty(newList)) {
                set.addAll(newList);
            }

            if (!CollectionUtil.isEmpty(oldList)) {
                set.removeAll(oldList);
            }
        }

        newServiceInstance.addAll(set);
        return Collections.unmodifiableList(newServiceInstance);
    }

    @Override
    public synchronized void unsubscribe(Service service) {
        if (service == null) {
            throw new IllegalArgumentException("service can not be null");
        }

        if (subscribed.contains(service)) {
            doUnSubscribe(service);
            logger.info("Unsubscribe service:" + service);
        }
    }

    @Override
    public void removeNotifyListener(ServiceNotifyListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
            logger.info("Remove ServiceNotifyListener : " + listener);
        }
    }

    protected abstract void doUnSubscribe(Service service);
}
