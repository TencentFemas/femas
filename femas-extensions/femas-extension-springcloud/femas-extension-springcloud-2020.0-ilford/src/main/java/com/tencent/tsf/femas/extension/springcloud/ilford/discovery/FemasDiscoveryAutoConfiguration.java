package com.tencent.tsf.femas.extension.springcloud.ilford.discovery;


import com.tencent.cloud.common.pojo.PolarisServiceInstance;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.consul.discovery.ConsulServiceInstance;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistrationCustomizer;
import org.springframework.cloud.kubernetes.commons.discovery.KubernetesServiceInstance;
import org.springframework.cloud.kubernetes.fabric8.Fabric8AutoConfiguration;
import org.springframework.cloud.kubernetes.fabric8.discovery.ConditionalOnKubernetesDiscoveryEnabled;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.alibaba.cloud.nacos.NacosServiceInstance;
import com.alibaba.cloud.nacos.registry.NacosRegistrationCustomizer;

import com.tencent.tsf.femas.extension.springcloud.ilford.discovery.consul.ConsulServerConverter;
import com.tencent.tsf.femas.extension.springcloud.ilford.discovery.kubernetes.FemasKubernetesDiscoveryClient;
import com.tencent.tsf.femas.extension.springcloud.ilford.discovery.kubernetes.KubernetesServerConverter;
import com.tencent.tsf.femas.extension.springcloud.ilford.discovery.loadbalancer.DiscoveryServerConverter;
import com.tencent.tsf.femas.extension.springcloud.ilford.discovery.nacos.NacosServerConverter;
import com.tencent.tsf.femas.extension.springcloud.ilford.discovery.polaris.PolarisServerConverter;
import com.tencent.tsf.femas.common.kubernetes.KubernetesClientServicesFunction;
import com.tencent.tsf.femas.common.kubernetes.KubernetesDiscoveryProperties;
import com.tencent.tsf.femas.common.kubernetes.ServicePortSecureResolver;

import io.fabric8.kubernetes.client.KubernetesClient;

@Configuration
public class FemasDiscoveryAutoConfiguration {

    @Configuration
    @ConditionalOnClass({ConsulServiceInstance.class, ConsulRegistrationCustomizer.class})
    static class FemasConsulConfiguration {
        @Bean("converterAdapter")
        public DiscoveryServerConverter consulConverterAdapter() {
            return new ConsulServerConverter();
        }
    }

    @Configuration
    @ConditionalOnClass({NacosServiceInstance.class, NacosRegistrationCustomizer.class})
    static class FemasNacosConfiguration {
        @Bean("converterAdapter")
        public DiscoveryServerConverter nacosConverterAdapter() {
            return new NacosServerConverter();
        }
    }
    
    @Configuration
    @ConditionalOnClass({PolarisServiceInstance.class})
    static class FemasPolarisConfiguration {
        @Bean("converterAdapter")
        public DiscoveryServerConverter polarisConverterAdapter() {
            return new PolarisServerConverter();
        }
    }

    @Configuration
    @ConditionalOnClass({KubernetesServiceInstance.class})
    static class FemasKubernetesConfiguration {
        @Bean("converterAdapter")
        public DiscoveryServerConverter kubernetesConverterAdapter() {
            return new KubernetesServerConverter();
        }
    }

    @ConditionalOnClass({Fabric8AutoConfiguration.class, KubernetesServiceInstance.class, KubernetesClient.class,
            KubernetesDiscoveryProperties.class, KubernetesClientServicesFunction.class,
            FemasKubernetesDiscoveryClient.class})
    @ConditionalOnKubernetesDiscoveryEnabled
    public static class KubernetesDiscoveryClientConfiguration {
        @Bean("kubernetesDiscoveryClient")
        @ConditionalOnMissingBean
        @Primary
        public DiscoveryClient kubernetesDiscoveryClient(KubernetesClient client,
                                                         KubernetesDiscoveryProperties properties,
                                                         KubernetesClientServicesFunction kubernetesClientServicesFunction) {
            return new FemasKubernetesDiscoveryClient(client, properties, kubernetesClientServicesFunction,
                    new ServicePortSecureResolver(properties));
        }
    }
}
