package com.tencent.tsf.femas.springcloud.discovery.starter.discovery.ribbon;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ServerList;
import com.tencent.tsf.femas.springcloud.discovery.starter.discovery.FemasDiscoveryProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.netflix.ribbon.PropertiesFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 描述：
 * 创建日期：2022年05月26 00:27:41
 *
 * @author gong zhao
 **/
@Configuration(proxyBeanMethods = false)
@ConditionalOnRibbonFemas
public class FemasRibbonClientConfiguration {

    @Autowired
    private PropertiesFactory propertiesFactory;

    @Bean
    @ConditionalOnMissingBean
    public ServerList<?> ribbonServerList(IClientConfig config,
                                          FemasDiscoveryProperties femasDiscoveryProperties) {
        if (this.propertiesFactory.isSet(ServerList.class, config.getClientName())) {
            ServerList serverList = this.propertiesFactory.get(ServerList.class, config,
                    config.getClientName());
            return serverList;
        }
        FemasServerList serverList = new FemasServerList(femasDiscoveryProperties);
        serverList.initWithNiwsConfig(config);
        return serverList;
    }

    @Bean
    @ConditionalOnMissingBean
    public FemasServerIntrospector femasServerIntrospector() {
        return new FemasServerIntrospector();
    }

}
