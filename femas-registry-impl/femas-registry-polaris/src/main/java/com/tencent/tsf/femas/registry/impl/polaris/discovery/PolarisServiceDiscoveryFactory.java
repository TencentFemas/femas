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
package com.tencent.tsf.femas.registry.impl.polaris.discovery;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tencent.tsf.femas.common.RegistryEnum;
import com.tencent.tsf.femas.common.discovery.ServiceDiscoveryClient;
import com.tencent.tsf.femas.common.discovery.ServiceDiscoveryFactory;

/**
* <pre>  
* 文件名称：PolarisServiceDiscoveryFactory.java  
* 创建时间：Dec 29, 2021 2:45:55 PM   
* @author juanyinyang  
* 类说明：  
*/
public class PolarisServiceDiscoveryFactory implements ServiceDiscoveryFactory {

    private static Map<String, ServiceDiscoveryClient> clientMap = new ConcurrentHashMap<>();

    @Override
    public ServiceDiscoveryClient getServiceDiscovery(Map<String, String> configMap) {
        String key = getKey(configMap);
        if (!clientMap.containsKey(key)) {
            ServiceDiscoveryClient client = new PolarisServiceDiscoveryClient(configMap);
            clientMap.putIfAbsent(key, client);
        }
        return clientMap.get(key);
    }

    @Override
    public String getType() {
        return RegistryEnum.POLARIS.name();
    }
}
  