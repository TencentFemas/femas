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

package com.tencent.tsf.femas.endpoint.trace;

import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.endpoint.adaptor.AbstractBaseEndpoint;
import com.tencent.tsf.femas.entity.trace.TopologyQueryCondition;
import com.tencent.tsf.femas.entity.trace.TraceDetailQueryCondition;
import com.tencent.tsf.femas.entity.trace.TraceQueryCondition;
import com.tencent.tsf.femas.entity.trace.vo.TopologyVo;
import com.tencent.tsf.femas.entity.trace.vo.TraceBriefVo;
import com.tencent.tsf.femas.entity.trace.vo.TraceVo;
import com.tencent.tsf.femas.service.trace.OpentracingServer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/8/18 11:12
 */
@RestController
@RequestMapping("/atom/v1/trace")
@Api(value = "/atom/v1/trace", tags = "链路查询模块")
public class TraceEndpoint extends AbstractBaseEndpoint {

    private final OpentracingServer opentracingServer;

    public TraceEndpoint(OpentracingServer opentracingServer) {
        this.opentracingServer = opentracingServer;
    }

    @ApiOperation(value = "获取链路拓扑图", notes = "根据命名空间获取链路拓扑")
    @PostMapping("/describeServiceTopology")
    public Result<TopologyVo> describeServiceTopology(@RequestBody TopologyQueryCondition condition) {
        return executor.process(() -> {
            TopologyVo topology = opentracingServer.getServiceTopology(condition);
            return Result.successData(topology);
        });
    }

    @ApiOperation(value = "获取链路列表", notes = "获取链路列表")
    @PostMapping("/describeBasicTraces")
    public Result<TraceBriefVo> describeBasicTraces(@RequestBody TraceQueryCondition condition) {
        return executor.process(() -> {
            TraceBriefVo traceBrief = opentracingServer.queryNamespaceBasicTraces(condition);
            return Result.successData(traceBrief);
        });
    }

    @ApiOperation(value = "获取链路列表", notes = "获取链路列表")
    @PostMapping("/describeTrace")
    public Result<TraceVo> describeTrace(@RequestBody TraceDetailQueryCondition condition) {
        return executor.process(() -> {
            TraceVo trace = opentracingServer.queryTrace(condition);
            return Result.successData(trace);
        });
    }

}
