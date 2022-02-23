package com.tencent.tsf.femas.springcloud.gateway.loadbalancer;

import com.tencent.tsf.femas.api.ExtensionManager;
import com.tencent.tsf.femas.api.IExtensionLayer;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.springcloud.gateway.discovery.DiscoveryServerConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.reactive.Request;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Femas-LoadBalancer-based implementation of {@link ReactorServiceInstanceLoadBalancer}.
 * 继承spring cloud loadBalancer的Femas自定义负载均衡器
 *
 * @author juanyinyang
 */
public class FemasRouteLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    public static final String BEFORE_INVOKE_FLAG_KEY = "before.invoke.flag";
    private static final Logger log = LoggerFactory.getLogger(FemasRouteLoadBalancer.class);
    final String serviceId;
    private final DiscoveryServerConverter converter;
    ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private IExtensionLayer extensionLayer = ExtensionManager.getExtensionLayer();


    /**
     * @param serviceInstanceListSupplierProvider a provider of
     *                                            {@link ServiceInstanceListSupplier} that will be used to get available instances
     * @param serviceId                           id of the service for which to choose an instance
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
        // 不能直接引用，需要重新构建 map
        Map<String, String> rpcContextInfo = new HashMap<>(Context.getRpcInfo().getAll());
        HttpHeaders headers = (HttpHeaders) request.getContext();

        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
                .getIfAvailable(NoopServiceInstanceListSupplier::new);
        return supplier.get().next()
                .map(serviceInstances -> processInstanceResponse(supplier, serviceInstances, rpcContextInfo,headers));
    }

    private Response<ServiceInstance> processInstanceResponse(ServiceInstanceListSupplier supplier,
                                                              List<ServiceInstance> serviceInstances, Map<String, String> rpcContextInfo, HttpHeaders httpHeaders) {
        // 跨线程了，重新设置上下文, todo 后续可能换种更好的方式
        Context.getRpcInfo().reset();
        for (Map.Entry<String, String> entry : rpcContextInfo.entrySet()) {
            if (StringUtils.isNotEmpty(entry.getValue())) {
                Context.getRpcInfo().put(entry.getKey(), entry.getValue());
            }
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
        boolean beforeInvokeFlag = true;
        // 获取要调用的下游服务的服务名
        com.tencent.tsf.femas.common.entity.Request request = Context.getRpcInfo().getRequest();
        // rest template 在之前已经获取，feign 这里才获取
        if (request == null) {
            request = getRequest(instances);
            Context.getRpcInfo().setRequest(request);
            beforeInvokeFlag = false;
        }
        Context.getRpcInfo().put(BEFORE_INVOKE_FLAG_KEY, String.valueOf(beforeInvokeFlag));
        com.tencent.tsf.femas.common.entity.ServiceInstance serviceInstance = extensionLayer
                .chooseServiceInstance(request, services);
        if (serviceInstance == null) {
            return new EmptyResponse();
        }
        ServiceInstance selectServer = converter.getOrigin(serviceInstance);
        if (selectServer != null) {
            return new DefaultResponse(selectServer);
        }
        return new EmptyResponse();
    }

    private com.tencent.tsf.femas.common.entity.Request getRequest(final List<ServiceInstance> instances) {
        com.tencent.tsf.femas.common.entity.ServiceInstance serviceInstance = converter.convert(instances.get(0));
        com.tencent.tsf.femas.common.entity.Request request = new com.tencent.tsf.femas.common.entity.Request();
        String serviceName = null;
        if (serviceInstance != null && serviceInstance.getService() != null) {
            serviceName = serviceInstance.getService().getName();
        }
        Service service = new Service();
        service.setName(serviceName);
        service.setNamespace(serviceInstance.getService().getNamespace());

        request.setTargetService(service);
        Context.getRpcInfo().setRequest(request);
        return request;
    }

}
