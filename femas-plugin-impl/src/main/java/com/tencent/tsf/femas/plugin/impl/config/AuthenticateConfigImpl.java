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

package com.tencent.tsf.femas.plugin.impl.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.tencent.tsf.femas.plugin.config.PluginConfigImpl;
import com.tencent.tsf.femas.plugin.config.gov.AuthenticateConfig;
import com.tencent.tsf.femas.plugin.config.verify.DefaultValues;
import com.tencent.tsf.femas.plugin.impl.config.rule.auth.AuthRuleConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @Author leoziltong
 * @Date: 2021/6/2 20:50
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticateConfigImpl extends PluginConfigImpl implements AuthenticateConfig {

    @JsonProperty
    private String type;

    @JsonProperty
    private List<AuthRuleConfig> authRule;

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<AuthRuleConfig> getAuthRule() {
        return authRule;
    }

    public void setAuthRule(List<AuthRuleConfig> authRule) {
        this.authRule = authRule;
    }

    @Override
    public void verify() throws IllegalArgumentException {
    }

    @Override
    public void setDefault() {
        if (StringUtils.isBlank(type)) {
            type = DefaultValues.DEFAULT_AUTHENTICATE;
        }
    }

    @Override
    public String toString() {
        return "AuthenticateConfigImpl{" +
                "type='" + type + '\'' +
                "} " + super.toString();
    }
}
