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

import com.tencent.tsf.femas.agent.exception.InterceptorInvalidConfigException;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author leoziltong
 * @Date: 2022/7/12 11:43
 * @Version 1.0
 */
public enum MechaRuntimeModule {

    GREENWICH("greenwich", "spring-cloud-g"),
    DUBBO2("dubbo2", "dubbo-2x"),
    DUBBO3("dubbo3", "dubbo-3x"),
    SC2020("sc2020", "spring-cloud-2020");
    private String tag;
    private String module;

    public static String getPluginModuleByTag(String tag) {
        if (StringUtils.isEmpty(tag)) {
            throw new InterceptorInvalidConfigException("interceptor module not found exception,set correct starting args please");
        }
        for (MechaRuntimeModule module : MechaRuntimeModule.values()) {
            if (module.getTag().equalsIgnoreCase(tag)) {
                return module.getModule();
            }
        }
        throw new InterceptorInvalidConfigException("interceptor module not found exception,set correct starting args please");
    }

    public static String getPluginModuleByBuzzTag(String buzzTag) {
        if (StringUtils.isEmpty(buzzTag)) {
            throw new InterceptorInvalidConfigException("interceptor module not found exception,set correct starting args please");
        }
        for (MechaRuntimeModule module : MechaRuntimeModule.values()) {
            if (buzzTag.toLowerCase().contains(module.getTag())) {
                return module.getModule();
            }
        }
        throw new InterceptorInvalidConfigException("interceptor module not found exception,set correct starting args please");
    }

    MechaRuntimeModule(String tag, String module) {
        this.tag = tag;
        this.module = module;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
