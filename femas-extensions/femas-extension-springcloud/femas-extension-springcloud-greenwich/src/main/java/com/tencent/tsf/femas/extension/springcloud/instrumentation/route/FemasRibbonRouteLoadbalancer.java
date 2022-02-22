package com.tencent.tsf.femas.extension.springcloud.instrumentation.route;

import com.google.common.collect.Lists;
import com.netflix.loadbalancer.Server;
import com.tencent.tsf.femas.api.ExtensionManager;
import com.tencent.tsf.femas.api.IExtensionLayer;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.Request;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.extension.springcloud.discovery.ribbon.DiscoveryServerConverter;
import com.tencent.tsf.femas.extension.springcloud.discovery.ribbon.FemasServiceFilterLoadBalancer;
import com.tencent.tsf.femas.governance.circuitbreaker.FemasCircuitBreakerIsolationLevelEnum;
import com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService;
import com.tencent.tsf.femas.governance.config.FemasPluginContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author leo
 */
public class FemasRibbonRouteLoadbalancer implements FemasServiceFilterLoadBalancer {

    public static final String BEFORE_INVOKE_FLAG_KEY = "before.invoke.flag";
    private static Logger logger = LoggerFactory.getLogger(FemasRibbonRouteLoadbalancer.class);
    private final ICircuitBreakerService circuitBreakerService = FemasPluginContext.getCircuitBreakers().get(0);
    private final DiscoveryServerConverter converter;
    private IExtensionLayer extensionLayer = ExtensionManager.getExtensionLayer();
    private volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();

    public FemasRibbonRouteLoadbalancer(DiscoveryServerConverter converter) {
        this.converter = converter;
    }

    @Override
    public void beforeChooseServer(Object key) {

    }

    @Override
    public void afterChooseServer(Server server, Object key) {
        Request request = Context.getRpcInfo().getRequest();
        // rest 的先进行 before invoke 的判断，因此需要这里进入熔断级别判断和抛异常
        String beforeInvokeFlag = Context.getRpcInfo().get(BEFORE_INVOKE_FLAG_KEY);
        Context.getRpcInfo().put(BEFORE_INVOKE_FLAG_KEY, null);
        if (server != null && Boolean.TRUE.toString().equals(beforeInvokeFlag)
                && !circuitBreakerService.tryAcquirePermission(request)) {
            FemasCircuitBreakerIsolationLevelEnum isolationLevel = circuitBreakerService
                    .getServiceCircuitIsolationLevel(request.getTargetService());
            throw new RuntimeException(
                    "CircuitBreaker Error. IsolationLevel : " + isolationLevel + ", Request : " + request);
        }
    }

    @Override
    public List<Server> filterAllServer(List<Server> servers) {
        if (CollectionUtil.isEmpty(servers)) {
            return Collections.emptyList();
        }
        List<ServiceInstance> services = new ArrayList<>(servers.size());
        servers.stream().forEach(s -> {
            ServiceInstance i = converter.convert(s);
            services.add(i);
        });
        boolean beforeInvokeFlag = true;
        // 获取要调用的下游服务的服务名
        Request request = Context.getRpcInfo().getRequest();
        // rest template 在之前已经获取，feign 这里才获取
        if (request == null) {
            request = getRequest(servers);
            Context.getRpcInfo().setRequest(request);
            beforeInvokeFlag = false;
        }
        Context.getRpcInfo().put(BEFORE_INVOKE_FLAG_KEY, String.valueOf(beforeInvokeFlag));
        ServiceInstance serviceInstance = extensionLayer.chooseServiceInstance(request, services);
        if (serviceInstance == null) {
            return Collections.emptyList();
        }
        Server selectServer = converter.getOrigin(serviceInstance);

        if (selectServer != null) {
            return Lists.newArrayList(selectServer);
        } else {
            return Collections.emptyList();
        }
    }

    public DiscoveryServerConverter getConverter() {
        return converter;
    }

    private Request getRequest(final List<Server> servers) {
        ServiceInstance serviceInstance = converter.convert(servers.get(0));
        Request request = new Request();
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
