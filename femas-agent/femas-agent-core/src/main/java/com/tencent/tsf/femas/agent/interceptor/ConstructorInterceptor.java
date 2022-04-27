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

/**
 * 构造方法interceptor，构造方法字节码只能追加在方法后面（java规范）
 *
 * @Author leoziltong@tencent.com
 */
public interface ConstructorInterceptor {
    /**
     * Called after the origin constructor invocation.
     */
    void afterConstructorInvocation(Object[] allArguments) throws Throwable;
}
