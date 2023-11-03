package com.tencent.tsf.femas.registry.impl.zookeeper;

import com.tencent.tsf.femas.common.RegistryConstants;
import com.tencent.tsf.femas.common.discovery.AbstractServiceDiscoveryClient;
import com.tencent.tsf.femas.common.entity.EndpointStatus;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistry;
import com.tencent.tsf.femas.registry.impl.zookeeper.discovery.ZookeeperServiceDiscoveryClient;
import com.tencent.tsf.femas.registry.impl.zookeeper.serviceregistry.ZookeeperServiceRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author huyuanxin
 */
public class ZookeeperRegistryTest {

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
                100
        );
    }

    /**
     * 每次new一个新的原因是,robin轮询刷新需要30s,new 一次进行单测效率更高
     *
     * @return 新的EtcdServiceDiscoveryClient
     */
    private AbstractServiceDiscoveryClient getDiscoveryClient() {
        return new ZookeeperServiceDiscoveryClient(configMap);
    }

    @Before
    public void init() {
        configMap = new HashMap<>(3);
        configMap.put(RegistryConstants.REGISTRY_HOST, "127.0.0.1");
        configMap.put(RegistryConstants.REGISTRY_PORT, "2181");
        abstractServiceRegistry = new ZookeeperServiceRegistry(configMap);
    }

    @Test
    public void testRegistryAndDeregistry() throws InterruptedException {
        abstractServiceRegistry.register(getServiceInstance("registry-test-1", 8080));
        AbstractServiceDiscoveryClient discoveryClient = getDiscoveryClient();
        List<ServiceInstance> serviceInstanceList = discoveryClient.getInstances(getService());
        Assert.assertEquals(1, serviceInstanceList.size());
        abstractServiceRegistry.deregister(getServiceInstance("registry-test-1", 8080));
        Thread.sleep(10000L);
        discoveryClient = getDiscoveryClient();
        serviceInstanceList = discoveryClient.getInstances(getService());
        Assert.assertEquals(0, serviceInstanceList.size());
    }

    @Test
    public void testRegistryAndDeregistryMany() throws InterruptedException {
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
