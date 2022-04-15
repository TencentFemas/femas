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

import java.util.List;

/**
 * 用于组装各种配套的插件组合，比如帮助用户配套spring cloud G版所需插件
 * TODO
 *
 * @Author leoziltong@tencent.com
 * @Date: 2022/4/14 11:09
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MechaRuntimeConfiguration implements Configuration{

    @JsonProperty
    private List<GlobalInterceptPluginConfig> interceptors;

    @Override
    public void verify() throws IllegalArgumentException {

    }

    @Override
    public void setDefault() {

    }

    public void setInterceptors(List<GlobalInterceptPluginConfig> interceptors) {
        this.interceptors = interceptors;
    }

    @Override
    public List<GlobalInterceptPluginConfig> getInterceptors() {
        return interceptors;
    }

    @Override
    public boolean hasEmpty() {
        return false;
    }
}
