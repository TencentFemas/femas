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
 * The static method's interceptor interface. Any plugin, which wants to intercept static methods, must implement this
 * interface.
 */
public interface StaticMethodsAroundInterceptor<T> {
    /**
     * called before target method invocation.
     */
    T beforeMethod(Class clazz, Method method, Object[] allArguments, Class<?>[] parameterTypes) throws Throwable;

    /**
     * called after target method invocation
     */
    default Object afterMethod(Class clazz, Method method, Object[] allArguments, Class<?>[] parameterTypes, Object ret) throws Throwable {
        return null;
    }

    /**
     * post context
     * 这里适用于需要处理一些上下文的情况
     *
     * @param clazz
     * @param method
     * @param allArguments
     * @param parameterTypes
     * @param ret
     * @return
     * @throws Throwable
     */
    default Object afterMethodWithContext(Class clazz, Method method, Object[] allArguments, Class<?>[] parameterTypes, Object ret) throws Throwable {
        return null;
    }

    /**
     * handler exception
     *
     * @param clazz
     * @param method
     * @param allArguments
     * @param parameterTypes
     * @param t
     */
    default void handleMethodException(Class clazz, Method method, Object[] allArguments, Class<?>[] parameterTypes,
                                       Throwable t) {

    }

    /**
     * verify result
     *
     * @param clazz
     * @param method
     * @param allArguments
     * @param parameterTypes
     * @param t
     * @param r
     */
    default void handleMethodExceptionWithResult(Class clazz, Method method, Object[] allArguments, Class<?>[] parameterTypes,
                                                 Throwable t, T r) {
    }
}
