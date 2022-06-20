package com.tencent.tsf.femas.registry.impl.consul.serviceregistry;

import com.tencent.tsf.femas.common.RegistryConstants;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.registry.impl.consul.ConsulConstants;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConsulServiceRegistryTest {

    private Map<String, String> configMap;

    private ConsulServiceRegistry consulServiceRegistry;

    @Before
    public void setUp() throws Exception {
        configMap = new ConcurrentHashMap<>();
        configMap.put(RegistryConstants.REGISTRY_HOST, "test.net");
        configMap.put(RegistryConstants.REGISTRY_PORT, "8080");
        configMap.put(ConsulConstants.CONSUL_ACCESS_TOKEN, "token");

        consulServiceRegistry = new ConsulServiceRegistry(configMap);
    }

    @Test(timeout = 30000)
    public void doRegister() {
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setId("id1000");
        serviceInstance.setService(new Service("namespace", "name"));
        consulServiceRegistry.doRegister(serviceInstance);
    }
}