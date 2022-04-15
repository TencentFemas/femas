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
package com.tencent.tsf.femas.agent.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2022/4/12 15:24
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InterceptPlugin {

    /**
     * 类名
     */
    @JsonProperty
    private String className;
    /**
     * 方法名
     */
    @JsonProperty
    private String methodName;
    /**
     * 匹配类型  MatcherType
     */
    @JsonProperty
    private String matcherType;
    /**
     * 字节码拦截interceptor
     */
    @JsonProperty
    private String interceptorClass;
    /**
     * 方法参数长度
     */
    @JsonProperty
    private Integer takesArguments;

    public InterceptPlugin() {

    }

    public InterceptPlugin(String className, String methodName, String matcherType, String interceptorClass, Integer takesArguments) {
        this.className = className;
        this.methodName = methodName;
        this.matcherType = matcherType;
        this.interceptorClass = interceptorClass;
        this.takesArguments = takesArguments;
    }

    public String getMatcherType() {
        return matcherType;
    }

    public void setMatcherType(String matcherType) {
        this.matcherType = matcherType;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getInterceptorClass() {
        return interceptorClass;
    }

    public void setInterceptorClass(String interceptorClass) {
        this.interceptorClass = interceptorClass;
    }

    public Integer getTakesArguments() {
        return takesArguments;
    }

    public void setTakesArguments(Integer takesArguments) {
        this.takesArguments = takesArguments;
    }
}