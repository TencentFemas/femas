package com.tencent.tsf.femas.extension.springcloud.ilford.discovery.loadbalancer;

import com.tencent.tsf.femas.api.ExtensionManager;
import com.tencent.tsf.femas.api.IExtensionLayer;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.SelectedInstanceCallback;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
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

    public static final String BEFORE_INVOKE_FLAG_KEY = "before.invoke.flag";
    private static final Logger log = LoggerFactory.getLogger(FemasRouteLoadBalancer.class);
    final String serviceId;
    private final DiscoveryServerConverter converter;
    private List<FemasServiceFilterLoadBalancer> loadBalancerList;
    ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private IExtensionLayer extensionLayer = ExtensionManager.getExtensionLayer();
    private volatile Context commonContext = ContextFactory.getContextInstance();

    /**
     * @param serviceInstanceListSupplierProvider a provider of
     *         {@link ServiceInstanceListSupplier} that will be used to get available instances
     * @param serviceId id of the service for which to choose an instance
     */
    public FemasRouteLoadBalancer(DiscoveryServerConverter converter,
            List<FemasServiceFilterLoadBalancer> loadBalancerList,
            ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
            String serviceId) {
        this.converter = converter;
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.loadBalancerList = loadBalancerList;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        Object rpcContextInfo = commonContext.getCopyRpcContext();
        com.tencent.tsf.femas.common.entity.Request rpcInfoRequest = Context.getRpcInfo().getRequest();

        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
                .getIfAvailable(NoopServiceInstanceListSupplier::new);
        return supplier.get(request).next()
                .map(serviceInstances -> processInstanceResponse(supplier, serviceInstances, rpcContextInfo, rpcInfoRequest));
    }

    private Response<ServiceInstance> processInstanceResponse(
            ServiceInstanceListSupplier supplier,
            List<ServiceInstance> serviceInstances, Object rpcContextInfo, com.tencent.tsf.femas.common.entity.Request rpcInfoRequest) {
        // 跨线程了，重新设置上下文
        commonContext.restoreRpcContext(rpcContextInfo);
        Context.getRpcInfo().setRequest(rpcInfoRequest);
        
        loadBalancerList.forEach(femasServiceFilterLoadBalancer -> femasServiceFilterLoadBalancer.beforeChooseServer(serviceInstances));

        Response<ServiceInstance> serviceInstanceResponse = getInstanceResponse(serviceInstances);
        if (supplier instanceof SelectedInstanceCallback && serviceInstanceResponse.hasServer()) {
            ((SelectedInstanceCallback) supplier).selectedServiceInstance(serviceInstanceResponse.getServer());
        }
        loadBalancerList.forEach(femasServiceFilterLoadBalancer -> femasServiceFilterLoadBalancer.afterChooseServer(serviceInstanceResponse.getServer(), serviceInstances));
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
