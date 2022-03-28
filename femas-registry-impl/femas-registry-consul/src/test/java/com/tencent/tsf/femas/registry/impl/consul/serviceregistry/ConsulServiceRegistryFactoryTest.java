package com.tencent.tsf.femas.registry.impl.consul.serviceregistry;

import com.tencent.tsf.femas.common.RegistryConstants;
import com.tencent.tsf.femas.common.RegistryEnum;
import com.tencent.tsf.femas.registry.impl.consul.ConsulConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConsulServiceRegistryFactoryTest {

    private ConsulServiceRegistryFactory consulServiceRegistryFactory;

    private Map<String, String> configMap;

    @Before
    public void setUp() throws Exception {
        consulServiceRegistryFactory = new ConsulServiceRegistryFactory();
    }

    @Test
    public void testGetServiceRegistry() {
        configMap = new ConcurrentHashMap<>();
        configMap.put(RegistryConstants.REGISTRY_HOST, "test.net");
        configMap.put(RegistryConstants.REGISTRY_PORT, "8080");
        configMap.put(ConsulConstants.CONSUL_ACCESS_TOKEN, "token");
        Assert.assertTrue(consulServiceRegistryFactory.getServiceRegistry(configMap) != null);
    }

    @Test
    public void testGetType() {
        Assert.assertEquals(consulServiceRegistryFactory.getType(), RegistryEnum.CONSUL.name());
    }
}