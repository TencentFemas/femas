package com.tencent.tsf.femas.springcloud.discovery.starter.discovery.discovery;

import com.tencent.tsf.femas.springcloud.discovery.starter.discovery.ConditionalOnFemasDiscoveryEnabled;
import com.tencent.tsf.femas.springcloud.discovery.starter.discovery.FemasDiscoveryProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
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
}
