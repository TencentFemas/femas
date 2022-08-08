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
package com.tencent.tsf.femas.agent.loadbalance.instrument;

import com.netflix.loadbalancer.Server;
import com.tencent.tsf.femas.agent.interceptor.InstanceMethodsAroundInterceptor;
import com.tencent.tsf.femas.agent.interceptor.wrapper.ConstructorInterceptorWrapper;
import com.tencent.tsf.femas.agent.interceptor.wrapper.InterceptResult;
import com.tencent.tsf.femas.agent.tools.AgentLogger;
import com.tencent.tsf.femas.api.ExtensionManager;
import com.tencent.tsf.femas.common.context.Context;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2022/4/8 15:31
 */
public class BaseLoadBalancerInterceptor implements InstanceMethodsAroundInterceptor<InterceptResult> {

    private static final AgentLogger LOG = AgentLogger.getLogger(BaseLoadBalancerInterceptor.class);

    @Override
    public InterceptResult beforeMethod(Method method, Object[] allArguments, Class<?>[] argumentsTypes) throws Throwable {
        return new InterceptResult();
    }

    @Override
    public Object afterMethod(Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        final List<Server>[] servers = new List[]{(List) ret};
        RibbonRouteLoadbalancerFactory.getLoadBalancerList().forEach(lb -> {
            servers[0] = lb.filterAllServer(servers[0]);
        });
        return servers[0];
    }

    @Override
    public void handleMethodException(Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        LOG.error("[femas-agent] BaseLoadBalancer  beforeMethod  Intercept error,", t);
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
//            final List<Server>[] servers = new List[]{(List) zuper.call()};
//            nacosLoadBalancerList.forEach(lb -> {
//                servers[0] = lb.filterAllServer(servers[0]);
//            });
//            return servers[0];
//        } catch (Exception e) {
//        }
//        return zuper.call();
//    }

}
