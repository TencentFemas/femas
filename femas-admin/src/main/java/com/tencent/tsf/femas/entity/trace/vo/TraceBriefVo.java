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

package com.tencent.tsf.femas.entity.trace.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/8/20 15:10
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TraceBriefVo {

    @Setter
    private List<BasicTraceVo> traces;
    @Setter
    private int total;

    public TraceBriefVo(int total) {
        this.total = total;
    }

    public TraceBriefVo() {
        this.traces = new ArrayList<>();
    }

    public TraceBriefVo(List<BasicTraceVo> traces, int total) {
        this.traces = traces;
        this.total = total;
    }
}
