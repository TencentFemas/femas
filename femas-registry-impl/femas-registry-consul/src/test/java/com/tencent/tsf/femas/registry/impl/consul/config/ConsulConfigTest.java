package com.tencent.tsf.femas.registry.impl.consul.config;

import com.tencent.tsf.femas.common.RegistryConstants;
import com.tencent.tsf.femas.registry.impl.consul.ConsulConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConsulConfigTest {

    private Map<String, String> configMap;

    private ConsulConfig consulConfig;

    @Before
    public void setUp() throws Exception {
        configMap = new ConcurrentHashMap<>();
        configMap.put(RegistryConstants.REGISTRY_HOST, "test.net");
        configMap.put(RegistryConstants.REGISTRY_PORT, "8080");
        configMap.put(ConsulConstants.CONSUL_ACCESS_TOKEN, "token");
    }

    @Test(timeout = 30000)
    public void testConstructSuccess() {
        consulConfig = new ConsulConfig(configMap);
        Assert.assertEquals(consulConfig.getHost(), "test.net");
        Assert.assertEquals(consulConfig.getPort(), 8080);
        Assert.assertEquals(consulConfig.getToken(), "token");
    }

    @Test(timeout = 30000,expected = IllegalArgumentException.class)
    public void testRegistryHostIsNull() {
        configMap.remove(RegistryConstants.REGISTRY_HOST);
        consulConfig = new ConsulConfig(configMap);
    }

    @Test(timeout = 30000,expected = IllegalArgumentException.class)
    public void testRegistryPortIsNull() {
        configMap.remove(RegistryConstants.REGISTRY_PORT);
        consulConfig = new ConsulConfig(configMap);
    }
}