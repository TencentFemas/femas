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

package com.tencent.tsf.femas.service.registry;

import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.entity.namespace.Namespace;
import com.tencent.tsf.femas.entity.param.RegistryInstanceParam;
import com.tencent.tsf.femas.entity.registry.ClusterServer;
import com.tencent.tsf.femas.entity.registry.RegistryConfig;
import com.tencent.tsf.femas.entity.registry.RegistryPageService;
import java.util.List;

/**
 * @Author leoziltong
 * @Date: 2021/4/29 21:58
 * @Version 1.0
 */
public interface RegistryOpenApiInterface {

    List<ClusterServer> clusterServers(RegistryConfig config);

    ServerMetrics fetchServerMetrics(RegistryConfig config);

    RegistryPageService fetchServices(RegistryConfig config, RegistryInstanceParam registryInstanceParam);

    List<ServiceInstance> fetchServiceInstances(RegistryConfig config, RegistryInstanceParam registryInstanceParam);

    boolean createNamespace(RegistryConfig config, Namespace namespace);

    default boolean healthCheck(RegistryConfig config) {
        return true;
    }

    boolean deleteNamespace(RegistryConfig config, Namespace namespace);

    boolean modifyNamespace(RegistryConfig config, Namespace namespace);
}
