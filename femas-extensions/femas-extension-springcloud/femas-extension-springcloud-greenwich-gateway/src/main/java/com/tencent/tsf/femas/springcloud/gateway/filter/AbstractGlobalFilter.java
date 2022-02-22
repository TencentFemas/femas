package com.tencent.tsf.femas.springcloud.gateway.filter;

import com.tencent.tsf.femas.api.ExtensionManager;
import com.tencent.tsf.femas.api.IExtensionLayer;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public abstract class AbstractGlobalFilter implements GlobalFilter, Ordered {

    protected final IExtensionLayer extensionLayer = ExtensionManager.getExtensionLayer();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (shouldFilter(exchange, chain)) {
            return doFilter(exchange, chain);
        } else {
            return chain.filter(exchange);
        }
    }

    @Override
    abstract public int getOrder();

    abstract public boolean shouldFilter(ServerWebExchange exchange, GatewayFilterChain chain);

    abstract public Mono<Void> doFilter(ServerWebExchange exchange, GatewayFilterChain chain);
}