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
package com.tencent.tsf.femas.agent.interceptor;

import com.tencent.tsf.femas.agent.classloader.AgentClassLoader;
import com.tencent.tsf.femas.agent.classloader.ClassLoaderCache;
import com.tencent.tsf.femas.agent.tools.AgentLogger;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2022/3/29 15:55
 */
public class InterceptorWrapper {

    private String className;
    private Object classInterceptor = null;
    private Method interceptorMethod = null;

    public InterceptorWrapper(String className) {
        this.className = className;
    }

    @RuntimeType
    public Object intercept(@This Object obj,
                            @AllArguments Object[] allArguments,
                            @SuperCall Callable<?> zuper,
                            @Origin Method method) throws Throwable {
        initInterceptor();

        if (interceptorMethod == null) {
            interceptorMethod = findInterceptor();
        }

        if (interceptorMethod != null) {
            return interceptorMethod.invoke(classInterceptor, obj, allArguments, zuper, method);
        } else {
            return zuper.call();
        }
    }

    private void initInterceptor() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (classInterceptor == null) {
            try {
                AgentClassLoader agentClassLoader = ClassLoaderCache.getAgentClassLoader(Thread.currentThread().getContextClassLoader());
                classInterceptor = agentClassLoader.loadClass(this.className).newInstance();
            } catch (Throwable throwable) {
                AgentLogger.getLogger().severe("initInterceptor error " + AgentLogger.getStackTraceString(throwable));
            }
        }
    }

    public Method findInterceptor() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        try {
            initInterceptor();
            if (classInterceptor != null) {
                Method[] methods = classInterceptor.getClass().getMethods();
                for (Method method : methods) {
                    if (method.getName().equals("intercept")) {
                        return method;
                    }
                }
            }
        } catch (Throwable throwable) {
            AgentLogger.getLogger().severe("findInterceptor error " + AgentLogger.getStackTraceString(throwable));
        }
        return null;
    }
}
