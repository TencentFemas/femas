package com.tencent.tsf.femas.extension.springcloud.common.discovery;


import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.registry.NacosRegistrationCustomizer;
import com.tencent.tsf.femas.extension.springcloud.common.discovery.consul.FemasConsulRegistrationCustomizer;
import com.tencent.tsf.femas.extension.springcloud.common.discovery.nacos.FemasNacosRegistrationCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.consul.ConsulProperties;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistrationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class FemasDiscoveryCommonAutoConfiguration {

    @Configuration
    @ConditionalOnClass(ConsulRegistrationCustomizer.class)
    static class FemasConsulCommonConfiguration {

        @Bean
        // 需要晚于 extension layer init，因此需要 dependson 控制
        @DependsOn("femasGovernanceAutoConfiguration")
        public FemasConsulRegistrationCustomizer femasConsulRegistrationCustomizer() {
            return new FemasConsulRegistrationCustomizer();
        }

        @Bean("registryUrl")
        public String registryUrl(ConsulProperties consulProperties) {
            return consulProperties.getHost() + ":" + consulProperties.getPort();
        }
    }

    @Configuration
    @ConditionalOnClass({NacosRegistrationCustomizer.class})
    static class FemasNacosCommonConfiguration {

        @Bean
        // 需要晚于 extension layer init，因此需要 dependson 控制
        @DependsOn("femasGovernanceAutoConfiguration")
        public FemasNacosRegistrationCustomizer femasNacosRegistrationCustomizer() {
            return new FemasNacosRegistrationCustomizer();
        }

        @Bean("registryUrl")
        public String registryUrl(NacosDiscoveryProperties nacosDiscoveryProperties) {
            return nacosDiscoveryProperties.getServerAddr();
        }
    }
}
