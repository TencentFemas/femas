package com.tencent.tsf.femas.springcloud.discovery.starter.discovery.ribbon;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.annotation.Configuration;

/**
 * 描述：
 * 创建日期：2022年05月26 00:22:47
 *
 * @author gong zhao
 **/
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@ConditionalOnBean(SpringClientFactory.class)
@ConditionalOnRibbonFemas
@ConditionalOnProperty(
        value = {"spring.cloud.femas.discovery.enabled"},
        matchIfMissing = true
)
@AutoConfigureAfter(RibbonAutoConfiguration.class)
@RibbonClients(defaultConfiguration = FemasRibbonClientConfiguration.class)
public class RibbonFemasAutoConfiguration {
}
