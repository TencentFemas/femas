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

package com.tencent.tsf.femas.springcloud.gateway.loadbalancer;

import com.tencent.tsf.femas.api.ExtensionManager;
import com.tencent.tsf.femas.api.IExtensionLayer;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.FemasContext;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.springcloud.gateway.discovery.DiscoveryServerConverter;
import com.tencent.tsf.femas.springcloud.gateway.filter.GatewayHeaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.EmptyResponse;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.client.loadbalancer.reactive.Request;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * A Femas-LoadBalancer-based implementation of {@link ReactorServiceInstanceLoadBalancer}.
 * 继承spring cloud loadBalancer的Femas自定义负载均衡器
 *
 * @author juanyinyang
 */
public class FemasRouteLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private static final Logger log = LoggerFactory.getLogger(FemasRouteLoadBalancer.class);
    final String serviceId;
    private final DiscoveryServerConverter converter;
    ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private IExtensionLayer extensionLayer = ExtensionManager.getExtensionLayer();
    private volatile Context commonContext = ContextFactory.getContextInstance();

    /**
     * @param serviceInstanceListSupplierProvider a provider of
     *         {@link ServiceInstanceListSupplier} that will be used to get available instances
     * @param serviceId id of the service for which to choose an instance
     */
    public FemasRouteLoadBalancer(DiscoveryServerConverter converter,
                                  ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
                                  String serviceId) {
        this.converter = converter;
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        Object rpcContextInfo = commonContext.getCopyRpcContext();
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
                .getIfAvailable(NoopServiceInstanceListSupplier::new);
        return supplier.get().next()
                .map(serviceInstances -> processInstanceResponse(serviceInstances, rpcContextInfo, request));
    }

    private Response<ServiceInstance> processInstanceResponse(List<ServiceInstance> serviceInstances, Object rpcContextInfo, Request request) {
        // 跨线程了，重新设置上下文
        commonContext.restoreRpcContext(rpcContextInfo);
        Object context = request.getContext();
        if (context != null && context instanceof ServerHttpRequest) {
            ServerHttpRequest serverHttpRequest = (ServerHttpRequest) context;
            commonContext.getSerializeTagsFromRequestMeta(new GatewayHeaderUtils(serverHttpRequest));
            getFemasRequest(serviceInstances, serverHttpRequest);
        }
        Response<ServiceInstance> serviceInstanceResponse = getInstanceResponse(serviceInstances);

        return serviceInstanceResponse;
    }

    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances) {
        if (instances.isEmpty()) {
            if (log.isWarnEnabled()) {
                log.warn("No servers available for service: " + serviceId);
            }
            return new EmptyResponse();
        }

        List<com.tencent.tsf.femas.common.entity.ServiceInstance> services = new ArrayList<>(instances.size());
        instances.stream().forEach(s -> {
            com.tencent.tsf.femas.common.entity.ServiceInstance i = converter.convert(s);
            services.add(i);
        });
        // 获取要调用的下游服务的服务名
        com.tencent.tsf.femas.common.entity.Request femasRequest = Context.getRpcInfo().getRequest();
        if (femasRequest == null) {
            femasRequest = getFemasRequest(instances, null);
        }
        com.tencent.tsf.femas.common.entity.ServiceInstance serviceInstance = extensionLayer
                .chooseServiceInstance(femasRequest, services);
        if (serviceInstance == null) {
            return new EmptyResponse();
        }
        ServiceInstance selectServer = converter.getOrigin(serviceInstance);

        if (selectServer != null) {
            return new DefaultResponse(selectServer);
        }
        return new EmptyResponse();
    }

    private com.tencent.tsf.femas.common.entity.Request getFemasRequest(final List<ServiceInstance> instances, ServerHttpRequest serverHttpRequest) {
        com.tencent.tsf.femas.common.entity.ServiceInstance serviceInstance = converter.convert(instances.get(0));
        com.tencent.tsf.femas.common.entity.Request femasRequest = new com.tencent.tsf.femas.common.entity.Request();
        String serviceName = null;
        if (serviceInstance != null && serviceInstance.getService() != null) {
            serviceName = serviceInstance.getService().getName();
        }
        Service service = new Service();
        service.setName(serviceName);
        service.setNamespace(serviceInstance.getService().getNamespace());

        femasRequest.setTargetService(service);

        String url = serverHttpRequest.getURI().getPath();
        femasRequest.setInterfaceName(url);
        femasRequest.setTargetMethodSig(serverHttpRequest.getMethodValue() + "/" + url);

        Context.getRpcInfo().setRequest(femasRequest);
        return femasRequest;
    }

}
