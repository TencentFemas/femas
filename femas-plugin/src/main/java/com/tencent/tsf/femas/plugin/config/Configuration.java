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

package com.tencent.tsf.femas.plugin.config;

import com.tencent.tsf.femas.plugin.config.global.GlobalConfig;
import com.tencent.tsf.femas.plugin.config.gov.*;
import com.tencent.tsf.femas.plugin.config.verify.Verifier;
/**
 * SDK全量配置对象
 *
 * @author andrewshan
 * @date 2019/8/20
 */
public interface Configuration extends Verifier {

    /**
     * services.global前缀开头的所有配置项
     *
     * @return GlobalConfig
     */
    GlobalConfig getGlobal();

    /**
     * services.femas.serviceRouter前缀开头的所有配置
     *
     * @return ServiceRouterConfig
     */
    ServiceRouterConfig getServiceRouter();

    /**
     * services.femas.loadbalancer前缀开头的所有配置
     *
     * @return LoadBalanceConfig
     */
    LoadBalanceConfig getLoadbalancer();

    /**
     * services.femas.circuitBreaker前缀开头的所有配置
     *
     * @return CircuitBreakerConfig
     */
    CircuitBreakerConfig getCircuitBreaker();

    /**
     * services.femas.rateLimit前缀开头的所有配置
     *
     * @return RateLimitConfig
     */
    RateLimitConfig getRateLimit();

    AuthenticateConfig getAuthenticate();

    LaneConfig getLane();

    MetricsConfig getMetrics();

    MetricsExporterConfig getMetricsExporter();

    MetricsTransformerConfig getMetricsTransformer();

    boolean hasEmpty();
}
