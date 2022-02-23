package com.tencent.tsf.femas.extension.springcloud.discovery.ribbon;/*
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

import com.netflix.loadbalancer.Server;
import com.tencent.tsf.femas.common.entity.ServiceInstance;

import java.util.Map;

/**
 * 根据元数据获取重要信息
 *
 * @Author leoziltong
 * @Date: 2021/6/16 16:26
 */
public abstract class AbstractDiscoveryServerConverter implements DiscoveryServerConverter {

    @Override
    public Map<String, String> getServerMetadata(Server server) {
        return null;
    }

    @Override
    public String getNamespace() {
        return null;
    }

    @Override
    public String getServiceName() {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public ServiceInstance convert(Server server) {
        return null;
    }

    @Override
    public Server getOrigin(ServiceInstance serviceInstance) {
        return null;
    }
}
