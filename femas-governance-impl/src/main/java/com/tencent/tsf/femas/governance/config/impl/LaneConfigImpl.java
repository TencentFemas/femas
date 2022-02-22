/**
 * Tencent is pleased to support the open source community by making Polaris available.
 * <p>
 * Copyright (C) 2019 THL A29 Limited, a Tencent company. All rights reserved.
 * <p>
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://opensource.org/licenses/BSD-3-Clause
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.tencent.tsf.femas.governance.config.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tencent.tsf.femas.governance.plugin.config.PluginConfigImpl;
import com.tencent.tsf.femas.governance.plugin.config.gov.LaneConfig;
import com.tencent.tsf.femas.governance.plugin.config.verify.DefaultValues;
import org.apache.commons.lang3.StringUtils;


/**
 * @author leozilong
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LaneConfigImpl extends PluginConfigImpl implements LaneConfig {

    @JsonProperty
    private String type;

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void verify() throws IllegalArgumentException {
    }

    @Override
    public void setDefault() {
        if (StringUtils.isBlank(type)) {
            type = DefaultValues.DEFAULT_LANE;
        }
    }

    @Override
    public String toString() {
        return "LaneConfigImpl{" +
                "type='" + type + '\'' +
                "} " + super.toString();
    }
}
