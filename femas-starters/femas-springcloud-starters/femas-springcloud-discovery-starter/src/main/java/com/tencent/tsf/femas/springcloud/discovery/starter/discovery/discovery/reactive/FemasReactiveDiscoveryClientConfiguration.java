package com.tencent.tsf.femas.springcloud.discovery.starter.discovery.discovery.reactive;

import com.tencent.tsf.femas.common.discovery.ServiceDiscoveryClient;
import com.tencent.tsf.femas.extension.springcloud.discovery.FemasDiscoveryAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.ConditionalOnReactiveDiscoveryEnabled;
import org.springframework.cloud.client.ReactiveCommonsClientAutoConfiguration;
import org.springframework.cloud.client.discovery.composite.reactive.ReactiveCompositeDiscoveryClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 描述：
 * 创建日期：2022年05月17 19:55:00
 *
 * @author gong zhao
 **/
@Configuration(
        proxyBeanMethods = false
)
@ConditionalOnReactiveDiscoveryEnabled
@AutoConfigureAfter({FemasDiscoveryAutoConfiguration.class, ReactiveCompositeDiscoveryClientAutoConfiguration.class})
@AutoConfigureBefore({ReactiveCommonsClientAutoConfiguration.class})
public class FemasReactiveDiscoveryClientConfiguration {

   @Bean
   @ConditionalOnMissingBean
   public FemasReactiveDiscoveryClient femasReactiveDiscoveryClient(
           ServiceDiscoveryClient serviceDiscoveryClient) {
      return new FemasReactiveDiscoveryClient(serviceDiscoveryClient);
   }
}
