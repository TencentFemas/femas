//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.tencent.tsf.femas.springcloud.gateway.config;

import com.tencent.tsf.femas.springcloud.gateway.discovery.DiscoveryServerConverter;
import com.tencent.tsf.femas.springcloud.gateway.filter.FemasGatewayFilter;
import com.tencent.tsf.femas.springcloud.gateway.filter.FemasReactiveLoadBalancerClientFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FemasGatewayFilter femasGatewayFilter() {
        return new FemasGatewayFilter();
    }

    @Bean
    @ConditionalOnMissingBean({FemasReactiveLoadBalancerClientFilter.class})
    public FemasReactiveLoadBalancerClientFilter reactiveLoadBalancerClientFilter(DiscoveryServerConverter converter, LoadBalancerClientFactory clientFactory, LoadBalancerProperties properties) {
        return new FemasReactiveLoadBalancerClientFilter(converter, clientFactory, properties);
    }
}
