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

package com.tencent.tsf.femas.entity.trace;

import com.tencent.tsf.femas.entity.trace.skywalking.Duration;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/8/6 16:57
 */
@Getter
@Setter
public class NodeMetricsQueryCondition {

    private MetricsType metricsName;
    private String serviceName;
    private Duration queryDuration;

    public NodeMetricsQueryCondition(MetricsType metricsName, String serviceName, Duration queryDuration) {
        this.metricsName = metricsName;
        this.serviceName = serviceName;
        this.queryDuration = queryDuration;
    }

    public NodeMetricsQueryCondition() {
    }

    public enum MetricsType {
        AVG_TIME, ERROR_RATE, REQUEST_VOLUME;
    }

}
