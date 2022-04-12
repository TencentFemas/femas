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

package com.tencent.tsf.femas.service.trace;

import static com.tencent.tsf.femas.constant.AdminConstants.TRACE_SERVER_BACKEND;

import com.tencent.tsf.femas.entity.trace.NodeMetricsQueryCondition;
import com.tencent.tsf.femas.entity.trace.TopologyQueryCondition;
import com.tencent.tsf.femas.entity.trace.TraceDetailQueryCondition;
import com.tencent.tsf.femas.entity.trace.TraceQueryCondition;
import com.tencent.tsf.femas.entity.trace.vo.TopologyVo;
import com.tencent.tsf.femas.entity.trace.vo.TraceBriefVo;
import com.tencent.tsf.femas.entity.trace.vo.TraceVo;

/**
 * @Author leoziltong
 * @Date: 2021/8/6 14:57
 * @Version 1.0
 */
public interface OpentracingServer {

    /**
     * 依赖拓扑图
     *
     * @param condition
     * @return
     */
    TopologyVo getServiceTopology(final TopologyQueryCondition condition);

    /**
     * trace 列表
     *
     * @param condition
     * @return
     */
    TraceBriefVo queryBasicTraces(final TraceQueryCondition condition);


    /**
     * 查询命名空间下的trace列表
     *
     * @param condition
     * @return
     */
    TraceBriefVo queryNamespaceBasicTraces(final TraceQueryCondition condition);


    /**
     * trace 详情
     *
     * @param condition
     * @return
     */
    TraceVo queryTrace(final TraceDetailQueryCondition condition);

    /**
     * 请求量
     *
     * @param condition
     * @return
     */
    Long queryRequestVolume(final NodeMetricsQueryCondition condition);

    /**
     * 错误率
     *
     * @param condition
     * @return
     */
    Double queryErrorRate(final NodeMetricsQueryCondition condition);

    /**
     * 平均时间
     *
     * @param condition
     * @return
     */
    Double queryAvgTime(final NodeMetricsQueryCondition condition);

    /**
     * server 地址
     *
     * @return
     */
    default String getServerAddr() {
        return System.getProperty(TRACE_SERVER_BACKEND);
    }

}
