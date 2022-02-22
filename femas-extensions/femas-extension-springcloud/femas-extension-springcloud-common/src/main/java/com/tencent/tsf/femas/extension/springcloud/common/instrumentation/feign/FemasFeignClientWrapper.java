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

package com.tencent.tsf.femas.extension.springcloud.common.instrumentation.feign;

import com.tencent.tsf.femas.api.ExtensionManager;
import com.tencent.tsf.femas.api.IExtensionLayer;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.RpcContext;
import com.tencent.tsf.femas.common.context.TracingContext;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.ErrorStatus;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import feign.Client;
import feign.Request;
import feign.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author juanyinyang
 */
public class FemasFeignClientWrapper implements Client {

    private static final Logger logger = LoggerFactory.getLogger(FemasFeignClientWrapper.class);
    final Client delegate;
    private volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();
    private String namespace = Context.getSystemTag(contextConstant.getNamespaceId());
    private IExtensionLayer extensionLayer = ExtensionManager.getExtensionLayer();

    public FemasFeignClientWrapper(Client client) {
        this.delegate = client;
        logger.info("[FEMAS CIRCUIT BREAKER] FemasCircuitBreakerFeignClientWrapper wrapped client:{}. ", client);
    }

    /**
     * chooseInstance -> FemasFeignClientWrapper#execute
     */
    @Override
    public Response execute(Request request, Request.Options options) throws IOException {

        com.tencent.tsf.femas.common.entity.Request femasRequest = Context.getRpcInfo().getRequest();
        URL url = getUrl(request);
        if (femasRequest == null) {
            femasRequest = getFemasRequest(request, url);
        }
        String httpMethod = request.httpMethod().name();
        femasRequest.setInterfaceName(url.getPath());
        femasRequest.setTargetMethodSig(httpMethod + "/" + url.getPath());
        femasRequest.setDoneChooseInstance(true);
        RpcContext rpcContext = extensionLayer.beforeClientInvoke(femasRequest, new FeignHeaderUtils(request));

        Response response = null;
        Throwable error = null;
        try {
            // 如果需要熔断
            if (rpcContext.getErrorStatus() != null && ErrorStatus.Code.CIRCUIT_BREAKER
                    .equals(rpcContext.getErrorStatus().getCode())) {
                throw new RuntimeException("CircuitBreaker Error. IsolationLevel : " +
                        rpcContext.getErrorStatus().getMessage() + ", Request : " + femasRequest);
            }
            response = this.delegate.execute(request, options);
        } catch (Throwable throwable) {
            error = throwable;
            throw throwable;
        } finally {
            com.tencent.tsf.femas.common.entity.Response femasResponse = new com.tencent.tsf.femas.common.entity.Response();
            if (error != null) {
                femasResponse.setError(error);
            } else if (response.status() >= HttpStatus.SC_BAD_REQUEST) {
                // 设置 error，保持 afterClientInvoke 逻辑统一
                femasResponse.setError(new RuntimeException(String.valueOf(response.status())));
            }
            fillTracingContext(rpcContext, request, response, femasRequest, url);
            extensionLayer.afterClientInvoke(femasRequest, femasResponse, rpcContext);
            Context.getRpcInfo().setRequest(null);
        }

        return response;
    }

    private URL getUrl(Request request) {
        URL url = null;
        try {
            url = new URL(request.url());
        } catch (MalformedURLException e) {
            logger.warn("MalformedURLException, feign request:{}", request);
        }
        return url;
    }

    private com.tencent.tsf.femas.common.entity.Request getFemasRequest(Request request, URL url) {
        com.tencent.tsf.femas.common.entity.Request femasRequest = new com.tencent.tsf.femas.common.entity.Request();
        Service service = new Service();
        if (url != null) {
            service.setName(url.getHost());
        }
        service.setNamespace(namespace);
        femasRequest.setTargetService(service);
        return femasRequest;
    }

    private void fillTracingContext(RpcContext rpcContext, Request request, Response response
            , com.tencent.tsf.femas.common.entity.Request femasRequest, URL url) {
        TracingContext tracingContext = rpcContext.getTracingContext();
        tracingContext.setProtocol("http");
        // local
        tracingContext.setLocalServiceName(Context.getSystemTag(contextConstant.getServiceName()));
        tracingContext.setLocalNamespaceId(Context.getSystemTag(contextConstant.getNamespaceId()));
        tracingContext.setLocalInstanceId(Context.getSystemTag(contextConstant.getInstanceId()));
        tracingContext.setLocalApplicationVersion(Context.getSystemTag(contextConstant.getApplicationVersion()));
        tracingContext.setLocalHttpMethod(Context.getRpcInfo().get(contextConstant.getRequestHttpMethod()));
        tracingContext.setLocalInterface(Context.getRpcInfo().get(contextConstant.getInterface()));
        tracingContext.setLocalIpv4(Context.getSystemTag(contextConstant.getLocalIp()));
        String localPort = Context.getSystemTag(contextConstant.getLocalPort());
        if (StringUtils.isNotEmpty(localPort)) {
            tracingContext.setLocalPort(Integer.valueOf(localPort));
        }

        // remote
        tracingContext.setRemoteHttpMethod(request.method());
        if (url != null) {
            tracingContext.setRemoteInterface(url.getPath());
            tracingContext.setRemoteIpv4(url.getHost());
            tracingContext.setRemotePort(url.getPort());
        }
        ServiceInstance serviceInstance = femasRequest.getTargetServiceInstance();
        if (serviceInstance != null && serviceInstance.getAllMetadata() != null) {
            tracingContext.setRemoteApplicationVersion(
                    serviceInstance.getMetadata(contextConstant.getMetaApplicationVersionKey()));
            tracingContext.setRemoteInstanceId(serviceInstance.getMetadata(contextConstant.getMetaInstanceIdKey()));
        }
        Service targetService = femasRequest.getTargetService();
        if (targetService != null) {
            tracingContext.setRemoteServiceName(targetService.getName());
            tracingContext.setRemoteNamespaceId(targetService.getNamespace());
        }
        if (response != null) {
            tracingContext.setResultStatus(String.valueOf(response.status()));
        }
        // clean
        Context.getRpcInfo().put(contextConstant.getInterface(), null);
        Context.getRpcInfo().put(contextConstant.getRequestHttpMethod(), null);
    }
}
  