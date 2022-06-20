package com.tencent.tsf.femas.registry.impl.consul.config;

import com.tencent.tsf.femas.registry.impl.consul.ConsulConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConsulHealthCheckConfigTest {

    private Map<String, String> configMap;

    private ConsulHealthCheckConfig consulHealthCheckConfig;

    @Before
    public void setUp() throws Exception {
        configMap = new ConcurrentHashMap<>();
        configMap.put(ConsulConstants.CONSUL_HEALTH_CHECK_URL, "/health/check");
        configMap.put(ConsulConstants.CONSUL_HEALTH_CHECK_INTERVAL, "15s");
        configMap.put(ConsulConstants.CONSUL_HEALTH_CHECK_TIMEOUT, "6s");
    }

    @Test(timeout = 30000)
    public void testConstructSuccess() {
        consulHealthCheckConfig = new ConsulHealthCheckConfig(configMap);
        Assert.assertEquals(consulHealthCheckConfig.getHealthCheckUrl(), "/health/check");
        Assert.assertEquals(consulHealthCheckConfig.getHealthCheckInterval(), "15s");
        Assert.assertEquals(consulHealthCheckConfig.getHealthCheckTimeout(), "6s");
        Assert.assertEquals(consulHealthCheckConfig.getHealthCheckCriticalTimeout(), "30m");
        Assert.assertEquals(consulHealthCheckConfig.getHealthCheckTlsSkipVerify(), false);
    }

}