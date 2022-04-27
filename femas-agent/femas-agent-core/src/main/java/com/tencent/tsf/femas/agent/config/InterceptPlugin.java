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
import com.tencent.tsf.femas.agent.exception.InterceptorWrapperException;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.commons.lang3.StringUtils;

import static net.bytebuddy.matcher.ElementMatchers.*;

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
     * 匹配类型  MatcherType 默认是精准匹配
     */
    @JsonProperty
    private String matcherType;

    /**
     * 匹配类型  MethodType 默认是实例方法
     */
    @JsonProperty
    private String methodType;
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

    /**
     * before重写之后，是否需要用修改之后的参数调用原始方法
     */
    @JsonProperty
    private Boolean overrideArgs;

    /**
     * true 使用com.tencent.tsf.femas.agent.interceptor.Interceptor方式拦截
     */
    @JsonProperty
    private Boolean originAround;

    public InterceptPlugin() {

    }

    public InterceptPlugin(String className, String methodName, String matcherType, String methodType, String interceptorClass, Integer takesArguments, Boolean overrideArgs, Boolean originAround) {
        this.className = className;
        this.methodName = methodName;
        this.matcherType = matcherType;
        this.methodType = methodType;
        this.interceptorClass = interceptorClass;
        this.takesArguments = takesArguments;
        this.overrideArgs = overrideArgs;
        this.originAround = originAround;
    }

    public String getMethodType() {
        return methodType;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }

    public Boolean getOverrideArgs() {
        return overrideArgs;
    }

    public void setOverrideArgs(Boolean overrideArgs) {
        this.overrideArgs = overrideArgs;
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

    public Boolean getOriginAround() {
        return originAround;
    }

    public void setOriginAround(Boolean originAround) {
        this.originAround = originAround;
    }

    public ElementMatcher<MethodDescription> getPluginMatcher() {
        ElementMatcher.Junction<MethodDescription> junction = null;
        if (MethodType.CONSTRUCTOR.getType().equalsIgnoreCase(methodType)) {
            if (takesArguments != null) {
                return takesArguments(takesArguments);
            } else {
                return any();
            }
        }
        if (StringUtils.isEmpty(matcherType) || MatcherType.EXACT_MATCH.getType().equalsIgnoreCase(matcherType)) {
            junction = named(methodName);
        }
        if (MatcherType.PREFIX.getType().equalsIgnoreCase(matcherType)) {
            junction = nameStartsWithIgnoreCase(methodName);
        }
        if (MatcherType.SUFFIX.getType().equalsIgnoreCase(matcherType)) {
            junction = nameEndsWithIgnoreCase(methodName);
        }
        if (MatcherType.CONTAIN.getType().equalsIgnoreCase(matcherType)) {
            junction = nameContainsIgnoreCase(methodName);
        }
        if (junction == null) {
            throw new InterceptorWrapperException("get Plugin Matcher failed,illegal plugin config...");
        }
        if (takesArguments != null) {
            junction.and(ElementMatchers.takesArguments(takesArguments));
        }
        return junction;
    }

}