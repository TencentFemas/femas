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
package com.tencent.tsf.femas.agent.feign.instrument;

import com.tencent.tsf.femas.agent.interceptor.Interceptor;
import com.tencent.tsf.femas.agent.tools.AgentLogger;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.governance.lane.LaneService;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2022/4/7 16:26
 */
public class FeignRequestHeaderInterceptor implements Interceptor {

    private volatile Context commonContext = ContextFactory.getContextInstance();

    /**
     * @param obj          feign.RequestTemplate
     * @param allArguments method args
     * @param zuper        callable
     * @param method       reflect method
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        try {
            feign.RequestTemplate template = (feign.RequestTemplate) allArguments[0];
            LaneService.headerPreprocess();
            for (Map.Entry<String, String> entry :
                    commonContext.getRequestMetaSerializeTags().entrySet()) {
                if (StringUtils.isNotEmpty(entry.getValue())) {
                    try {
                        template.header(entry.getKey(),
                                URLEncoder.encode(entry.getValue(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        AgentLogger.getLogger().severe("[UnsupportedEncodingException] name:" + entry.getKey() + ", value:" +
                                entry.getValue());
                        template.header(entry.getKey(), entry.getValue());
                    }
                }
            }
        } catch (Throwable throwable) {
            AgentLogger.getLogger().severe("HttpClientInterceptor," + AgentLogger.getStackTraceString(throwable));
        } finally {
        }
        return zuper.call();
    }
}
