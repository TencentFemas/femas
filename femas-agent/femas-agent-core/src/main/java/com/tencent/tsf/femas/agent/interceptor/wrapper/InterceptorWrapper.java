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
package com.tencent.tsf.femas.agent.interceptor.wrapper;

import com.tencent.tsf.femas.agent.classloader.AgentClassLoader;
import com.tencent.tsf.femas.agent.classloader.InterceptorClassLoaderCache;
import com.tencent.tsf.femas.agent.tools.AgentLogger;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * 有了Around interceptor 为什么还需要这个Interceptor
 * <p>
 * 这里需要解释下，某些特殊情况下，Around方式存在一些局限性，before after exception各个方法都需要有上下文关联处理的话，
 * 很难在割裂的各个方法中协调处理，容易出错，干脆就合并到一起，简单直观，能解决问题，所以衍生出了这个类，这个类目前只用来处理实例方法，
 * 这个Feature在agent的premain入口体现的
 *
 * @Author leoziltong@tencent.com
 * @Date: 2022/3/29 15:55
 */
public class InterceptorWrapper {

    private String className;
    private Object classInterceptor = null;
    private Method interceptorMethod = null;
    private ClassLoader classLoader;

    public InterceptorWrapper(String className, ClassLoader classLoader) {
        this.className = className;
        this.classLoader = classLoader;
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
                classInterceptor = InterceptorClassLoaderCache.load(this.className, classLoader);
            } catch (Throwable throwable) {
                AgentLogger.getLogger().severe("[femas-agent] initInterceptor error " + className + AgentLogger.getStackTraceString(throwable));
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
            AgentLogger.getLogger().severe("[femas-agent] findInterceptor error " + AgentLogger.getStackTraceString(throwable));
        }
        return null;
    }
}
