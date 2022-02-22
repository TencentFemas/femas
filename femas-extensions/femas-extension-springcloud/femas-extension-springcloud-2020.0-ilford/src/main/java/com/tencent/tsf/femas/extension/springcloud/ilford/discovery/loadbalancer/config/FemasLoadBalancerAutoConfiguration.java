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

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalancerProperties;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerBeanPostProcessorAutoConfiguration;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerClientAutoConfiguration;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.context.annotation.Configuration;

/**
 * <pre>
 * 文件名称：FemasLoadBalancerAutoConfiguration.java
 * 创建时间：Oct 30, 2021 9:39:14 PM
 * @author juanyinyang
 * 类说明：
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(LoadBalancerProperties.class)
@AutoConfigureBefore({ReactorLoadBalancerClientAutoConfiguration.class,
        LoadBalancerBeanPostProcessorAutoConfiguration.class})
@ConditionalOnProperty(value = "femas.discovery.loadbalancer.enabled", havingValue = "true", matchIfMissing = true)
@LoadBalancerClients(defaultConfiguration = {FemasLoadBalancerClientConfiguration.class})
public class FemasLoadBalancerAutoConfiguration {

}
  