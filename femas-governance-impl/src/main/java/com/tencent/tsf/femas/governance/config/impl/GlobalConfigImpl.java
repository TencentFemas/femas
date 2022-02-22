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
import com.tencent.tsf.femas.governance.plugin.config.global.GlobalConfig;
import com.tencent.tsf.femas.governance.plugin.config.global.SystemConfig;

/**
 * 全局配置对象
 *
 * @author andrewshan
 * @date 2019/8/20
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GlobalConfigImpl implements GlobalConfig {

    @JsonProperty
    private SystemConfigImpl system;

    @JsonProperty
    private ServerConnectorConfigImpl serverConnector;

    @Override
    public SystemConfig getSystem() {
        return system;
    }

    @Override
    public ServerConnectorConfigImpl getServerConnector() {
        return serverConnector;
    }

    public void setServerConnector(ServerConnectorConfigImpl serverConnector) {
        this.serverConnector = serverConnector;
    }

    @Override
    public void verify() throws IllegalArgumentException {
        serverConnector.verify();
    }

    @Override
    public void setDefault() {
        if (null == system) {
            system = new SystemConfigImpl();
        }
        if (null == serverConnector) {
            serverConnector = new ServerConnectorConfigImpl();
        }
        system.setDefault();
        serverConnector.setDefault();
    }


    @Override
    public String toString() {
        return "GlobalConfigImpl{" +
                "system=" + system +
                ", serverConnector=" + serverConnector +
                '}';
    }

}
