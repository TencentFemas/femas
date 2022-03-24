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

package com.tencent.tsf.femas.springcloud.gateway.filter;

import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.header.AbstractRequestMetaUtils;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.governance.lane.LaneService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @Author leoziltong
 * @Date: 2021/12/28 15:55
 */
public class GatewayHeaderUtils extends AbstractRequestMetaUtils {

    private volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();

    private final ServerHttpRequest.Builder builder;

    private ServerHttpRequest serverHttpRequest;

    public GatewayHeaderUtils(ServerHttpRequest serverHttpRequest) {
        this.serverHttpRequest = serverHttpRequest;
        this.builder = serverHttpRequest.mutate();
    }

    @Override
    public void preprocess() {

    }

    @Override
    public void setRequestMeta(String name, String value) {
        builder.headers(new Consumer<HttpHeaders>() {
            @Override
            public void accept(HttpHeaders headers) {
                headers.add(name, value);
            }
        });
    }

    @Override
    public void setRequestMetas(Map<String, String> kvs) {
        super.setRequestMetas(kvs);
    }

    @Override
    public String getRequestMeta(String name) {
        return builder.build().getHeaders().getFirst(name);
    }

    @Override
    public Map<String, String> getPrefixRequestMetas(String prefix) {
        Map<String, String> result = new HashMap<>();
        Set<Map.Entry<String, List<String>>> headersSet = builder.build().getHeaders().entrySet();
        for (Map.Entry<String, List<String>> map: headersSet) {
            String key = map.getKey();
            if (key.startsWith(prefix)) {
                result.put(key, map.getValue().get(0));
            }
        }
        return result;
    }

    @Override
    public void getUniqueInfo() {
        // clean at client interceptor#fillTracingContext
        Context.getRpcInfo().put(contextConstant.getInterface(), serverHttpRequest.getURI().toString());
        Context.getRpcInfo().put(contextConstant.getRequestHttpMethod(), serverHttpRequest.getMethodValue());
    }
}
