package com.tencent.tsf.femas.registry.impl.consul.config;

import static com.tencent.tsf.femas.common.util.CommonUtils.getOrDefault;
import static com.tencent.tsf.femas.registry.impl.consul.ConsulConstants.CONSUL_HEALTH_CHECK_CRITICAL_TIMEOUT;
import static com.tencent.tsf.femas.registry.impl.consul.ConsulConstants.CONSUL_HEALTH_CHECK_INTERVAL;
import static com.tencent.tsf.femas.registry.impl.consul.ConsulConstants.CONSUL_HEALTH_CHECK_TIMEOUT;
import static com.tencent.tsf.femas.registry.impl.consul.ConsulConstants.CONSUL_HEALTH_CHECK_TLS_SKIP_VERIFY;
import static com.tencent.tsf.femas.registry.impl.consul.ConsulConstants.CONSUL_HEALTH_CHECK_URL;
import static com.tencent.tsf.femas.registry.impl.consul.ConsulConstants.DEFAULT_CONSUL_HEALTH_CHECK_CRITICAL_TIMEOUT;
import static com.tencent.tsf.femas.registry.impl.consul.ConsulConstants.DEFAULT_CONSUL_HEALTH_CHECK_INTERVAL;
import static com.tencent.tsf.femas.registry.impl.consul.ConsulConstants.DEFAULT_CONSUL_HEALTH_CHECK_TIMEOUT;

import java.util.Map;

public class ConsulHealthCheckConfig {

    /**
     * Custom health check url to override default
     */
    private String healthCheckUrl;

    /**
     * How often to perform the health check (e.g. 10s), defaults to 10s.
     */
    private String healthCheckInterval;

    /**
     * Timeout for health check (e.g. 10s).
     */
    private String healthCheckTimeout;

    /**
     * Timeout to deregister services critical for longer than timeout (e.g. 30m).
     * Requires consul version 7.x or higher.
     */
    private String healthCheckCriticalTimeout;

    /**
     * Skips certificate verification during service checks if true, otherwise runs
     * certificate verification.
     */
    private Boolean healthCheckTlsSkipVerify;

    public ConsulHealthCheckConfig(Map<String, String> configMap) {
        this.healthCheckUrl = configMap.get(CONSUL_HEALTH_CHECK_URL);
        this.healthCheckInterval = getOrDefault(configMap.get(CONSUL_HEALTH_CHECK_INTERVAL),
                DEFAULT_CONSUL_HEALTH_CHECK_INTERVAL);
        this.healthCheckTimeout = getOrDefault(configMap.get(CONSUL_HEALTH_CHECK_TIMEOUT),
                DEFAULT_CONSUL_HEALTH_CHECK_TIMEOUT);
        this.healthCheckCriticalTimeout = getOrDefault(configMap.get(CONSUL_HEALTH_CHECK_CRITICAL_TIMEOUT),
                DEFAULT_CONSUL_HEALTH_CHECK_CRITICAL_TIMEOUT);

        try {
            this.healthCheckTlsSkipVerify = Boolean.parseBoolean(configMap.get(CONSUL_HEALTH_CHECK_TLS_SKIP_VERIFY));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getHealthCheckUrl() {
        return healthCheckUrl;
    }

    public String getHealthCheckInterval() {
        return healthCheckInterval;
    }

    public String getHealthCheckTimeout() {
        return healthCheckTimeout;
    }

    public String getHealthCheckCriticalTimeout() {
        return healthCheckCriticalTimeout;
    }

    public Boolean getHealthCheckTlsSkipVerify() {
        return healthCheckTlsSkipVerify;
    }

    @Override
    public String toString() {
        return "ConsulHealthCheckConfig{" +
                "healthCheckUrl='" + healthCheckUrl + '\'' +
                ", healthCheckInterval='" + healthCheckInterval + '\'' +
                ", healthCheckTimeout='" + healthCheckTimeout + '\'' +
                ", healthCheckCriticalTimeout='" + healthCheckCriticalTimeout + '\'' +
                ", healthCheckTlsSkipVerify=" + healthCheckTlsSkipVerify +
                '}';
    }
}
