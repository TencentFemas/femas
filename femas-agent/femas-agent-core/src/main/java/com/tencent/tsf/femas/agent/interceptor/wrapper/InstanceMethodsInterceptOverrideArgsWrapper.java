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
import com.tencent.tsf.femas.agent.exception.InterceptorWrapperException;
import com.tencent.tsf.femas.agent.interceptor.InstanceMethodsAroundInterceptor;
import com.tencent.tsf.femas.agent.tools.AgentLogger;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;

public class InstanceMethodsInterceptOverrideArgsWrapper {

    private InstanceMethodsAroundInterceptor<InterceptResult> interceptor;

    public InstanceMethodsInterceptOverrideArgsWrapper(String interceptorClassName, ClassLoader classLoader) {
        try {
            interceptor = InterceptorClassLoaderCache.load(interceptorClassName, classLoader);
        } catch (Throwable t) {
            throw new InterceptorWrapperException("[femas-agent] create InstanceMethodsAroundInterceptor failed", t);
        }
    }

    /**
     * Intercept the target instance method.
     *
     * @param obj          target class instance.
     * @param allArguments all method arguments
     * @param method       method description.
     * @param zuper        OverrideArgsCallable
     * @return the return value of target instance method.
     */
    @RuntimeType
    public Object intercept(@This Object obj, @AllArguments Object[] allArguments, @Origin Method method,
                            @Morph OverrideArgsCallable zuper) throws Throwable {
        InterceptResult result = new InterceptResult();
        Object context = null;
        try {
            result = interceptor.beforeMethod(method, allArguments, method.getParameterTypes());
            context = result.getContext();
        } catch (Throwable t) {
            AgentLogger.getLogger().info("[femas-agent] error  class:" + obj.getClass() + " before method:" + method.getName() + "intercept failure");
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
                if (context != null) {
                    interceptor.handleMethodExceptionWithResult(method, allArguments, method.getParameterTypes(), t,result);
                }else{
                    interceptor.handleMethodException(method, allArguments, method.getParameterTypes(), t);
                }
            } catch (Throwable t2) {
                AgentLogger.getLogger().info("[femas-agent] error  class:" + obj.getClass() + " before method:" + method.getName() + "intercept failure");
            }
            throw t;
        } finally {
            try {
                if (context != null) {
                    ret = interceptor.afterMethodWithContext(method, allArguments, method.getParameterTypes(), ret, context);
                } else {
                    ret = interceptor.afterMethod(method, allArguments, method.getParameterTypes(), ret);
                }
            } catch (Throwable t) {
                AgentLogger.getLogger().info("[femas-agent] error  class:" + obj.getClass() + " before method:" + method.getName() + "intercept failure");
            }
        }
        return ret;
    }
}
