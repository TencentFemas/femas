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
import com.tencent.tsf.femas.agent.tools.AbstractAgentLogger;
import com.tencent.tsf.femas.agent.tools.AgentLogger;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * 拦截实例方法
 *
 * @Author leoziltong@tencent.com
 */
public class InstanceMethodsInterceptorWrapper {

    private static final AbstractAgentLogger LOG = AgentLogger.getLogger(InstanceMethodsInterceptorWrapper.class);

    private InstanceMethodsAroundInterceptor<InterceptResult> interceptor;

    public InstanceMethodsInterceptorWrapper(String interceptorClassName, ClassLoader classLoader) {
        try {
            interceptor = InterceptorClassLoaderCache.load(interceptorClassName, classLoader);
        } catch (Throwable t) {
            LOG.error("[femas-agent] create InstanceMethodsAroundInterceptor:" + interceptorClassName + "failed.", t);
            throw new InterceptorWrapperException("[femas-agent] create InstanceMethodsAroundInterceptor:" + interceptorClassName + "failed.", t);
        }
    }

    /**
     * Intercept the target instance method.
     *
     * @param obj          target class.
     * @param allArguments all method arguments
     * @param method       method description.
     * @param zuper        the origin call ref.
     */
    @RuntimeType
    public Object intercept(@This Object obj, @AllArguments Object[] allArguments, @SuperCall Callable<?> zuper,
                            @Origin Method method) throws Throwable {
        InterceptResult result = new InterceptResult();
        try {
            result = interceptor.beforeMethod(method, allArguments, method.getParameterTypes());
        } catch (Throwable t) {
            LOG.error("[femas-agent] error class:" + obj.getClass() + " before method:" + method.getName() + "intercept failure", t);
        }
        Object ret = null;
        try {
            if (!result.isContinue()) {
                ret = result.ret();
            } else {
                ret = zuper.call();
            }
        } catch (Throwable t) {
            try {
                interceptor.handleMethodException(method, allArguments, method.getParameterTypes(), t);
            } catch (Throwable t2) {
                LOG.error("[femas-agent] error  class:" + obj.getClass() + " handleMethodException:" + method.getName() + "intercept failure", t);
            }
            throw t;
        } finally {
            try {
                ret = interceptor.afterMethod(method, allArguments, method.getParameterTypes(), ret);
            } catch (Throwable t) {
                LOG.error("[femas-agent] error  class:" + obj.getClass() + " afterMethod:" + method.getName() + "intercept failure", t);
            }
        }
        return ret;
    }
}
