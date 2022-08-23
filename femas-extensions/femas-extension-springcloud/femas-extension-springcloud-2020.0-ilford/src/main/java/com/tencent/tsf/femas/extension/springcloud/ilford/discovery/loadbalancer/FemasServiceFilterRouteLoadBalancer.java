package com.tencent.tsf.femas.extension.springcloud.ilford.discovery.loadbalancer;

import org.springframework.cloud.client.ServiceInstance;

import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.entity.Request;
import com.tencent.tsf.femas.governance.circuitbreaker.FemasCircuitBreakerIsolationLevelEnum;
import com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService;
import com.tencent.tsf.femas.plugin.impl.FemasPluginContext;

/**
 * @author juanyinyang
 */
public class FemasServiceFilterRouteLoadBalancer implements FemasServiceFilterLoadBalancer {

    public static final String BEFORE_INVOKE_FLAG_KEY = "before.invoke.flag";
    private final ICircuitBreakerService circuitBreakerService = FemasPluginContext.getCircuitBreakers().get(0);

    public FemasServiceFilterRouteLoadBalancer() {
    }

    @Override
    public void beforeChooseServer(Object key) {

    }

    @Override
    public void afterChooseServer(ServiceInstance server, Object key) {
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
}
