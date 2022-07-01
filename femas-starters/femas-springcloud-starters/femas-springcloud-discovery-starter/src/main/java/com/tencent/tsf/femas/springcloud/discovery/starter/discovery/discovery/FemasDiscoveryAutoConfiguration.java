package com.tencent.tsf.femas.springcloud.discovery.starter.discovery.discovery;

import com.alibaba.cloud.nacos.registry.NacosRegistrationCustomizer;
import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;
import com.tencent.tsf.femas.extension.springcloud.common.discovery.eureka.FemasEurekaRegistrationCustomizer;
import com.tencent.tsf.femas.springcloud.discovery.starter.discovery.ConditionalOnFemasDiscoveryEnabled;
import com.tencent.tsf.femas.springcloud.discovery.starter.discovery.FemasDiscoveryProperties;
import com.tencent.tsf.femas.springcloud.discovery.starter.discovery.discovery.consul.ConsulServerConverter;
import com.tencent.tsf.femas.springcloud.discovery.starter.discovery.discovery.eureka.EurekaServerConverter;
import com.tencent.tsf.femas.springcloud.discovery.starter.discovery.discovery.nacos.NacosServerConverter;
import com.tencent.tsf.femas.springcloud.discovery.starter.discovery.discovery.ribbo.DiscoveryServerConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.consul.discovery.ConsulServer;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistrationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 描述：
 * 创建日期：2022年05月18 19:41:12
 *
 * @author gong zhao
 **/
@Configuration(proxyBeanMethods = false)
@ConditionalOnDiscoveryEnabled
@ConditionalOnFemasDiscoveryEnabled
public class FemasDiscoveryAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public FemasDiscoveryProperties femasDiscoveryProperties() {
        return new FemasDiscoveryProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public FemasServiceDiscovery femasServiceDiscovery(
            FemasDiscoveryProperties femasDiscoveryProperties) {
        return new FemasServiceDiscovery(femasDiscoveryProperties);
    }


    @Configuration
    @ConditionalOnProperty(
            name = {"spring.cloud.femas.discovery.registryType"},
            havingValue = "consul"
    )
    @ConditionalOnMissingBean(DiscoveryServerConverter.class)
    @ConditionalOnClass({ConsulServer.class, ConsulRegistrationCustomizer.class})
    static class FemasConsulConfiguration {
        @Bean("converterAdapter")
        public DiscoveryServerConverter consulConverterAdapter() {
            return new ConsulServerConverter();
        }
    }

    @Configuration
    @ConditionalOnProperty(
            name = {"spring.cloud.femas.discovery.registryType"},
            havingValue = "nacos"
    )
    @ConditionalOnMissingBean(DiscoveryServerConverter.class)
    @ConditionalOnClass({NacosServer.class, NacosRegistrationCustomizer.class})
    static class FemasNacosConfiguration {
        @Bean("converterAdapter")
        public DiscoveryServerConverter nacosConverterAdapter() {
            return new NacosServerConverter();
        }
    }

    @Configuration
    @ConditionalOnProperty(
            name = {"spring.cloud.femas.discovery.registryType"},
            havingValue = "eureka"
    )
    @ConditionalOnMissingBean(DiscoveryServerConverter.class)
    @ConditionalOnClass({DiscoveryEnabledServer.class, FemasEurekaRegistrationCustomizer.class})
    static class FemasEurekaConfiguration {
        @Bean("converterAdapter")
        public DiscoveryServerConverter eurekaConverterAdapter() {
            return new EurekaServerConverter();
        }

        @Bean
        public FemasEurekaRegistrationCustomizer femasEurekaRegistrationCustomizer() {
            return new FemasEurekaRegistrationCustomizer();
        }
    }
}
