package com.tencent.tsf.femas.springcloud.discovery.starter.discovery.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 描述：
 * 创建日期：2022年05月17 19:42:35
 *
 * @author gong zhao
 **/
public class FemasAutoServiceRegistration extends AbstractAutoServiceRegistration<Registration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FemasAutoServiceRegistration.class);

    private FemasRegistration registration;

    protected FemasAutoServiceRegistration(ServiceRegistry<Registration> serviceRegistry,
                                           AutoServiceRegistrationProperties properties,
                                           FemasRegistration femasRegistration) {
        super(serviceRegistry, properties);
        this.registration = femasRegistration;
    }

    @Override
    protected Registration getRegistration() {
        if (this.registration.getPort() < 0 && this.getPort().get() > 0) {
            this.registration.setPort(this.getPort().get());
        }
        Assert.isTrue(this.registration.getPort() > 0, "service.port has not been set");
        return this.registration;
    }

    @Override
    protected Registration getManagementRegistration() {
        return null;
    }

    @Override
    protected void register() {
        if (!this.registration.getFemasDiscoveryProperties().isRegisterEnabled()) {
            LOGGER.debug("Registration disabled.");
            return;
        }
        if (this.registration.getPort() < 0) {
            this.registration.setPort(getPort().get());
        }
        super.register();
    }

    @Override
    protected void registerManagement() {
        if (this.registration.getFemasDiscoveryProperties().isRegisterEnabled()) {
            super.registerManagement();
        }
    }

    @Override
    protected Object getConfiguration() {
        return this.registration.getFemasDiscoveryProperties();
    }

    @Override
    protected boolean isEnabled() {
        return this.registration.getFemasDiscoveryProperties().isRegisterEnabled();
    }

    @Override
    protected String getAppName() {
        String appName = this.registration.getFemasDiscoveryProperties().getService();
        return StringUtils.isEmpty(appName) ? super.getAppName() : appName;
    }
}
