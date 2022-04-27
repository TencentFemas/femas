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

import com.tencent.tsf.femas.agent.interceptor.ConstructorInterceptor;
import com.tencent.tsf.femas.agent.interceptor.StaticMethodsAroundInterceptor;
import com.tencent.tsf.femas.agent.interceptor.wrapper.InterceptResult;
import com.tencent.tsf.femas.agent.tools.AgentLogger;
import com.tencent.tsf.femas.api.CommonExtensionLayer;
import com.tencent.tsf.femas.api.ExtensionManager;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.governance.lane.LaneService;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2022/4/26 15:10
 */
public class FeignRequestInterceptor implements StaticMethodsAroundInterceptor<InterceptResult> {

    private volatile Context commonContext = ExtensionManager.getExtensionLayer().getCommonContext();

    @Override
    public InterceptResult beforeMethod(Class clazz, Method method, Object[] allArguments, Class<?>[] parameterTypes) throws Throwable {
        Map<String, Collection<String>> headers = (Map<String, Collection<String>>) allArguments[2];
        Map<String, Collection<String>> temp = new ConcurrentHashMap<>();
        temp.putAll(headers);
        LaneService.headerPreprocess();
        for (Map.Entry<String, String> entry :
                commonContext.getRequestMetaSerializeTags().entrySet()) {
            if (StringUtils.isNotEmpty(entry.getValue())) {
                try {
                    temp.put(entry.getKey(),
                            Arrays.asList(URLEncoder.encode(entry.getValue(), "UTF-8")));
                } catch (UnsupportedEncodingException e) {
                    AgentLogger.getLogger().severe("[UnsupportedEncodingException] name:" + entry.getKey() + ", value:" +
                            entry.getValue());
                    temp.put(entry.getKey(), Arrays.asList(entry.getValue()));
                }
            }
        }
        allArguments[2] = temp;
        return new InterceptResult();
    }

    @Override
    public Object afterMethod(Class clazz, Method method, Object[] allArguments, Class<?>[] parameterTypes, Object ret) throws Throwable {
        return ret;
    }

    @Override
    public void handleMethodException(Class clazz, Method method, Object[] allArguments, Class<?>[] parameterTypes, Throwable t) {

    }

//    public void afterConstructorInvocation(Object[] allArguments) throws Throwable {
//        Map<String, Collection<String>> headers = (Map<String, Collection<String>>) allArguments[2];
//        Map<String, Collection<String>> temp = new ConcurrentHashMap<>();
//        temp.putAll(headers);
//        LaneService.headerPreprocess();
//        for (Map.Entry<String, String> entry :
//                commonContext.getRequestMetaSerializeTags().entrySet()) {
//            if (StringUtils.isNotEmpty(entry.getValue())) {
//                try {
//                    temp.put(entry.getKey(),
//                            Arrays.asList(URLEncoder.encode(entry.getValue(), "UTF-8")));
//                } catch (UnsupportedEncodingException e) {
//                    AgentLogger.getLogger().severe("[UnsupportedEncodingException] name:" + entry.getKey() + ", value:" +
//                            entry.getValue());
//                    temp.put(entry.getKey(), Arrays.asList(entry.getValue()));
//                }
//            }
//        }
//        headers = temp;
//    }
}
