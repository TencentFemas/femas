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
package com.tencent.tsf.femas.agent.nacos.instrument;

import com.tencent.tsf.femas.agent.common.AgentLoadBalancerInterceptor;
import com.tencent.tsf.femas.agent.interceptor.InstanceMethodsAroundInterceptor;
import com.tencent.tsf.femas.agent.interceptor.Interceptor;
import com.netflix.loadbalancer.Server;
import com.tencent.tsf.femas.agent.interceptor.wrapper.InterceptResult;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2022/4/8 11:35
 */
public class ZoneAwareLoadBalancerInterceptor extends AgentLoadBalancerInterceptor implements InstanceMethodsAroundInterceptor<InterceptResult> {


    @Override
    public InterceptResult beforeMethod(Method method, Object[] allArguments, Class<?>[] argumentsTypes) throws Throwable {
        nacosLoadBalancerList.forEach(femasZoneAwareLoadBalancer -> femasZoneAwareLoadBalancer.beforeChooseServer(allArguments[0]));
        return new InterceptResult();
    }

    @Override
    public Object afterMethod(Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        nacosLoadBalancerList
                .forEach(femasZoneAwareLoadBalancer -> femasZoneAwareLoadBalancer.afterChooseServer((com.netflix.loadbalancer.Server) ret, allArguments[0]));
        return ret;
    }

    @Override
    public void handleMethodException(Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {

    }

//    /**
//     * @param obj          Object key
//     * @param allArguments method args
//     * @param zuper        callable
//     * @param method       reflect method
//     * @return
//     * @throws Throwable
//     */
//    @Override
//    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
//        try {
//            nacosLoadBalancerList.forEach(femasZoneAwareLoadBalancer -> femasZoneAwareLoadBalancer.beforeChooseServer(obj));
//            Server server = (Server) zuper.call();
//            nacosLoadBalancerList
//                    .forEach(femasZoneAwareLoadBalancer -> femasZoneAwareLoadBalancer.afterChooseServer(server, obj));
//            return server;
//        } catch (Exception e) {
//        }
//        return zuper.call();
//    }
}