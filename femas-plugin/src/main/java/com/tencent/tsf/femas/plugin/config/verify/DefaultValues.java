/*
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

package com.tencent.tsf.femas.plugin.config.verify;

import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;

/**
 * 默认值定义
 *
 * @author andrewshan
 * @author leoziltong
 * @date 2019/8/20
 */
public interface DefaultValues {

    ContextConstant contextConstant = ContextFactory.getContextConstantInstance();

    /**
     * 是否打开熔断能力，默认true
     */
    boolean DEFAULT_CIRCUIT_BREAKER_ENABLE = true;


    /**
     * 默认的服务端连接器插件
     */
    String DEFAULT_SERVER_CONNECTOR = contextConstant.getDefaultServerConnectorClient();


    /**
     * 默认负载均衡器
     */
    String DEFAULT_LOADBALANCER = contextConstant.getDefaultLoadBalancer();

    String DEFAULT_AUTHENTICATE = contextConstant.getDefaultAuth();
    String DEFAULT_METRICS = contextConstant.getDefaultMetrics();
    String DEFAULT_METRICS_EXPORTER = contextConstant.getDefaultMetricsExporter();
    String DEFAULT_METRICS_EXPORTER_ADDR = contextConstant.getDefaultMetricsExporterAddr();
    String DEFAULT_RATE_LIMIT = contextConstant.getDefaultRateLimiter();
    String DEFAULT_CIRCUIT_BREAKER = contextConstant.getDefaultCircuitBreaker();

    String DEFAULT_METRICS_TRANSFORMER = contextConstant.getDefaultMetricsTransformer();

    String DEFAULT_LANE = contextConstant.getDefaultLane();

    /**
     * 默认权重值
     */
    int DEFAULT_WEIGHT = 100;

    /**
     * 默认最大重试次数
     */
    int DEFAULT_MAX_RETRY_TIMES = 10;

}
