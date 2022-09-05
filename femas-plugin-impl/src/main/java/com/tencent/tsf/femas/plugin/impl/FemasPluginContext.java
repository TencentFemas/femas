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

package com.tencent.tsf.femas.plugin.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.governance.auth.IAuthentication;
import com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService;
import com.tencent.tsf.femas.governance.connector.server.ServerConnectorManager;
import com.tencent.tsf.femas.governance.lane.LaneFilter;
import com.tencent.tsf.femas.governance.loadbalance.Loadbalancer;
import com.tencent.tsf.femas.governance.metrics.IMeterRegistry;
import com.tencent.tsf.femas.governance.metrics.MetricsExporter;
import com.tencent.tsf.femas.governance.metrics.MetricsTransformer;
import com.tencent.tsf.femas.governance.ratelimit.RateLimiter;
import com.tencent.tsf.femas.governance.route.Router;
import com.tencent.tsf.femas.plugin.DefaultConfigurablePluginHolder;
import com.tencent.tsf.femas.plugin.PluginDefinitionReader;
import com.tencent.tsf.femas.plugin.config.Configuration;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 根据用户配置获取Femas的各个治理插件
 * <p>
 * 根据配置文件，选取插件，以及加载插件所需配置
 *
 * @Author leoziltong
 * @Date: 2021/6/2 20:38
 */
public class FemasPluginContext {

    private static final Logger logger = LoggerFactory.getLogger(FemasPluginContext.class);

    private static final PluginDefinitionReader reader = new PluginDefinitionReader();

    private static final ObjectMapper mapper = new ObjectMapper();

    private static List<Router> serviceRouters = new CopyOnWriteArrayList<>();

    private static Loadbalancer loadBalancer;

    private static ServerConnectorManager serverConnectorManager;

    private static List<IMeterRegistry> meterRegistries = new CopyOnWriteArrayList<>();

    private static MetricsExporter metricsExporter;

    private static MetricsTransformer metricsTransformer;

    private static List<ICircuitBreakerService> circuitBreakers = new CopyOnWriteArrayList<>();

    private static IAuthentication authentication;

    private static RateLimiter rateLimiter;

    private static LaneFilter laneFilter;

    private static Configuration configuration;


    static {
        try {
            TreeTraversingParser treeTraversingParser = new TreeTraversingParser(reader.getJsonNode());
            configuration = mapper.readValue(treeTraversingParser, ConfigurationImpl.class);
        } catch (Exception e) {
            logger.error("load yaml configuration failed");
        }
        if (configuration == null) {
            configuration = new ConfigurationImpl();
        }
        configuration.setDefault();
        configuration.verify();
        serverConnectorManager =
                (ServerConnectorManager) DefaultConfigurablePluginHolder.getSDKContext()
                        .getPlugin(SPIPluginType.SERVER_CONNECTOR.getInterfaces(), configuration.getGlobal().getServerConnector().getProtocol().toUpperCase());
        loadBalancer = (Loadbalancer) DefaultConfigurablePluginHolder.getSDKContext()
                .getPlugin(SPIPluginType.LOAD_BALANCER.getInterfaces(), configuration.getLoadbalancer().getType());
        rateLimiter = (RateLimiter) DefaultConfigurablePluginHolder.getSDKContext()
                .getPlugin(SPIPluginType.RATE_LIMITER.getInterfaces(), configuration.getRateLimit().getType());
        authentication = (IAuthentication) DefaultConfigurablePluginHolder.getSDKContext()
                .getPlugin(SPIPluginType.AUTH.getInterfaces(), configuration.getAuthenticate().getType());
        metricsExporter = (MetricsExporter) DefaultConfigurablePluginHolder.getSDKContext()
                .getPlugin(SPIPluginType.METRICS_EXPORTER.getInterfaces(),
                        configuration.getMetricsExporter().getType());
        metricsTransformer = (MetricsTransformer) DefaultConfigurablePluginHolder.getSDKContext()
                .getPlugin(SPIPluginType.METRICS_TRANSFORMER.getInterfaces(),
                        configuration.getMetricsTransformer().getType());
        laneFilter = (LaneFilter) DefaultConfigurablePluginHolder.getSDKContext()
                .getPlugin(SPIPluginType.LANE.getInterfaces(), configuration.getLane().getType());

        boolean enable = configuration.getCircuitBreaker().isEnable();
        List<String> cbChain = configuration.getCircuitBreaker().getChain();
        if (enable && CollectionUtils.isNotEmpty(cbChain)) {
            for (String cbName : cbChain) {
                circuitBreakers.add((ICircuitBreakerService) DefaultConfigurablePluginHolder.getSDKContext()
                        .getPlugin(SPIPluginType.CIRCUIT_BREAKER.getInterfaces(), cbName));
            }
        }

        List<String> routerChain = configuration.getServiceRouter().getChain();
        if (CollectionUtils.isNotEmpty(routerChain)) {
            for (String routerName : routerChain) {
                if (StringUtils.isNotEmpty(routerName)) {
                    serviceRouters.add((Router) DefaultConfigurablePluginHolder.getSDKContext()
                            .getPlugin(SPIPluginType.SERVICE_ROUTER.getInterfaces(), routerName));
                }
            }
        }

        List<String> metricChan = configuration.getMetrics().getChain();
        if (CollectionUtils.isNotEmpty(metricChan)) {
            for (String metric : metricChan) {
                meterRegistries.add((IMeterRegistry) DefaultConfigurablePluginHolder.getSDKContext()
                        .getPlugin(SPIPluginType.METRICS.getInterfaces(), metric));
            }
        }

    }

    public static PluginDefinitionReader getReader() {
        return reader;
    }

    public static ObjectMapper getMapper() {
        return mapper;
    }

    public static List<Router> getServiceRouters() {
        return serviceRouters;
    }

    public static Loadbalancer getLoadBalancer() {
        return loadBalancer;
    }

    public static List<ICircuitBreakerService> getCircuitBreakers() {
        return circuitBreakers;
    }

    public static IAuthentication getAuthentication() {
        return authentication;
    }

    public static RateLimiter getRateLimiter() {
        return rateLimiter;
    }

    public static Configuration getConfiguration() {
        return configuration;
    }

    public static List<IMeterRegistry> getMeterRegistry() {
        return meterRegistries;
    }

    public static MetricsExporter getMetricsExporter() {
        return metricsExporter;
    }

    public static MetricsTransformer getMetricsTransformer() {
        return metricsTransformer;
    }

    public static ServerConnectorManager getServerConnectorManager() {
        return serverConnectorManager;
    }

    public static LaneFilter getLaneFilter() {
        return laneFilter;
    }
}
