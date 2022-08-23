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

package com.tencent.tsf.femas.plugin.config.enums;

/**
 * <pre>
 * 文件名称：ConfigHandlerTypeEnum.java
 * 创建时间：Jul 30, 2021 4:22:56 PM
 * @author juanyinyang
 * 类说明：
 */
public enum ConfigHandlerTypeEnum {

    AUTH("authHandler"),
    CIRCUIT_BREAKER("circuitBreakerHandler"),
    LOAD_BALANCER("loadBalancerHandler"),
    RATE_LIMITER("rateLimiterHandler"),
    ROUTER("routerHandler"),
    LANE("laneHandler"),
    METRICS_EXPORTER("metricsExporter"),
    METRICS_TRANSFORMER("metricsTransformer");

    private String type;

    ConfigHandlerTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
  