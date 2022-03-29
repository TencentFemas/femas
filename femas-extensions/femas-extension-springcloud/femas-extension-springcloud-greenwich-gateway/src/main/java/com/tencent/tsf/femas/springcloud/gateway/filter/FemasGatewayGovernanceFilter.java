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

package com.tencent.tsf.femas.springcloud.gateway.filter;

import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.RpcContext;
import com.tencent.tsf.femas.common.context.TracingContext;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.ErrorStatus;
import com.tencent.tsf.femas.common.entity.Request;
import com.tencent.tsf.femas.common.entity.Response;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.springcloud.gateway.exception.GatewayException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.tencent.tsf.femas.springcloud.gateway.filter.FemasReactiveLoadBalancerClientFilter.GATEWAY_FEMAS_REQUEST;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

/**
 * 网关治理
 * @Author jolyonzheng
 * @Description //TODO
 * @Date 2022/3/24
 * @Version v1.0
 **/
public class FemasGatewayGovernanceFilter extends AbstractGlobalFilter {

    private volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();
    private String namespace = Context.getSystemTag(contextConstant.getNamespaceId());

    @Override
    public int getOrder() {
        return FemasReactiveLoadBalancerClientFilter.FEMAS_LOAD_BALANCER_CLIENT_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return true;
    }

    @Override
    public Mono<Void> doFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        ServerHttpResponse serverHttpResponse = exchange.getResponse();
        Response femasResponse = new Response();

        RpcContext serverRpcContext = this.beforeServerInvoke(serverHttpRequest, serverHttpResponse, femasResponse);

        Request clientFemasRequest = getClientFemasRequest(serverHttpRequest, exchange.getAttribute(GATEWAY_ROUTE_ATTR));
        RpcContext clientRpcContext;
        try {
            clientRpcContext = this.beforeClientInvoke(exchange, clientFemasRequest, femasResponse);
        } catch (Exception e) {
            afterServerInvoke(serverRpcContext, serverHttpResponse, femasResponse);
            throw e;
        }

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // Manipulate the response
            Request femasRequest = exchange.getAttribute(GATEWAY_FEMAS_REQUEST);
            ServerHttpResponse response = exchange.getResponse();
            try {
                if (response.getStatusCode().value() > HttpStatus.BAD_REQUEST.value()) {
                    femasResponse.setError(new GatewayException(String.valueOf(response.getStatusCode()), response.getStatusCode().getReasonPhrase()));
                    femasResponse.setErrorStatus(ErrorStatus.INTERNAL);
                }
            } catch (Exception e) {
                femasResponse.setErrorStatus(ErrorStatus.INTERNAL);
                femasResponse.setError(new GatewayException(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())));
            }
            afterClientInvoke(clientRpcContext, exchange, femasRequest, femasResponse);
            afterServerInvoke(serverRpcContext, serverHttpResponse, femasResponse);
        }));
    }

    private RpcContext beforeServerInvoke(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, Response femasResponse) {
        Request serverFemasRequest = getServerFemasRequest();
        RpcContext serverRpcContext = extensionLayer.beforeServerInvoke(serverFemasRequest, new GatewayHeaderUtils(serverHttpRequest));
        if (serverRpcContext.getErrorStatus() != null) {
            afterServerInvoke(serverRpcContext, serverHttpResponse, femasResponse);
            throw new GatewayException(serverRpcContext.getErrorStatus().StatusCode(), serverRpcContext.getErrorStatus().getCode().name() + ": " + serverRpcContext.getErrorStatus().getMessage());
        }
        return serverRpcContext;
    }

    private RpcContext beforeClientInvoke(ServerWebExchange exchange, Request clientFemasRequest, Response femasResponse) {
        ServerHttpRequest serverHttpRequest = exchange.getRequest();

        RpcContext clientRpcContext = extensionLayer.beforeClientInvoke(clientFemasRequest, new GatewayHeaderUtils(serverHttpRequest));
        if (clientRpcContext.getErrorStatus() != null) {
            afterClientInvoke(clientRpcContext, exchange, clientFemasRequest, femasResponse);
            throw new GatewayException(clientRpcContext.getErrorStatus().StatusCode(), clientRpcContext.getErrorStatus().getCode().name() + ": " + clientRpcContext.getErrorStatus().getMessage());
        }
        return clientRpcContext;
    }

    private void afterClientInvoke(RpcContext rpcContext, ServerWebExchange exchange, Request femasRequest, Response femasResponse) {
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        ServerHttpResponse serverHttpResponse = exchange.getResponse();

        fillTracingContext(rpcContext, serverHttpRequest, serverHttpResponse);
        extensionLayer.afterClientInvoke(femasRequest, femasResponse, rpcContext);
    }

    private void afterServerInvoke(RpcContext rpcContext, ServerHttpResponse serverHttpResponse, Response femasResponse) {
//        fillTracingContext(rpcContext, serverHttpRequest, serverHttpResponse);
        extensionLayer.afterServerInvoke(femasResponse, rpcContext);
        if (rpcContext.getErrorStatus() != null) {
            if (ErrorStatus.UNAUTHENTICATED.equals(rpcContext.getErrorStatus())) {
                serverHttpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
            } else if (ErrorStatus.RESOURCE_EXHAUSTED.equals(rpcContext.getErrorStatus())) {
                serverHttpResponse.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            } else {
                serverHttpResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            femasResponse.setError(new GatewayException(rpcContext.getErrorStatus().StatusCode(), rpcContext.getErrorStatus().getMessage()));
        }
    }

    private Request getClientFemasRequest(ServerHttpRequest serverHttpRequest, Route route) {
        Request femasRequest = Context.getRpcInfo().getRequest();
        if (femasRequest == null) {
            femasRequest = new Request();
            Service service = new Service();
            service.setName(route.getUri() != null ? route.getUri().getHost() : null);
            service.setNamespace(namespace);

            femasRequest.setTargetService(service);
        }
        String url = serverHttpRequest.getURI().getPath();
        femasRequest.setInterfaceName(url);
        femasRequest.setTargetMethodSig(serverHttpRequest.getMethodValue() + "/" + url);
        femasRequest.setDoneChooseInstance(true);
        return femasRequest;
    }

    private Request getServerFemasRequest() {
        String serviceName = Context.getSystemTag(contextConstant.getServiceName());
        String namespace = Context.getSystemTag(contextConstant.getNamespaceId());
        Service service = new Service(namespace, serviceName);
        Request request = new Request();
        request.setTargetService(service);
        return request;
    }

    private void fillTracingContext(RpcContext rpcContext, ServerHttpRequest httpServletRequest, ServerHttpResponse httpServletResponse) {
        TracingContext tracingContext = rpcContext.getTracingContext();
        tracingContext.setLocalServiceName(Context.getSystemTag(contextConstant.getServiceName()));
        tracingContext.setLocalNamespaceId(Context.getSystemTag(contextConstant.getNamespaceId()));
        tracingContext.setLocalInstanceId(Context.getSystemTag(contextConstant.getInstanceId()));
        tracingContext.setLocalApplicationVersion(Context.getSystemTag(contextConstant.getApplicationVersion()));
        tracingContext.setLocalHttpMethod(httpServletRequest.getMethod().toString());
        tracingContext.setLocalInterface(httpServletRequest.getURI().getPath());
        String localPort = Context.getSystemTag(contextConstant.getLocalPort());
        if (StringUtils.isNotEmpty(localPort)) {
            tracingContext.setLocalPort(Integer.valueOf(localPort));
        }
        tracingContext.setLocalIpv4(Context.getSystemTag(contextConstant.getLocalIp()));
        tracingContext.setResultStatus(String.valueOf(httpServletResponse.getStatusCode().value()));
    }
}
