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
import com.tencent.tsf.femas.governance.plugin.config.global.ServerConnectorConfig;
import java.util.List;

/**
 * 与规则平台的连接配置
 * 这里的抽象应该再往上一层，不应该在gov层
 *
 * @author andrewshan
 * @author leo
 * @date 2019/8/20
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerConnectorConfigImpl extends PluginConfigImpl implements ServerConnectorConfig {

    private static List<String> DEFAULT_ADDRESS;

    static {
        DEFAULT_ADDRESS = getDefaultAddress();
    }

    @JsonProperty
    private List<String> addresses;
    @JsonProperty
    private String protocol;
    @JsonProperty
    private String connectTimeout;
    private long connectTimeoutMs;
    @JsonProperty
    private int requestQueueSize;
    @JsonProperty
    private String connectionIdleTimeout;
    private long connectionIdleTimeoutMs;

    private static List<String> getDefaultAddress() {
        return null;
    }

    @Override
    public List<String> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(String connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @Override
    public long getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(long connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    @Override
    public int getRequestQueueSize() {
        return requestQueueSize;
    }

    public void setRequestQueueSize(int requestQueueSize) {
        this.requestQueueSize = requestQueueSize;
    }


    @Override
    public long getConnectionIdleTimeoutMs() {
        return connectionIdleTimeoutMs;
    }

    public void setConnectionIdleTimeoutMs(long connectionIdleTimeoutMs) {
        this.connectionIdleTimeoutMs = connectionIdleTimeoutMs;
    }

    public String getConnectionIdleTimeout() {
        return connectionIdleTimeout;
    }

    public void setConnectionIdleTimeout(String connectionIdleTimeout) {
        this.connectionIdleTimeout = connectionIdleTimeout;
    }

    @Override
    public void verify() throws IllegalArgumentException {

        if (requestQueueSize < 0) {
            throw new IllegalArgumentException(
                    String.format("global.serverConnector.requestQueueSize %d is invalid", requestQueueSize));
        }
    }

    @Override
    public void setDefault() {

    }

    @Override
    public String toString() {
        return null;
    }

}
