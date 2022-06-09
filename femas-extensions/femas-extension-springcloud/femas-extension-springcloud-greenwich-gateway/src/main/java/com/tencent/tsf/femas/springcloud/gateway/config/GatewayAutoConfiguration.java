/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.tsf.femas.springcloud.gateway.config;

import com.tencent.tsf.femas.springcloud.gateway.discovery.DiscoveryServerConverter;
import com.tencent.tsf.femas.springcloud.gateway.filter.FemasGatewayGovernanceFilter;
import com.tencent.tsf.femas.springcloud.gateway.filter.FemasReactiveLoadBalancerClientFilter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.gateway.filter.LoadBalancerClientFilter;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayAutoConfiguration implements BeanPostProcessor, ApplicationContextAware {

    private ApplicationContext context;

    @Bean
    @ConditionalOnMissingBean
    public FemasGatewayGovernanceFilter femasGatewayFilter() {
        return new FemasGatewayGovernanceFilter();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if(bean instanceof LoadBalancerClientFilter || bean instanceof ReactiveLoadBalancerClientFilter){
            DiscoveryServerConverter converter = context.getBean(DiscoveryServerConverter.class);
            LoadBalancerClientFactory clientFactory= context.getBean(LoadBalancerClientFactory.class);
            LoadBalancerProperties properties =context.getBean(LoadBalancerProperties.class);
            return new FemasReactiveLoadBalancerClientFilter(converter, clientFactory, properties);
        }
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}
