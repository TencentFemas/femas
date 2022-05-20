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
import com.tencent.tsf.femas.agent.interceptor.ConstructorInterceptor;
import com.tencent.tsf.femas.agent.tools.AbstractAgentLogger;
import com.tencent.tsf.femas.agent.tools.AgentLogger;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

/**
 * 构造方法的拦截
 *
 * @Author leoziltong@tencent.com
 */
public class ConstructorInterceptorWrapper {
    private static final AbstractAgentLogger LOG = AgentLogger.getLogger(ConstructorInterceptorWrapper.class);

    private ConstructorInterceptor interceptor;

    public ConstructorInterceptorWrapper(String constructorInterceptorClassName, ClassLoader classLoader) throws InterceptorWrapperException {
        try {
            interceptor = InterceptorClassLoaderCache.load(constructorInterceptorClassName, classLoader);
        } catch (Throwable t) {
            LOG.error("[femas-agent] create constructorInterceptorClassName:" + constructorInterceptorClassName + "failed.", t);
            throw new InterceptorWrapperException("[femas-agent] create constructorInterceptorClassName:" + constructorInterceptorClassName + "failed.", t);
        }
    }

    /**
     * Intercept the target constructor
     *
     * @param obj          target class instance.
     * @param allArguments all constructor arguments
     */
    @RuntimeType
    public void intercept(@This Object obj, @AllArguments Object[] allArguments) {
        try {
            interceptor.afterConstructorInvocation(allArguments);
        } catch (Throwable t) {
            LOG.error("ConstructorInterceptorWrapper failure.", t);
        }

    }
}
