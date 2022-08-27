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

package com.tencent.tsf.femas.extension.springcloud.common.instrumentation.restapi;

import com.google.gson.Gson;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.governance.api.entity.ServiceApi;
import com.tencent.tsf.femas.governance.connector.server.ServerConnectorManager;
import com.tencent.tsf.femas.plugin.impl.FemasPluginContext;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import springfox.documentation.service.Documentation;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.spring.web.json.JsonSerializer;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2Mapper;

public class FemasApiMetadataGrapher implements SmartLifecycle {

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private Logger logger = LoggerFactory.getLogger(FemasApiMetadataGrapher.class);
    private ApplicationContext applicationContext;
    private ServiceModelToSwagger2Mapper swagger2Mapper;
    private DocumentationCache documentationCache;
    private JsonSerializer jsonSerializer;
    private String groupName;
    private volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();
    private volatile Context context = ContextFactory.getContextInstance();
    private ServerConnectorManager manager = FemasPluginContext.getServerConnectorManager();

    public FemasApiMetadataGrapher(DocumentationCache documentationCache, ServiceModelToSwagger2Mapper swagger2Mapper,
                                   JsonSerializer jsonSerializer, String groupName, ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.swagger2Mapper = swagger2Mapper;
        this.documentationCache = documentationCache;
        this.jsonSerializer = jsonSerializer;
        this.groupName = groupName;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable runnable) {
        runnable.run();
        stop();
    }

    @Override
    public void start() {
        if (!isRunning.compareAndSet(false, true)) {
            return;
        }
        try {
            Documentation documentation = documentationCache.documentationByGroup(groupName);
            Swagger swagger = swagger2Mapper.mapDocumentation(documentation);
            if (swagger != null) {
                List<ServiceApi> serviceApis = buildServiceApis(swagger);
                reportApi(new Gson().toJson(serviceApis));
            }
        } catch (Throwable t) {
            logger.warn("[femas swagger] init femasApiMetadataGrapher failed. occur exception: ", t);
        }
    }

    private void reportApi(String apis) {
        String serviceName = Context.getSystemTag(contextConstant.getServiceName());
        String namespaceId = Context.getSystemTag(contextConstant.getNamespaceId());
        String applicationVersion = Context.getSystemTag(contextConstant.getApplicationVersion());
        manager.reportApis(namespaceId, serviceName, applicationVersion, apis);
    }

    private List<ServiceApi> buildServiceApis(Swagger swagger) {
        List<ServiceApi> serviceApis = new ArrayList<>();
        if (swagger == null || swagger.getPaths() == null) {
            return serviceApis;
        }
        String basePath = getBasePath(swagger);
        String applicationVersion = Context.getSystemTag(contextConstant.getApplicationVersion());
        for (Map.Entry<String, Path> entry : swagger.getPaths().entrySet()) {
            Path path = entry.getValue();
            for (Map.Entry<HttpMethod, Operation> operationEntry : path.getOperationMap().entrySet()) {
                ServiceApi api = new ServiceApi();
                api.setMethod(operationEntry.getKey().name());
                api.setPath(basePath + entry.getKey());
                api.setServiceVersion(applicationVersion);
                api.setStatus(ServiceApi.Status.NORMAL.getCode());
                serviceApis.add(api);
            }
        }
        return serviceApis;
    }

    private String getBasePath(Swagger swagger) {
        String basePath = "";
        if (!"/".equals(swagger.getBasePath())) {
            basePath = swagger.getBasePath();
        }
        return basePath;
    }

    @Override
    public void stop() {
        isRunning.set(true);
    }

    @Override
    public boolean isRunning() {
        return isRunning.get();
    }

    @Override
    public int getPhase() {
        // same with DocumentationPluginsBootstrapper
        return Integer.MAX_VALUE;
    }
}
