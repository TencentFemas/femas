package com.tencent.tsf.femas.registry.impl.consul.discovery;

import com.google.common.collect.Maps;
import com.tencent.tsf.femas.common.RegistryConstants;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.registry.impl.consul.ConsulConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class ConsulServiceDiscoveryClientTest {

    private ConsulServiceDiscoveryClient discoveryClient;

    @Before
    public void setUp() throws Exception {
        Map<String, String> configMap = Maps.newConcurrentMap();
        configMap.put(RegistryConstants.REGISTRY_HOST, "106.53.107.83");
        configMap.put(RegistryConstants.REGISTRY_PORT, "8080");
        configMap.put(ConsulConstants.CONSUL_ACCESS_TOKEN, "token");

        discoveryClient = new ConsulServiceDiscoveryClient(configMap);
    }

    @Test
    public void testSubscribeSuccess() throws Exception {
        Service service = new Service("12312", "123123");
        discoveryClient.subscribe(service);
    }
}