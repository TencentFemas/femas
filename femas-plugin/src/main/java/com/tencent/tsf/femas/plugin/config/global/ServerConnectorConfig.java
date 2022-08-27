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

package com.tencent.tsf.femas.plugin.config.global;

import com.tencent.tsf.femas.plugin.config.PluginConfig;
import com.tencent.tsf.femas.plugin.config.verify.Verifier;

import java.util.List;

/**
 * 与规则平台的连接配置
 *
 * @author andrewshan
 * @date 2019/8/20
 */
public interface ServerConnectorConfig extends PluginConfig, Verifier {

    /**
     * global.serverConnector.addresses
     * 远端server地址，格式为<host>:<port>
     *
     * @return 地址列表
     */
    List<String> getAddresses();

    /**
     * global.serverConnector.protocol
     * 与server对接的协议，默认GRPC
     *
     * @return 协议名称
     */
    String getProtocol();

    /**
     * global.serverConnector.connectTimeout
     * 与server的连接超时时间
     *
     * @return long, 毫秒
     */
    long getConnectTimeoutMs();

    /**
     * global.serverConnector.clientRequestQueueSize
     * 新请求的队列BUFFER容量
     *
     * @return buffer长度
     */
    int getRequestQueueSize();

    /**
     * global.serverConnector.connectionIdleTimeout
     * 空闲连接过期时间
     *
     * @return long, 毫秒
     */
    long getConnectionIdleTimeoutMs();

}
