/**
 * Tencent is pleased to support the open source community by making Polaris available.
 * <p>
 * Copyright (C) 2019 THL A29 Limited, a Tencent company. All rights reserved.
 * <p>
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://opensource.org/licenses/BSD-3-Clause
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.tencent.tsf.femas.plugin.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tencent.tsf.femas.plugin.config.Configuration;
import com.tencent.tsf.femas.plugin.config.gov.*;
import com.tencent.tsf.femas.plugin.impl.config.*;


/**
 * SDK全量配置对象
 *
 * @author andrewshan
 * @author leoziltong
 * @date 2019/8/20
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigurationImpl implements Configuration {

    @JsonProperty
    private GlobalConfigImpl global;

    @JsonProperty
    private ServiceRouterConfigImpl serviceRouter;

    @JsonProperty
    private LoadBalanceConfigImpl loadbalancer;

    @JsonProperty
    private CircuitBreakerConfigImpl circuitBreaker;

    @JsonProperty
    private RateLimitConfigImpl rateLimit;

    @JsonProperty
    private AuthenticateConfigImpl authenticate;

    @JsonProperty
    private LaneConfigImpl lane;

    @JsonProperty
    private MetricsConfigImpl metrics;

    @JsonProperty
    private MetricsExporterConfigImpl metricsExporter;

    @JsonProperty
    private MetricsTransformerConfigImpl metricsTransformer;

    @Override
    public boolean hasEmpty() {
        if (null == global || null == serviceRouter || null == loadbalancer ||
                null == circuitBreaker || null == rateLimit || null == authenticate || lane == null || metrics == null || metricsExporter == null || metricsTransformer == null) {
            return true;
        }
        return false;
    }

    @Override
    public GlobalConfigImpl getGlobal() {
        return global;
    }

    public void setGlobal(GlobalConfigImpl global) {
        this.global = global;
    }

    @Override
    public void verify() {
        global.verify();
        serviceRouter.verify();
        loadbalancer.verify();
        circuitBreaker.verify();
        rateLimit.verify();
        authenticate.verify();
        lane.verify();
        metrics.verify();
        metricsExporter.verify();
        metricsTransformer.verify();
    }

    @Override
    public void setDefault() {
        if (null == global) {
            global = new GlobalConfigImpl();
        }
        if (null == serviceRouter) {
            serviceRouter = new ServiceRouterConfigImpl();
        }
        if (null == loadbalancer) {
            loadbalancer = new LoadBalanceConfigImpl();
        }
        if (null == circuitBreaker) {
            circuitBreaker = new CircuitBreakerConfigImpl();
        }
        if (null == rateLimit) {
            rateLimit = new RateLimitConfigImpl();
        }
        if (null == authenticate) {
            authenticate = new AuthenticateConfigImpl();
        }
        if (null == lane) {
            lane = new LaneConfigImpl();
        }
        if (null == metrics) {
            metrics = new MetricsConfigImpl();
        }
        if (null == metricsExporter) {
            metricsExporter = new MetricsExporterConfigImpl();
        }
        if (null == metricsTransformer) {
            metricsTransformer = new MetricsTransformerConfigImpl();
        }
        global.setDefault();
        serviceRouter.setDefault();
        loadbalancer.setDefault();
        circuitBreaker.setDefault();
        rateLimit.setDefault();
        authenticate.setDefault();
        lane.setDefault();
        metrics.setDefault();
        metricsExporter.setDefault();
        metricsTransformer.setDefault();
    }


    @Override
    public String toString() {
        return "ConfigurationImpl{" +
                "global=" + global +
                '}';
    }

    @Override
    public ServiceRouterConfig getServiceRouter() {
        return serviceRouter;
    }

    @Override
    public LoadBalanceConfig getLoadbalancer() {
        return loadbalancer;
    }

    @Override
    public CircuitBreakerConfig getCircuitBreaker() {
        return circuitBreaker;
    }

    @Override
    public RateLimitConfigImpl getRateLimit() {
        return rateLimit;
    }

    @Override
    public AuthenticateConfig getAuthenticate() {
        return authenticate;
    }

    @Override
    public LaneConfigImpl getLane() {
        return lane;
    }

    public MetricsConfig getMetrics() {
        return metrics;
    }

    public MetricsExporterConfig getMetricsExporter() {
        return metricsExporter;
    }

    public MetricsTransformerConfig getMetricsTransformer() {
        return metricsTransformer;
    }


}
