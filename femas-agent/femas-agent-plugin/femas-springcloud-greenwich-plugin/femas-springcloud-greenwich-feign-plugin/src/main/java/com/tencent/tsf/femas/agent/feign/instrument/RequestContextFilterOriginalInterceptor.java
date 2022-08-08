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
package com.tencent.tsf.femas.agent.feign.instrument;

import com.tencent.tsf.femas.agent.common.HttpServletHeaderUtils;
import com.tencent.tsf.femas.agent.interceptor.OriginalInterceptor;
import com.tencent.tsf.femas.api.ExtensionManager;
import com.tencent.tsf.femas.api.IExtensionLayer;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.RpcContext;
import com.tencent.tsf.femas.common.context.TracingContext;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.ErrorStatus;
import com.tencent.tsf.femas.common.entity.Request;
import com.tencent.tsf.femas.common.entity.Response;
import com.tencent.tsf.femas.common.entity.Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2022/4/26 15:50
 */
public class RequestContextFilterOriginalInterceptor implements OriginalInterceptor {


    private volatile Context commonContext = ExtensionManager.getExtensionLayer().getCommonContext();

    private IExtensionLayer extensionLayer = ExtensionManager.getExtensionLayer();
    private volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();

    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        HttpServletRequest httpServletRequest = (HttpServletRequest) allArguments[0];
        HttpServletResponse httpServletResponse = (HttpServletResponse) allArguments[1];
        Request request = getFemasRequest();
        RpcContext rpcContext = extensionLayer
                .beforeServerInvoke(request, new HttpServletHeaderUtils(httpServletRequest));
        Throwable error = null;
        try {
            if (ErrorStatus.UNAUTHENTICATED.equals(rpcContext.getErrorStatus())) {
                httpServletResponse
                        .sendError(HttpServletResponse.SC_FORBIDDEN, ErrorStatus.UNAUTHENTICATED.getMessage());
            } else if (ErrorStatus.RESOURCE_EXHAUSTED.equals(rpcContext.getErrorStatus())) {
                httpServletResponse.sendError(ErrorStatus.RESOURCE_EXHAUSTED.getCode().Value(),
                        ErrorStatus.RESOURCE_EXHAUSTED.getMessage());
            } else {
                zuper.call();
            }
        } catch (Throwable throwable) {
            // 异常时，如果未设置 status 则设置 500，原本是外层 WebMvcMetricsFilter 才设置，这里提前设置以获取 tracing 信息
            if (httpServletResponse.getStatus() == HttpStatus.OK.value()) {
                httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
            error = throwable;
            throw throwable;
        } finally {
            Response response = new Response();
            response.setError(error);
            fillTracingContext(rpcContext, httpServletRequest, httpServletResponse);
            extensionLayer.afterServerInvoke(response, rpcContext);
        }
        return null;
    }


    private Request getFemasRequest() {
        String serviceName = commonContext.getSystemTag(contextConstant.getServiceName());
        String namespace = commonContext.getSystemTag(contextConstant.getNamespaceId());
        Service service = new Service(namespace, serviceName);
        Request request = new Request();
        request.setTargetService(service);
        return request;
    }

    private void fillTracingContext(RpcContext rpcContext, HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse) {
        TracingContext tracingContext = rpcContext.getTracingContext();
        tracingContext.setLocalServiceName(Context.getSystemTag(contextConstant.getServiceName()));
        tracingContext.setLocalNamespaceId(Context.getSystemTag(contextConstant.getNamespaceId()));
        tracingContext.setLocalInstanceId(Context.getSystemTag(contextConstant.getInstanceId()));
        tracingContext.setLocalApplicationVersion(Context.getSystemTag(contextConstant.getApplicationVersion()));
        tracingContext.setLocalHttpMethod(httpServletRequest.getMethod());
        tracingContext.setLocalInterface(httpServletRequest.getRequestURI());
        String localPort = Context.getSystemTag(contextConstant.getLocalPort());
        if (StringUtils.isNotEmpty(localPort)) {
            tracingContext.setLocalPort(Integer.valueOf(localPort));
        }
        tracingContext.setLocalIpv4(Context.getSystemTag(contextConstant.getLocalIp()));
        tracingContext.setResultStatus(String.valueOf(httpServletResponse.getStatus()));
    }

}
