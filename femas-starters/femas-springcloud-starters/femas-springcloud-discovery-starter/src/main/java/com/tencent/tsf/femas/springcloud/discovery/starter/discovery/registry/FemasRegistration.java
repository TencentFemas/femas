package com.tencent.tsf.femas.springcloud.discovery.starter.discovery.registry;

import com.tencent.cloud.metadata.context.MetadataContextHolder;
import com.tencent.tsf.femas.springcloud.discovery.starter.discovery.FemasDiscoveryProperties;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.serviceregistry.Registration;

import java.net.URI;
import java.util.Map;

/**
 * 描述：
 * 创建日期：2022年05月17 19:44:01
 *
 * @author gong zhao
 **/
public class FemasRegistration implements Registration, ServiceInstance {

    private FemasDiscoveryProperties femasDiscoveryProperties;

    public FemasRegistration(FemasDiscoveryProperties femasDiscoveryProperties) {
        this.femasDiscoveryProperties = femasDiscoveryProperties;
    }

    @Override
    public String getServiceId() {
        return this.femasDiscoveryProperties.getService();
    }

    @Override
    public String getHost() {
        return this.femasDiscoveryProperties.getIp();
    }

    @Override
    public int getPort() {
        return this.femasDiscoveryProperties.getPort();
    }

    public void setPort(int port) {
        this.femasDiscoveryProperties.setPort(port);
    }

    @Override
    public boolean isSecure() {
        return this.femasDiscoveryProperties.getSecure();
    }

    @Override
    public URI getUri() {
        return DefaultServiceInstance.getUri(this);
    }

    @Override
    public Map<String, String> getMetadata() {
        return MetadataContextHolder.get().getAllSystemMetadata();
    }

    public FemasDiscoveryProperties getFemasDiscoveryProperties() {
        return this.femasDiscoveryProperties;
    }
}
