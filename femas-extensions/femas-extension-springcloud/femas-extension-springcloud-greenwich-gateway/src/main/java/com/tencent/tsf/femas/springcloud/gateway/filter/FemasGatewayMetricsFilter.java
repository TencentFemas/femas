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


import com.tencent.tsf.femas.agent.tools.FemasRuntimeException;
import com.tencent.tsf.femas.common.serialize.JSONSerializer;
import com.tencent.tsf.femas.plugin.impl.FemasPluginContext;
import com.tencent.tsf.femas.governance.metrics.MetricsExporter;
import com.tencent.tsf.femas.governance.metrics.micrometer.exporter.PrometheusMeterExporter;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 网关metrics
 *
 * @Author leo
 **/
public class FemasGatewayMetricsFilter extends AbstractGlobalFilter {

    private final MetricsExporter metricsExporter = FemasPluginContext.getMetricsExporter();

    private CollectorRegistry collectorRegistry = null;

    @Override
    public int getOrder() {
        return FemasReactiveLoadBalancerClientFilter.HIGHEST_PRECEDENCE;
    }

    @Override
    public boolean shouldFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return true;
    }

    @Override
    public Mono<Void> doFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        if (serverHttpRequest.getPath().value().contains("/femas/actuator/prometheus")) {
            response.setStatusCode(HttpStatus.OK);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            String metricsRes;
            Writer writer = new StringWriter();
            try {
                if (null == collectorRegistry) {
                    initCollectorRegistry();
                }
                TextFormat.write004(writer, this.collectorRegistry.metricFamilySamples());
                metricsRes = writer.toString();
            } catch (IOException e) {
                throw new FemasRuntimeException("Writing metrics failed", e);
            } catch (Exception e) {
                throw new FemasRuntimeException("Writing metrics failed", e);
            }
            DataBuffer dataBuffer = response.bufferFactory().allocateBuffer().write(metricsRes.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(dataBuffer));
        } else {
            return chain.filter(exchange);
        }
    }

    private void initCollectorRegistry() {
        if (metricsExporter instanceof PrometheusMeterExporter) {
            ((PrometheusMeterExporter) metricsExporter).initPrometheus();
            this.collectorRegistry = ((PrometheusMeterExporter) metricsExporter).getCollectorRegistry();
        } else {
            this.collectorRegistry = null;
        }
    }
}