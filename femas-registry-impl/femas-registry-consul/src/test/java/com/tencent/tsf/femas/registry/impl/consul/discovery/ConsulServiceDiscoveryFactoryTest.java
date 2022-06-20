package com.tencent.tsf.femas.registry.impl.consul.discovery;

import com.tencent.tsf.femas.common.RegistryConstants;
import com.tencent.tsf.femas.common.RegistryEnum;
import com.tencent.tsf.femas.common.discovery.ServiceDiscoveryClient;
import com.tencent.tsf.femas.registry.impl.consul.ConsulConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConsulServiceDiscoveryFactoryTest {

    private Map<String, String> configMap;

    private ConsulServiceDiscoveryFactory consulServiceDiscoveryFactory;

    @Before
    public void setUp() throws Exception {
        consulServiceDiscoveryFactory = new ConsulServiceDiscoveryFactory();
    }

    @Test(timeout = 30000)
    public void testGetType() {
        Assert.assertEquals(consulServiceDiscoveryFactory.getType(), RegistryEnum.CONSUL.name());
    }

    @Test(timeout = 30000)
    public void tetGetServiceDiscovery() {
        configMap = new ConcurrentHashMap<>();
        configMap.put(RegistryConstants.REGISTRY_HOST, "test.net");
        configMap.put(RegistryConstants.REGISTRY_PORT, "8080");
        configMap.put(ConsulConstants.CONSUL_ACCESS_TOKEN, "token");
        ServiceDiscoveryClient serviceDiscovery = consulServiceDiscoveryFactory.getServiceDiscovery(configMap);
        Assert.assertTrue(serviceDiscovery != null);
    }
}