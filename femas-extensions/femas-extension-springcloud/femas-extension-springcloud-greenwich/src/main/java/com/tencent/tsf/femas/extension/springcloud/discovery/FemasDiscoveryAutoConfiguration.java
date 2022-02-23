package com.tencent.tsf.femas.extension.springcloud.discovery;

import com.alibaba.cloud.nacos.registry.NacosRegistrationCustomizer;
import com.alibaba.cloud.nacos.ribbon.NacosServer;

import com.tencent.tsf.femas.extension.springcloud.discovery.consul.ConsulServerConverter;

import com.tencent.tsf.femas.extension.springcloud.discovery.nacos.NacosServerConverter;
import com.tencent.tsf.femas.extension.springcloud.discovery.ribbon.DiscoveryServerConverter;

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

}
