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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/4/30 17:36
 */
@Component
public class OpenApiFactory {

    private final NacosRegistryOpenApi nacosRegistryOpenApi;
    private final EurekaRegistryOpenApi eurekaRegistryOpenApi;
    private final ConsulRegistryOpenApi consulRegistryOpenApi;
    private final KubernetesFabricRegistryOpenApi k8sRegistryOpenApi;
    private final PolarisRegistryOpenApi polarisRegistryOpenApi;

    private final Map<String, RegistryOpenApiInterface> registryOpenApiInterfaceMapCache = new ConcurrentHashMap<>();

    public OpenApiFactory(NacosRegistryOpenApi nacosRegistryOpenApi, EurekaRegistryOpenApi eurekaRegistryOpenApi, ConsulRegistryOpenApi consulRegistryOpenApi, KubernetesFabricRegistryOpenApi k8sRegistryOpenApi, PolarisRegistryOpenApi polarisRegistryOpenApi) {
        this.nacosRegistryOpenApi = nacosRegistryOpenApi;
        this.eurekaRegistryOpenApi = eurekaRegistryOpenApi;
        this.consulRegistryOpenApi = consulRegistryOpenApi;
        this.k8sRegistryOpenApi = k8sRegistryOpenApi;
        this.polarisRegistryOpenApi = polarisRegistryOpenApi;
        registryOpenApiInterfaceMapCache.put("CONSUL", consulRegistryOpenApi);
        registryOpenApiInterfaceMapCache.put("NACOS", nacosRegistryOpenApi);
        registryOpenApiInterfaceMapCache.put("EUREKA", eurekaRegistryOpenApi);
        registryOpenApiInterfaceMapCache.put("K8S", k8sRegistryOpenApi);
        registryOpenApiInterfaceMapCache.put("POLARIS", polarisRegistryOpenApi);
    }

    public RegistryOpenApiInterface select(String type) {
        return registryOpenApiInterfaceMapCache.get(type.toUpperCase());
    }

}
