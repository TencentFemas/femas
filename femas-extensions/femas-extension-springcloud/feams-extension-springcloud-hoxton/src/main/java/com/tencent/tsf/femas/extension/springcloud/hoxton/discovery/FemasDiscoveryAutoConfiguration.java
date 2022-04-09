package com.tencent.tsf.femas.extension.springcloud.hoxton.discovery;

import com.alibaba.cloud.nacos.registry.NacosRegistrationCustomizer;
import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;
import com.tencent.tsf.femas.extension.springcloud.common.discovery.eureka.FemasEurekaRegistrationCustomizer;
import com.tencent.tsf.femas.extension.springcloud.hoxton.discovery.consul.ConsulServerConverter;
import com.tencent.tsf.femas.extension.springcloud.hoxton.discovery.eureka.EurekaServerConverter;
import com.tencent.tsf.femas.extension.springcloud.hoxton.discovery.nacos.NacosServerConverter;
import com.tencent.tsf.femas.extension.springcloud.hoxton.discovery.ribbon.DiscoveryServerConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.consul.discovery.ConsulServer;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistrationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class FemasDiscoveryAutoConfiguration {

    @Configuration
    @ConditionalOnClass({ConsulServer.class, ConsulRegistrationCustomizer.class})
    static class FemasConsulConfiguration {
        @Bean("converterAdapter")
        public DiscoveryServerConverter consulConverterAdapter() {
            return new ConsulServerConverter();
        }
    }

    @Configuration
    @ConditionalOnClass({NacosServer.class, NacosRegistrationCustomizer.class})
    static class FemasNacosConfiguration {
        @Bean("converterAdapter")
        public DiscoveryServerConverter nacosConverterAdapter() {
            return new NacosServerConverter();
        }
    }

    @Configuration
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
