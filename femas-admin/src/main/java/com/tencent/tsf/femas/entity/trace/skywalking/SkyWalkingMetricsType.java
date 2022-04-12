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

package com.tencent.tsf.femas.entity.trace.skywalking;

import com.tencent.tsf.femas.entity.trace.NodeMetricsQueryCondition;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/8/25 14:40
 */
public enum SkyWalkingMetricsType {

    service_cpm, service_sla, service_resp_time;


    public static SkyWalkingMetricsType getTypeByCondition(NodeMetricsQueryCondition.MetricsType type) {
        if (type.equals(NodeMetricsQueryCondition.MetricsType.REQUEST_VOLUME)) {
            return service_cpm;
        }
        if (type.equals(NodeMetricsQueryCondition.MetricsType.ERROR_RATE)) {
            return service_sla;
        }
        if (type.equals(NodeMetricsQueryCondition.MetricsType.AVG_TIME)) {
            return service_resp_time;
        }
        return null;
    }

}
