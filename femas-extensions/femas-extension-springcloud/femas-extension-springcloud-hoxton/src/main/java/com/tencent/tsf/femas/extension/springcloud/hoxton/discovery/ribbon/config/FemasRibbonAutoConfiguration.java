package com.tencent.tsf.femas.extension.springcloud.hoxton.discovery.ribbon.config;

import com.tencent.tsf.femas.extension.springcloud.hoxton.discovery.ribbon.DiscoveryServerConverter;
import com.tencent.tsf.femas.extension.springcloud.hoxton.instrumentation.route.FemasRibbonRouteLoadbalancer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * @author juanyinyang
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Configuration
@EnableConfigurationProperties
@ConditionalOnBean(SpringClientFactory.class)
@ConditionalOnProperty(value = "femas.discovery.ribbon.enabled", matchIfMissing = true)
@AutoConfigureAfter(RibbonAutoConfiguration.class)
@RibbonClients(defaultConfiguration = FemasRibbonClientConfiguration.class)
public class FemasRibbonAutoConfiguration {

    @Bean
    @DependsOn("converterAdapter")
    public FemasRibbonRouteLoadbalancer femasRibbonRouteLoadbalancer(DiscoveryServerConverter converter) {
        return new FemasRibbonRouteLoadbalancer(converter);
    }
}
