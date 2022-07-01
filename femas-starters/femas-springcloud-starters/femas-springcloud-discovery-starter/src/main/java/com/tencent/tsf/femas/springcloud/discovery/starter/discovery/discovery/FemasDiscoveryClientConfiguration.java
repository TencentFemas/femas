package com.tencent.tsf.femas.springcloud.discovery.starter.discovery.discovery;

import com.tencent.tsf.femas.springcloud.discovery.starter.discovery.FemasDiscoveryProperties;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.cloud.client.CommonsClientAutoConfiguration;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 描述：
 * 创建日期：2022年05月17 16:46:58
 *
 * @author gong zhao
 **/
@Configuration(proxyBeanMethods = false)
@ConditionalOnDiscoveryEnabled
@AutoConfigureBefore({SimpleDiscoveryClientAutoConfiguration.class,
        CommonsClientAutoConfiguration.class})
public class FemasDiscoveryClientConfiguration {

    @Bean
    public DiscoveryClient femasDiscoveryClient(FemasServiceDiscovery femasServiceDiscovery, FemasDiscoveryProperties femasDiscoveryProperties) {
        return new FemasDiscoveryClient(femasServiceDiscovery, femasDiscoveryProperties);
    }

    /**
     * 从这里通过配置参数，构造一个合适的 serviceDiscoveryClient
     */
}
