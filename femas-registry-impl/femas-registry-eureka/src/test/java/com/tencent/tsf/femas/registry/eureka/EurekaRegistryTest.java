/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tencent.tsf.femas.registry.eureka;

import com.tencent.tsf.femas.common.RegistryConstants;
import com.tencent.tsf.femas.common.discovery.AbstractServiceDiscoveryClient;
import com.tencent.tsf.femas.common.entity.EndpointStatus;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistry;
import com.tencent.tsf.femas.registry.impl.eureka.discovery.EurekaServiceDiscoveryClient;
import com.tencent.tsf.femas.registry.impl.eureka.serviceregistry.EurekaServiceRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 如果想要单测成功执行,需要设置eureka的刷新时间为0,否则会存在缓存问题导致单测失效
 *
 * @author huyuanxin
 */
public class EurekaRegistryTest {

    private Map<String, String> configMap;

    private AbstractServiceRegistry abstractServiceRegistry;

    private Service getService() {
        return new Service("registry-test", "registry-test");
    }

    private ServiceInstance getServiceInstance(String id, Integer port) {
        return new ServiceInstance(getService(),
                id,
                "127.0.0.1",
                port,
                System.currentTimeMillis(),
                "1.0.0",
                "1.0.0",
                EndpointStatus.UP,
                "",
                new HashMap<>(),
                new HashMap<>(),
                true,
                5
        );
    }

    /**
     * 每次new一个新的原因是,robin轮询刷新需要30s,new 一次进行单测效率更高
     *
     * @return 新的EtcdServiceDiscoveryClient
     */
    private AbstractServiceDiscoveryClient getDiscoveryClient() {
        return new EurekaServiceDiscoveryClient(configMap);
    }

    @Before
    public void init() {
        configMap = new HashMap<>(3);
        configMap.put(RegistryConstants.REGISTRY_HOST, "127.0.0.1");
        configMap.put(RegistryConstants.REGISTRY_PORT, "8761");
        abstractServiceRegistry = new EurekaServiceRegistry(configMap);
    }

    @Test
    public void testRegistryAndDeregistry() {
        abstractServiceRegistry.register(getServiceInstance("registry-test-1", 8080));
        AbstractServiceDiscoveryClient discoveryClient = getDiscoveryClient();
        List<ServiceInstance> serviceInstanceList = discoveryClient.getInstances(getService());
        Assert.assertEquals(1, serviceInstanceList.size());
        abstractServiceRegistry.deregister(getServiceInstance("registry-test-1", 8080));
        discoveryClient = getDiscoveryClient();
        serviceInstanceList = discoveryClient.getInstances(getService());
        Assert.assertEquals(0, serviceInstanceList.size());
    }

    @Test
    public void testRegistryAndDeregistryMany() {
        // 注册第一个
        abstractServiceRegistry.register(getServiceInstance("registry-test-1", 8080));
        AbstractServiceDiscoveryClient discoveryClient = getDiscoveryClient();
        List<ServiceInstance> serviceInstanceList = discoveryClient.getInstances(getService());
        Assert.assertEquals(1, serviceInstanceList.size());
        // 注册第二个
        abstractServiceRegistry.register(getServiceInstance("registry-test-2", 8081));
        discoveryClient = getDiscoveryClient();
        serviceInstanceList = discoveryClient.getInstances(getService());
        Assert.assertEquals(2, serviceInstanceList.size());
        // 反注册一个
        abstractServiceRegistry.deregister(getServiceInstance("registry-test-1", 8080));
        discoveryClient = getDiscoveryClient();
        serviceInstanceList = discoveryClient.getInstances(getService());
        Assert.assertEquals(1, serviceInstanceList.size());
        // 反注册第二个
        abstractServiceRegistry.deregister(getServiceInstance("registry-test-2", 8081));
        discoveryClient = getDiscoveryClient();
        serviceInstanceList = discoveryClient.getInstances(getService());
        Assert.assertEquals(0, serviceInstanceList.size());
    }

}
