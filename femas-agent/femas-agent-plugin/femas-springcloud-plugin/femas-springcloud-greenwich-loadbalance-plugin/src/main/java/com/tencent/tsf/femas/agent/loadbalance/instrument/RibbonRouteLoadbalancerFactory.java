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
package com.tencent.tsf.femas.agent.loadbalance.instrument;

import com.tencent.tsf.femas.agent.common.discovery.ConsulServerConverter;
import com.tencent.tsf.femas.agent.common.discovery.EurekaServerConverter;
import com.tencent.tsf.femas.agent.common.discovery.NacosServerConverter;
import com.tencent.tsf.femas.agent.tools.AbstractAgentLogger;
import com.tencent.tsf.femas.agent.tools.AgentLogger;
import com.tencent.tsf.femas.api.ExtensionManager;
import com.tencent.tsf.femas.common.context.Context;
import org.apache.commons.collections.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.tencent.tsf.femas.common.RegistryEnum.*;
import static com.tencent.tsf.femas.common.context.ContextConstant.AGENT_REGISTER_TYPE_KEY;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2022/4/8 11:48
 */
public class RibbonRouteLoadbalancerFactory {

    private static final AbstractAgentLogger LOG = AgentLogger.getLogger(RibbonRouteLoadbalancerFactory.class);

    private final static Context commonContext = ExtensionManager.getExtensionLayer().getCommonContext();

    protected static Map<String, List<FemasRibbonRouteLoadbalancer>> loadBalancerList = new ConcurrentHashMap<>();

    protected static List<FemasRibbonRouteLoadbalancer> getLoadBalancerList() {
        String registerType = commonContext.getSystemTag(AGENT_REGISTER_TYPE_KEY);
        List<FemasRibbonRouteLoadbalancer> list = loadBalancerList.get(registerType);
        if (CollectionUtils.isEmpty(list)) {
            if (NACOS.getAlias().equalsIgnoreCase(registerType)) {
                list = Arrays.asList(new FemasRibbonRouteLoadbalancer(new NacosServerConverter()));
                loadBalancerList.put(NACOS.getAlias(), list);
            }
            if (CONSUL.getAlias().equalsIgnoreCase(registerType)) {
                list = Arrays.asList(new FemasRibbonRouteLoadbalancer(new ConsulServerConverter()));
                loadBalancerList.put(CONSUL.getAlias(), list);
            }
            if (EUREKA.getAlias().equalsIgnoreCase(registerType)) {
                list = Arrays.asList(new FemasRibbonRouteLoadbalancer(new EurekaServerConverter()));
                loadBalancerList.put(EUREKA.getAlias(), list);
            }
        }
        if (CollectionUtils.isEmpty(list)) {
            LOG.warn("[femas-agent] get Ribbon Route Loadbalancer failed,undefined register type");
        }
        return list;
    }

}
