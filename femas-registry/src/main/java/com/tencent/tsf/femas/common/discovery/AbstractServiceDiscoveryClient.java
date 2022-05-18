package com.tencent.tsf.femas.common.discovery;

import com.google.common.collect.Sets;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.common.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zhixinzxliu
 */
public abstract class AbstractServiceDiscoveryClient implements ServiceDiscoveryClient {

    private static final Logger logger = LoggerFactory.getLogger(AbstractServiceDiscoveryClient.class);

    protected volatile Set<Service> subscribed = Sets.newConcurrentHashSet();

    protected volatile Set<Service> initialized = Sets.newConcurrentHashSet();

    private volatile Set<ServiceNotifyListener> listeners = Sets.newConcurrentHashSet();

    private volatile ExecutorService notifyExecutor;

    protected AbstractServiceDiscoveryClient() {
        this.notifyExecutor = Executors.newCachedThreadPool(new NamedThreadFactory("ServiceNotifyListener", true));
    }

    /**
     * 订阅服务
     *
     * @param service 微服务实体模型(命名空间和名称)
     */
    @Override
    public synchronized void subscribe(Service service) {
        if (service == null) {
            throw new IllegalArgumentException("service can not be null");
        }

        if (!subscribed.contains(service)) {
            doSubscribe(service);
            subscribed.add(service);
            logger.info("Subscribe service: {}", service);
        }
    }

    /**
     * 添加listener
     *
     * @param listener 订阅者
     */
    @Override
    public void addNotifyListener(ServiceNotifyListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("subscribe listener can not be null");
        }

        if (!listeners.contains(listener)) {
            listeners.add(listener);
            logger.info("Add ServiceNotifyListener : {}", listener);
        }
    }

    /**
     * 订阅服务
     *
     * @param service 微服务实体模型(命名空间和名称)
     */
    protected abstract void doSubscribe(Service service);

    /**
     * 异步通知listeners
     *
     * @param service      微服务实体模型(命名空间和名称)
     * @param instances    新的服务实体List
     * @param oldInstances 原来的服务实体List
     */
    protected void notifyListeners(Service service, List<ServiceInstance> instances,
                                   List<ServiceInstance> oldInstances) {
        notifyExecutor.submit(() -> {
            List<ServiceInstance> removed = diffServiceInstances(oldInstances, instances, true);
            List<ServiceInstance> added = diffServiceInstances(oldInstances, instances, false);

            logger.info("Service Discovery Client on changed. Removed instances : {}", removed);
            logger.info("Service Discovery Client on changed. Added instances : {}", added);

            for (ServiceNotifyListener listener : listeners) {
                listener.notify(service, instances);
                listener.notifyOnRemoved(service, removed);
                listener.notifyOnAdded(service, added);
            }
        });
    }


    /**
     * 获得两个列表的差异
     *
     * @param oldList 旧的列表
     * @param newList 新的列表
     * @param removed 是否是移除-true为获得移除，false为获得新增
     * @return 差异列表-true为获得移除list，false为获得新增list
     */
    protected List<ServiceInstance> diffServiceInstances(List<ServiceInstance> oldList, List<ServiceInstance> newList,
                                                         boolean removed) {

        Set<ServiceInstance> set = new HashSet<>();

        if (removed) {
            // 旧list移除新list，为移除的
            if (!CollectionUtil.isEmpty(oldList)) {
                set.addAll(oldList);
            }

            if (!CollectionUtil.isEmpty(newList)) {
                newList.forEach(set::remove);
            }
        } else {
            // 新list移除旧list,为新增加的
            if (!CollectionUtil.isEmpty(newList)) {
                set.addAll(newList);
            }

            if (!CollectionUtil.isEmpty(oldList)) {
                oldList.forEach(set::remove);
            }
        }

        List<ServiceInstance> newServiceInstance = new ArrayList<>(set);
        return Collections.unmodifiableList(newServiceInstance);
    }

    /**
     * 取消订阅
     *
     * @param service 微服务实体模型(命名空间和名称)
     */
    @Override
    public synchronized void unsubscribe(Service service) {
        if (service == null) {
            throw new IllegalArgumentException("service can not be null");
        }

        if (subscribed.contains(service)) {
            doUnSubscribe(service);
            logger.info("Unsubscribe service:{}", service);
        }
    }

    /**
     * 移除listener
     *
     * @param listener 订阅者
     */
    @Override
    public void removeNotifyListener(ServiceNotifyListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
            logger.info("Remove ServiceNotifyListener : {}", listener);
        }
    }

    /**
     * 取消订阅
     *
     * @param service 微服务实体模型(命名空间和名称)
     */
    protected abstract void doUnSubscribe(Service service);

}
