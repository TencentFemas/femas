package com.tencent.tsf.femas.extension.springcloud.hoxton.discovery.ribbon.config;


import com.ecwid.consul.v1.ConsulClient;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.*;
import com.tencent.tsf.femas.extension.springcloud.hoxton.discovery.consul.FemasConsulServerList;
import com.tencent.tsf.femas.extension.springcloud.hoxton.discovery.ribbon.FemasRibbonLoadbalancer;
import com.tencent.tsf.femas.extension.springcloud.hoxton.discovery.ribbon.FemasServiceFilterLoadBalancer;
import com.tencent.tsf.femas.extension.springcloud.hoxton.discovery.ribbon.LongPollingServerListUpdater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.ConsulServer;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistrationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.List;

@Configuration
public class FemasRibbonClientConfiguration {

    @Bean
    public ILoadBalancer femasRibbonLoadBalancer(IClientConfig config, ServerList<Server> serverList,
            ServerListFilter<Server> serverListFilter, IRule rule,
            IPing ping, ServerListUpdater serverListUpdater,
            @Lazy @Autowired(required = false) List<FemasServiceFilterLoadBalancer> loadBalancerList) {
        return new FemasRibbonLoadbalancer<>(config, rule, ping, serverList, serverListFilter,
                serverListUpdater, loadBalancerList);
    }

    @ConditionalOnClass({ConsulServer.class, ConsulRegistrationCustomizer.class})
    static class FemasConsulRibbonClientConfiguration {

        @Autowired
        private ConsulClient client;

        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE)
        @ConditionalOnProperty(name = "femas.discovery.watch.enabled", matchIfMissing = true)
        public ServerList<?> ribbonServerList(IClientConfig config,
                ConsulDiscoveryProperties properties) {
            FemasConsulServerList serverList = new FemasConsulServerList(this.client, properties);
            serverList.initWithNiwsConfig(config);
            return serverList;
        }

        @Bean
        @ConditionalOnProperty(name = "femas.discovery.watch.enabled", matchIfMissing = true)
        public ServerListUpdater ribbonServerListUpdater(IClientConfig config) {
            return new LongPollingServerListUpdater(config);
        }
    }

}