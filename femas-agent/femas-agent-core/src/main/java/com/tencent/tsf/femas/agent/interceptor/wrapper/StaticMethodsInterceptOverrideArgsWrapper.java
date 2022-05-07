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
 *
 */

package com.tencent.tsf.femas.agent.interceptor.wrapper;


import com.tencent.tsf.femas.agent.classloader.InterceptorClassLoaderCache;
import com.tencent.tsf.femas.agent.interceptor.StaticMethodsAroundInterceptor;
import com.tencent.tsf.femas.agent.tools.AgentLogger;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.lang.reflect.Method;

/**
 * 拦截静态方法，并且origin调用的时候，使用修改过的参数作为入参
 *
 * @Author leoziltong@tencent.com
 */
public class StaticMethodsInterceptOverrideArgsWrapper {


    private static final AgentLogger LOG = AgentLogger.getLogger(StaticMethodsInterceptOverrideArgsWrapper.class);

    private String interceptorClassName;

    public StaticMethodsInterceptOverrideArgsWrapper(String interceptorClassName) {
        this.interceptorClassName = interceptorClassName;
    }

    /**
     * Intercept the target static method.
     *
     * @param clazz        target class
     * @param allArguments all method arguments
     * @param method       method description.
     * @param zuper        the origin call ref.
     * @return the return value of target static method.
     */
    @RuntimeType
    public Object intercept(@Origin Class<?> clazz, @AllArguments Object[] allArguments, @Origin Method method,
                            @Morph OverrideArgsCallable zuper) throws Throwable {
        StaticMethodsAroundInterceptor<InterceptResult> interceptor = InterceptorClassLoaderCache.load(interceptorClassName, clazz
                .getClassLoader());
        InterceptResult result = new InterceptResult();
        try {
            result = interceptor.beforeMethod(clazz, method, allArguments, method.getParameterTypes());
        } catch (Throwable t) {
            LOG.error("[femas-agent] error  class:" + clazz + " before method:" + method.getName() + "intercept failure", t);
        }
        Object ret = null;
        try {
            if (!result.isContinue()) {
                ret = result.ret();
            } else {
                ret = zuper.call(allArguments);
            }
        } catch (Throwable t) {
            try {
                interceptor.handleMethodException(clazz, method, allArguments, method.getParameterTypes(), t);
            } catch (Throwable t2) {
                LOG.error("[femas-agent] error  class:" + clazz + " handleMethodException:" + method.getName() + "intercept failure", t);
            }
            throw t;
        } finally {
            try {
                ret = interceptor.afterMethod(clazz, method, allArguments, method.getParameterTypes(), ret);
            } catch (Throwable t) {
                LOG.error("[femas-agent] error  class:" + clazz + " afterMethod:" + method.getName() + "intercept failure", t);
            }
        }
        return ret;
    }
}
