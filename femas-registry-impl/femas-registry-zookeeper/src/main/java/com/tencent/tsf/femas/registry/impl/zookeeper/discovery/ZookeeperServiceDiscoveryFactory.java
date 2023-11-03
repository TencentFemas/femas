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
package com.tencent.tsf.femas.registry.impl.zookeeper.discovery;

import com.tencent.tsf.femas.common.RegistryEnum;
import com.tencent.tsf.femas.common.discovery.ServiceDiscoveryClient;
import com.tencent.tsf.femas.common.discovery.ServiceDiscoveryFactory;

import java.util.Map;

/**
 * @author huyuanxin
 */
public class ZookeeperServiceDiscoveryFactory implements ServiceDiscoveryFactory {

    @Override
    public ServiceDiscoveryClient getServiceDiscovery(Map<String, String> configMap) {
        return new ZookeeperServiceDiscoveryClient(configMap);
    }

    @Override
    public String getType() {
        return RegistryEnum.ZOOKEEPER.name();
    }
}
  