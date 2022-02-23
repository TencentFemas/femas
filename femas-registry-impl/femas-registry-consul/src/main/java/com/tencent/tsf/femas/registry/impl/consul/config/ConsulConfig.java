package com.tencent.tsf.femas.registry.impl.consul.config;

import static com.tencent.tsf.femas.common.util.CommonUtils.checkNotNull;
import static com.tencent.tsf.femas.common.util.CommonUtils.getOrDefault;
import static com.tencent.tsf.femas.registry.impl.consul.ConsulConstants.CONSUL_ACCESS_TOKEN;
import static com.tencent.tsf.femas.registry.impl.consul.ConsulConstants.CONSUL_ENABLE_TTL;
import static com.tencent.tsf.femas.registry.impl.consul.ConsulConstants.CONSUL_FAIL_FAST;
import static com.tencent.tsf.femas.registry.impl.consul.ConsulConstants.CONSUL_TTL;
import static com.tencent.tsf.femas.registry.impl.consul.ConsulConstants.DEFAULT_CONSUL_ENABLE_TTL;
import static com.tencent.tsf.femas.registry.impl.consul.ConsulConstants.DEFAULT_CONSUL_FAIL_FAST;
import static com.tencent.tsf.femas.registry.impl.consul.ConsulConstants.DEFAULT_CONSUL_TTL;

import com.tencent.tsf.femas.common.RegistryConstants;
import com.tencent.tsf.femas.common.util.StringUtils;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConsulConfig {

    private static final Logger logger = LoggerFactory.getLogger(ConsulConfig.class);

    private String token;
    private String host;
    private int port;
    private boolean enableTtl;
    private String ttl;
    private boolean failFast;

    private Map<String, String> properties;
    private ConsulHealthCheckConfig healthCheckConfig;

    public ConsulConfig(Map<String, String> configMap) {
        this.host = checkNotNull("CONSUL_HOST", configMap.get(RegistryConstants.REGISTRY_HOST));

        String portString = checkNotNull("CONSUL_PORT", configMap.get(RegistryConstants.REGISTRY_PORT));
        this.port = Integer.parseInt(portString);

        // token 可以为null，兼容本地调试启动
        this.token = configMap.get(CONSUL_ACCESS_TOKEN);

        // 是否采用心跳上报
        Boolean consulEnableTtl = null;
        try {
            String consulEnableTtlString = configMap.get(CONSUL_ENABLE_TTL);
            if (!StringUtils.isEmpty(consulEnableTtlString)) {
                consulEnableTtl = Boolean.parseBoolean(configMap.get(CONSUL_ENABLE_TTL));
            }
        } catch (Exception e) {
        }
        this.enableTtl = getOrDefault(consulEnableTtl, DEFAULT_CONSUL_ENABLE_TTL);

        // 是否启动注册failFast
        Boolean enableFailFast = null;
        try {
            String enableFailFastTtlString = configMap.get(CONSUL_FAIL_FAST);
            if (!StringUtils.isEmpty(enableFailFastTtlString)) {
                enableFailFast = Boolean.parseBoolean(configMap.get(CONSUL_FAIL_FAST));
            }
        } catch (Exception e) {
        }
        this.failFast = getOrDefault(enableFailFast, DEFAULT_CONSUL_FAIL_FAST);

        this.ttl = getOrDefault(configMap.get(CONSUL_TTL), DEFAULT_CONSUL_TTL);

        this.properties = configMap;
        this.healthCheckConfig = new ConsulHealthCheckConfig(configMap);

        logger.info("Import config : " + this);
    }

    public String getToken() {
        return token;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public boolean isEnableTtl() {
        return enableTtl;
    }

    public String getTtl() {
        return ttl;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public ConsulHealthCheckConfig getHealthCheckConfig() {
        return healthCheckConfig;
    }

    @Override
    public String toString() {
        return "ConsulConfig{" +
                "token='" + token + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", enableTtl=" + enableTtl +
                ", ttl='" + ttl + '\'' +
                ", failFast=" + failFast +
                ", properties=" + properties +
                ", healthCheckConfig=" + healthCheckConfig +
                '}';
    }
}
