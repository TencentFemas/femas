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


package com.tencent.tsf.femas.agent.interceptor;

import java.lang.reflect.Method;

/**
 * A interceptor, which intercept method's invocation. The target methods will be defined in {@link
 */
public interface InstanceMethodsAroundInterceptor<T> {
    /**
     * called before target method invocation.
     */
    T beforeMethod(Method method, Object[] allArguments, Class<?>[] argumentsTypes) throws Throwable;

    /**
     * called after target method invocation.
     */
    default Object afterMethod(Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                               Object ret) throws Throwable {
        return null;
    }

    /**
     * post context
     * 这里适用于需要处理一些上下文的情况
     */
    default Object afterMethodWithContext(Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                                          Object ret, Object context) throws Throwable {
        return null;
    }

    /**
     * handler exception
     */
    default void handleMethodException(Method method, Object[] allArguments,
                                       Class<?>[] argumentsTypes, Throwable t) throws Throwable {

    }

    /**
     * verify result
     */
    default void handleMethodExceptionWithResult(Method method, Object[] allArguments,
                                                 Class<?>[] argumentsTypes, Throwable t, T r) throws Throwable {
    }
}
