package com.tencent.tsf.femas.springcloud.gateway.config;


import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.discovery.NacosServiceDiscovery;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistryMetadata;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistryMetadataFactory;
import com.tencent.tsf.femas.springcloud.gateway.discovery.DiscoveryServerConverter;
import com.tencent.tsf.femas.springcloud.gateway.discovery.nacos.NacosServerConverter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.alibaba.cloud.nacos.discovery.NacosDiscoveryAutoConfiguration;


@Configuration
@AutoConfigureBefore(NacosDiscoveryAutoConfiguration.class)
public class FemasDiscoveryAutoConfiguration {

    @Configuration
    @ConditionalOnDiscoveryEnabled
    @ConditionalOnNacosDiscoveryEnabled
    static class FemasNacosConfiguration {

        private volatile AbstractServiceRegistryMetadata serviceRegistryMetadata = AbstractServiceRegistryMetadataFactory
                .getServiceRegistryMetadata();

        @Bean
        @Primary
        public NacosDiscoveryProperties nacosProperties() {
            NacosDiscoveryProperties nacosDiscoveryProperties = new NacosDiscoveryProperties();
            nacosDiscoveryProperties.getMetadata().putAll(serviceRegistryMetadata.getRegisterMetadataMap());
            return nacosDiscoveryProperties;
        }

        @Bean
        @Primary
        public NacosServiceDiscovery nacosServiceDiscovery(NacosDiscoveryProperties nacosDiscoveryProperties) {
            nacosDiscoveryProperties.getMetadata().putAll(serviceRegistryMetadata.getRegisterMetadataMap());
            return new NacosServiceDiscovery(nacosDiscoveryProperties);
        }

        @Bean("converterAdapter")
        public DiscoveryServerConverter nacosConverterAdapter() {
            return new NacosServerConverter();
        }
    }

    @Bean("registryUrl")
    public String registryUrl(NacosDiscoveryProperties nacosDiscoveryProperties) {
        return nacosDiscoveryProperties.getServerAddr();
    }

}
