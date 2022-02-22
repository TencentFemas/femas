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
import io.protostuff.Rpc;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

public class FemasGatewayFilter extends AbstractGlobalFilter {
    private volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();

    private String namespace = Context.getSystemTag(contextConstant.getNamespaceId());
    private RpcContext rpcContext;

    @Override
    public int getOrder() {
        // NettyWriteResponseFilter 之前
        return -2;
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
        Request femasRequest = beforeInvoke(exchange, femasResponse);
        ServerHttpResponse originalResponse = exchange.getResponse();
        ServerHttpResponseDecorator response = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (getStatusCode().value() > HttpStatus.BAD_REQUEST.value()) {
                    femasResponse.setError(new GatewayException(String.valueOf(getStatusCode()), getStatusCode().getReasonPhrase()));
                }
                afterInvoke(rpcContext, serverHttpRequest, serverHttpResponse, femasRequest, femasResponse);
                return super.writeWith(body);
            }
        };
        return chain.filter(exchange.mutate().response(response).build());
    }

    private Request beforeInvoke(ServerWebExchange exchange, Response femasResponse) {
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        ServerHttpResponse serverHttpResponse = exchange.getResponse();

        Request femasRequest = getServerFemasRequest();
        rpcContext = extensionLayer.beforeServerInvoke(femasRequest, new GateWayHeaderUtils(serverHttpRequest));
        if (rpcContext.getErrorStatus() != null) {
            afterServerInvoke(rpcContext, serverHttpResponse, femasResponse);
            throw new GatewayException(rpcContext.getErrorStatus().StatusCode(), rpcContext.getErrorStatus().getCode().name() + ": " + rpcContext.getErrorStatus().getMessage());
        }

        femasRequest = getClientFemasRequest(exchange);
        rpcContext = extensionLayer.beforeClientInvoke(femasRequest, new GateWayHeaderUtils(serverHttpRequest));
        if (rpcContext.getErrorStatus() != null) {
            afterInvoke(rpcContext, serverHttpRequest, serverHttpResponse, femasRequest, femasResponse);
            throw new GatewayException(rpcContext.getErrorStatus().StatusCode(), rpcContext.getErrorStatus().getCode().name() + ": " + rpcContext.getErrorStatus().getMessage());
        }
        return femasRequest;
    }

    private void afterInvoke(RpcContext rpcContext
            , ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse
            , Request femasRequest, Response femasResponse) {
        fillTracingContext(rpcContext, serverHttpRequest, serverHttpResponse);
        extensionLayer.afterClientInvoke(femasRequest, femasResponse, rpcContext);
        Context.getRpcInfo().setRequest(null);

        afterServerInvoke(rpcContext, serverHttpResponse, femasResponse);
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

    private Request getClientFemasRequest(ServerWebExchange exchange) {
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        Request femasRequest = Context.getRpcInfo().getRequest();
        if (femasRequest == null) {
            femasRequest = new Request();
            Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
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
