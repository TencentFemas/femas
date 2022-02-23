/*
 * Tencent is pleased to support the open source community by making Femas available.
 *
 * Copyright (C) 2021, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.tencent.tsf.femas.extension.springcloud.ilford.discovery.loadbalancer.config;

import com.tencent.tsf.femas.extension.springcloud.ilford.discovery.loadbalancer.DiscoveryServerConverter;
import com.tencent.tsf.femas.extension.springcloud.ilford.discovery.loadbalancer.FemasRouteLoadBalancer;
import com.tencent.tsf.femas.extension.springcloud.ilford.discovery.loadbalancer.FemasServiceFilterLoadBalancer;
import com.tencent.tsf.femas.extension.springcloud.ilford.discovery.loadbalancer.FemasServiceFilterRouteLoadBalancer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;

import java.util.List;

/**
 * <pre>
 * 文件名称：FemasLoadBalancerClientConfiguration.java
 * 创建时间：Oct 30, 2021 9:51:49 PM
 * @author juanyinyang
 * 类说明：
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnDiscoveryEnabled
public class FemasLoadBalancerClientConfiguration {

    @Bean
    public FemasServiceFilterLoadBalancer femasServiceFilterRouteLoadBalancer() {
        return new FemasServiceFilterRouteLoadBalancer();
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn("converterAdapter")
    public FemasRouteLoadBalancer femasRouteLoadBalancer(DiscoveryServerConverter converter, Environment environment,
            LoadBalancerClientFactory loadBalancerClientFactory,
            @Lazy @Autowired(required = false) List<FemasServiceFilterLoadBalancer> loadBalancerList) {
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new FemasRouteLoadBalancer(converter, loadBalancerList,
                loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), name);
    }

}
  