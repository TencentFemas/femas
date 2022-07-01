package com.tencent.tsf.femas.springcloud.discovery.starter.discovery;

import com.tencent.tsf.femas.common.RegistryConstants;
import com.tencent.tsf.femas.common.RegistryEnum;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.discovery.DiscoveryService;
import com.tencent.tsf.femas.common.serviceregistry.RegistryService;
import com.tencent.tsf.femas.common.util.AddressUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import javax.annotation.PostConstruct;
import java.util.Map;

import static com.tencent.tsf.femas.config.impl.paas.PaasConstants.*;

/**
 * 描述：
 * 创建日期：2022年05月16 23:35:02
 *
 * @author gong zhao
 **/
@ConfigurationProperties("spring.cloud.femas.discovery")
@EnableConfigurationProperties(FemasDiscoveryProperties.class)
public class FemasDiscoveryProperties {
    private static final Logger LOGGER = LoggerFactory.getLogger(FemasDiscoveryProperties.class);

    private volatile static Context commonContext = ContextFactory.getContextInstance();

    @Value("${server.port}")
    private Integer serverPort;

    private String serverHost;

    @Value("${spring.cloud.femas.discovery.serverAddr:")
    private String serverAddr;

    @Value("${spring.cloud.femas.discovery.ip:")
    private String ip;

    @Value("${spring.cloud.femas.discovery.port:#{-1}}")
    private int port = -1;

    @Value("${spring.cloud.femas.discovery.username:")
    private String username;

    @Value("${spring.cloud.femas.discovery.password:")
    private String password;

    @Value("${spring.cloud.femas.discovery.token:")
    private String token;

    @Value("${spring.cloud.femas.discovery.registryType}")
    private RegistryEnum registryType;

    @Value("${spring.cloud.femas.discovery.namespace:#{'default'}}")
    private String namespace;

    @Value("${spring.cloud.femas.discovery.service:${spring.application.name:}}")
    private String service;

    @Value("${spring.cloud.femas.discovery.register.enabled:#{true}}")
    private Boolean registerEnabled;

    @Value("${spring.cloud.femas.discovery.register.secure:#{false}}")
    private Boolean secure = false;

    private volatile com.tencent.tsf.femas.common.serviceregistry.ServiceRegistry serviceRegistry;

    private volatile com.tencent.tsf.femas.common.discovery.ServiceDiscoveryClient serviceDiscoveryClient;

    public FemasDiscoveryProperties() {
    }

    @PostConstruct
    public void init() {
        Map<String, String> confMap = commonContext.getRegistryConfigMap();
        if (StringUtils.isNotEmpty(this.getServerAddr())) {
            confMap.put(PAAS_SERVER_ADDRESS, this.getServerAddr());
        }
        if (StringUtils.isNotEmpty(this.getIp())) {
            confMap.put(RegistryConstants.REGISTRY_HOST, this.getIp());
        }
        if (this.getPort() != -1) {
            confMap.put(RegistryConstants.REGISTRY_PORT, this.getPort() + "");
        }
        if (this.getRegistryType() != null) {
            confMap.put(RegistryConstants.REGISTRY_TYPE, this.getRegistryType().getAlias());
        }
        if (StringUtils.isNotEmpty(this.getNamespace())) {
            confMap.put(NAMESPACE_ID, this.getNamespace());
        }
        if (StringUtils.isNotEmpty(this.getService())) {
            confMap.put(SERVICE_NAME, this.getService());
        }
        setServerHost(AddressUtils.getValidLocalHost());
        //todo username、password、token、enabled参数暂时忽略
    }

    public com.tencent.tsf.femas.common.serviceregistry.ServiceRegistry serviceRegistry() {
        if (serviceRegistry == null) {
            synchronized (this) {
                if (serviceRegistry == null) {
                    serviceRegistry = RegistryService
                            .createRegistry(registryType.getAlias(), commonContext.getRegistryConfigMap());
                }
            }
        }
        return serviceRegistry;
    }

    public com.tencent.tsf.femas.common.discovery.ServiceDiscoveryClient serviceDiscoveryClient() {
        if (serviceDiscoveryClient == null) {
            synchronized (this) {
                if (serviceDiscoveryClient == null) {
                    serviceDiscoveryClient = DiscoveryService
                            .createDiscoveryClient(registryType, commonContext.getRegistryConfigMap());
                }
            }
        }
        return serviceDiscoveryClient;
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Boolean isRegisterEnabled() {
        return registerEnabled;
    }

    public void setRegisterEnabled(Boolean registerEnabled) {
        this.registerEnabled = registerEnabled;
    }

    public RegistryEnum getRegistryType() {
        return registryType;
    }

    public void setRegistryType(RegistryEnum registryType) {
        this.registryType = registryType;
    }

    public Boolean getSecure() {
        return secure;
    }

    public void setSecure(Boolean secure) {
        this.secure = secure;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }
}
