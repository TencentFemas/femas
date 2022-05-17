/*
 * Tencent is pleased to support the open source community by making Femas available.
 *
 * Copyright (C) 2021, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */   
package com.tencent.tsf.femas.registry.impl.polaris.discovery;

import com.tencent.polaris.api.core.ConsumerAPI;
import com.tencent.polaris.api.pojo.Instance;
import com.tencent.polaris.api.rpc.GetAllInstancesRequest;
import com.tencent.polaris.api.rpc.InstancesResponse;
import com.tencent.polaris.factory.api.DiscoveryAPIFactory;
import com.tencent.tsf.femas.common.discovery.AbstractServiceDiscoveryClient;
import com.tencent.tsf.femas.common.discovery.SchedulePollingServerListUpdater;
import com.tencent.tsf.femas.common.discovery.ServerUpdater;
import com.tencent.tsf.femas.common.entity.EndpointStatus;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
* <pre>  
* 文件名称：PolarisServiceDiscoveryClient.java  
* 创建时间：Dec 29, 2021 12:09:52 PM   
* @author juanyinyang  
* 类说明：  
*/
public class PolarisServiceDiscoveryClient extends AbstractServiceDiscoveryClient {

    private static final  Logger LOGGER = LoggerFactory.getLogger(PolarisServiceDiscoveryClient.class);
    protected volatile ServerUpdater serverListUpdater;
    private final PolarisServerList serverListImpl;
    private final Map<Service, List<ServiceInstance>> instances = new ConcurrentHashMap<>();
    private Map<Service, Notifier> notifiers = new ConcurrentHashMap<>();
    protected AtomicBoolean serverListUpdateInProgress;
    private ConsumerAPI consumerAPI = null;
    
    public PolarisServiceDiscoveryClient(Map<String, String> configMap) {
        //TODO Builder?
        this.serverListUpdateInProgress = new AtomicBoolean(false);
        this.serverListUpdater = new SchedulePollingServerListUpdater();
        this.serverListImpl = new PolarisServerList();
        consumerAPI = DiscoveryAPIFactory.createConsumerAPI();
    }
    
    /** 
     * @see com.tencent.tsf.femas.common.discovery.ServiceDiscoveryClient#getInstances(com.tencent.tsf.femas.common.entity.Service)
     */
    @Override
    public List<ServiceInstance> getInstances(Service service) {
        List<ServiceInstance> instancesList = instances.get(service);
        if (instancesList != null) {
            return instancesList;
        }
        List<Instance> instances = serverListImpl.getInitialListOfServers(service.getNamespace(), service.getName());
        instancesList = convert(service, instances);
        refreshServiceCache(service, instancesList);
        return instancesList;
    }

    @Override
    public List<String> getAllServices() {
        List<Instance> instances = Arrays.stream(consumerAPI.getAllInstance(new GetAllInstancesRequest()).getInstances())
                .collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(instances)) {
            return Collections.emptyList();
        }

        return instances
                .stream()
                .map(Instance::getService)
                .collect(Collectors.toList());
    }

    /** 
     * @see com.tencent.tsf.femas.common.discovery.AbstractServiceDiscoveryClient#doSubscribe(com.tencent.tsf.femas.common.entity.Service)
     */
    @Override
    protected void doSubscribe(Service service) {
        Notifier notifier = new Notifier(service);
        notifier.run();
        notifiers.putIfAbsent(service, notifier);
    }

    /** 
     * @see com.tencent.tsf.femas.common.discovery.AbstractServiceDiscoveryClient#doUnSubscribe(com.tencent.tsf.femas.common.entity.Service)
     */
    @Override
    protected void doUnSubscribe(Service service) {
        serverListUpdater.stop(notifiers.get(service).scheduledFuture);
        notifiers.remove(service);
    }
    
    public ScheduledFuture enableAndInitLearnNewServersFeature(Service service) {
        LOGGER.info("Using serverListUpdater {}", this.serverListUpdater.getClass().getSimpleName());
        ScheduledFuture scheduledFuture = this.serverListUpdater.start(new Action(service));
        return scheduledFuture;
    }
    
    public void updateListOfServers(Service service) {
        List<Instance> instances = new ArrayList<>();
        if (this.serverListImpl != null) {
            instances = this.serverListImpl.getUpdatedListOfServers(service.getNamespace(), service.getName());
//            LOGGER.debug("List of Servers for {} obtained from Discovery client: {}", this.getIdentifier(), servers);
        }

        this.updateAllServerList(service, instances);
    }
    
    protected void updateAllServerList(Service service, List<Instance> ls) {
        if (this.serverListUpdateInProgress.compareAndSet(false, true)) {
            try {
                List<ServiceInstance> newInstances = convert(service, ls);
                List<ServiceInstance> oldInstances = instances.get(service);
                this.refreshServiceCache(service, newInstances);
                this.notifyListeners(service, newInstances, oldInstances);
            } finally {
                this.serverListUpdateInProgress.set(false);
            }
        }
    }
    
    private void refreshServiceCache(Service service, List<ServiceInstance> instances) {
        this.instances.put(service, instances);
    }
    
    List<ServiceInstance> convert(Service service, List<Instance> ls) {
        List<ServiceInstance> instances = new ArrayList<>();
        ls.stream().forEach(i -> {
            ServiceInstance instance = new ServiceInstance();
            instance.setAllMetadata(i.getMetadata());
            instance.setHost(i.getHost());
            instance.setPort(i.getPort());
            instance.setService(service);
            instance.setStatus(EndpointStatus.UP);
            instances.add(instance);
        });
        return instances;
    }
    
    //server级别监听
    class PolarisServerList {
        
        public List<Instance> getInitialListOfServers(String namespace, String serviceName) {
            return getServers(namespace, serviceName);
        }

        public List<Instance> getUpdatedListOfServers(String namespace, String serviceName) {
            return getServers(namespace, serviceName);
        }

        private List<Instance> getServers(String namespace, String serviceName) {
            try {
                //拉取所有服务实例 GetAllInstancesRequest 
                GetAllInstancesRequest request = new GetAllInstancesRequest();
                request.setNamespace(namespace);
                request.setService(serviceName);
                InstancesResponse instancesResponse = consumerAPI.getAllInstance(request);
                Instance[] instances = instancesResponse.getInstances();
                return Arrays.stream(instances).collect(Collectors.toList());
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Can not get service instances from polaris, namespace="+namespace+",serviceName="+serviceName, e);
            }
        }
    }
    
    class Action implements ServerUpdater.UpdateAction {

        private final Service service;

        Action(Service service) {
            this.service = service;
        }

        public void doUpdate() {
            PolarisServiceDiscoveryClient.this.updateListOfServers(service);
        }
    }
    
    private class Notifier {
        private final Service service;
        private ScheduledFuture<?> scheduledFuture;

        public Notifier(Service service) {
            this.service = service;
        }

        public void run() {
            this.scheduledFuture = enableAndInitLearnNewServersFeature(service);
        }
    }

}
  