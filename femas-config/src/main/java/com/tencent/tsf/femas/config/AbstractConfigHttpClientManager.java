/*
 * Tencent is pleased to support the open source community by making Femas available.
 *
 * Copyright (C) 2021, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.tencent.tsf.femas.config;

import com.tencent.tsf.femas.common.entity.Service;

/**
 * <pre>
 * 文件名称：AbstractConfigHttpClientManager.java
 * 创建时间：Jul 31, 2021 9:12:22 PM
 * @author juanyinyang
 * 类说明：
 */
public abstract class AbstractConfigHttpClientManager {

    public static final String HTTP ="http";

    public abstract void reportApis(String namespaceId, String serviceName, String applicationVersion, String data);

    public abstract String fetchKVValue(String key, String namespaceId);

    public abstract void initNamespace(String registryAddress, String namespaceId);

    public abstract void reportEvent(Service service, String eventId, String data);

    public abstract String getType();

    public enum PollingType{
        http,grpc
    }

}
  