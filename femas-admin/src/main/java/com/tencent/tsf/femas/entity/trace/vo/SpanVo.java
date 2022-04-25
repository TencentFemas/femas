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
import com.tencent.tsf.femas.entity.trace.skywalking.KeyValue;
import com.tencent.tsf.femas.entity.trace.skywalking.LogEntity;
import com.tencent.tsf.femas.entity.trace.skywalking.Ref;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/8/20 15:16
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class SpanVo {

    private String traceId;

    private String segmentId;

    private int spanId;

    private int parentSpanId;
    private List<Ref> refs;

    private String serviceCode;

    private String namespaceId;

//    private String namespaceName;

    private String serviceInstanceName;

    private long startTime;

    private long endTime;

    private long duration;

    private String endpointName;

    private String type;

    private String peer;

    private String localIp;

    private String component;

    private Boolean isError;

    private String layer;

    private List<KeyValue> tags;

    private List<LogEntity> logs;

    private Boolean isRoot;

    private String segmentSpanId;

    private String segmentParentSpanId;

    public SpanVo() {
        this.refs = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.logs = new ArrayList<>();
    }

    public void verify() {
        if (StringUtils.isNotEmpty(this.serviceInstanceName)) {
            String[] strings = serviceInstanceName.split("@");
            if (strings.length >= 1) {
                this.localIp = strings[1];
            }
        }
    }
}
