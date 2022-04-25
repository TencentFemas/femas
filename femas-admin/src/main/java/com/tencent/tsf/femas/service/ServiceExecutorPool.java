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
package com.tencent.tsf.femas.service;

import com.tencent.tsf.femas.enums.ServiceInvokeEnum;
import com.tencent.tsf.femas.service.dcfg.ConfigService;
import com.tencent.tsf.femas.service.dcfg.ConfigVersionService;
import com.tencent.tsf.femas.service.namespace.NamespaceMangerService;
import com.tencent.tsf.femas.service.registry.RegistryManagerService;
import com.tencent.tsf.femas.service.rule.AuthService;
import com.tencent.tsf.femas.service.rule.BreakerService;
import com.tencent.tsf.femas.service.rule.LimitService;
import com.tencent.tsf.femas.service.rule.RouteService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/4/29 11:02
 */
@Component
public class ServiceExecutorPool implements InitializingBean {

    private final Map<ServiceInvokeEnum, ServiceExecutor> executorMap = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        executorMap.putIfAbsent(ServiceInvokeEnum.REGISTRY_MANAGER,registryManagerService);
        executorMap.putIfAbsent(ServiceInvokeEnum.NAMESPACE_MANGER,namespaceMangerService);
        executorMap.putIfAbsent(ServiceInvokeEnum.DCFG_CONFIG,configService);
        executorMap.putIfAbsent(ServiceInvokeEnum.DCFG_CONFIG_VERSION,configVersionService);
        executorMap.putIfAbsent(ServiceInvokeEnum.SERVICE_BREAK,breakerService);
        executorMap.putIfAbsent(ServiceInvokeEnum.SERVICE_AUTH,authService);
        executorMap.putIfAbsent(ServiceInvokeEnum.SERVICE_LIMIT,limitService);
        executorMap.putIfAbsent(ServiceInvokeEnum.SERVICE_ROUTE,routeService);
    }

    public ServiceExecutor selectOne(ServiceInvokeEnum invokeEnum) {
        return executorMap.get(invokeEnum);
    }

    @Autowired
    private RegistryManagerService registryManagerService;

    @Autowired
    private NamespaceMangerService namespaceMangerService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private ConfigVersionService configVersionService;

    @Autowired
    private BreakerService breakerService;

    @Autowired
    private AuthService authService;

    @Autowired
    private LimitService limitService;

    @Autowired
    private RouteService routeService;

//    @Autowired
//    private RawKvListenerService kvListenerService;

}
