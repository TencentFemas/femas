package com.tencent.tsf.femas.springcloud.discovery.starter.discovery.registry;

import com.tencent.tsf.femas.springcloud.discovery.starter.discovery.FemasDiscoveryProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationAutoConfiguration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 描述：
 * 创建日期：2022年05月17 19:36:07
 *
 * @author gong zhao
 **/
@Configuration(
        proxyBeanMethods = false
)
@EnableConfigurationProperties
@ConditionalOnProperty(
        value = {"spring.cloud.service-registry.auto-registration.enabled"},
        matchIfMissing = true
)
@AutoConfigureAfter({AutoServiceRegistrationConfiguration.class,
        AutoServiceRegistrationAutoConfiguration.class})
public class FemasServiceRegistryAutoConfiguration {
    @Bean
    public FemasServiceRegistry femasServiceRegistry(FemasDiscoveryProperties femasDiscoveryProperties) {
        return new FemasServiceRegistry(femasDiscoveryProperties);
    }

    @Bean
    @ConditionalOnBean(AutoServiceRegistrationProperties.class)
    public FemasRegistration femasRegistration(FemasDiscoveryProperties femasDiscoveryProperties) {
        return new FemasRegistration(femasDiscoveryProperties);
    }

    @Bean
    @Primary
    @ConditionalOnBean(AutoServiceRegistrationProperties.class)
    public FemasAutoServiceRegistration femasAutoServiceRegistration(
            FemasServiceRegistry registry,
            AutoServiceRegistrationProperties autoServiceRegistrationProperties,
            FemasRegistration registration) {
        return new FemasAutoServiceRegistration(registry,
                autoServiceRegistrationProperties, registration);
    }
}
