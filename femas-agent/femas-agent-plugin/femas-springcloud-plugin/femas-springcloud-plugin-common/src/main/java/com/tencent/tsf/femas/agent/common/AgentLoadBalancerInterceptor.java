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
package com.tencent.tsf.femas.agent.common;

import com.tencent.tsf.femas.agent.common.discovery.ConsulServerConverter;
import com.tencent.tsf.femas.agent.common.discovery.EurekaServerConverter;
import com.tencent.tsf.femas.agent.common.discovery.NacosServerConverter;

import java.util.Arrays;
import java.util.List;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2022/4/8 11:48
 */
public abstract class AgentLoadBalancerInterceptor {

    protected static List<FemasRibbonRouteLoadbalancer> nacosLoadBalancerList;
    protected static List<FemasRibbonRouteLoadbalancer> eurekaLoadBalancerList;
    protected static List<FemasRibbonRouteLoadbalancer> consulLoadBalancerList;

    static {
        nacosLoadBalancerList = Arrays.asList(new FemasRibbonRouteLoadbalancer(new NacosServerConverter()));
        eurekaLoadBalancerList = Arrays.asList(new FemasRibbonRouteLoadbalancer(new EurekaServerConverter()));
        consulLoadBalancerList = Arrays.asList(new FemasRibbonRouteLoadbalancer(new ConsulServerConverter()));
    }
}
