package com.tencent.tsf.femas.governance.circuitbreaker.service;

import com.tencent.tsf.femas.common.entity.Request;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService;
import com.tencent.tsf.femas.governance.circuitbreaker.core.CircuitBreaker;
import com.tencent.tsf.femas.governance.plugin.context.ConfigContext;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author zhixinzxliu
 */
public class SingleFemasCircuitBreakerService implements ICircuitBreakerService {

    private CircuitBreaker circuitBreaker;

    public SingleFemasCircuitBreakerService(CircuitBreaker tsfCircuitBreaker) {
        this.circuitBreaker = tsfCircuitBreaker;
    }

    @Override
    public boolean tryAcquirePermission(Request request) {
        return circuitBreaker.tryAcquirePermission();
    }

    @Override
    public void handleSuccessfulServiceRequest(Request request, long responseTime) {
        this.circuitBreaker.onSuccess(responseTime, TimeUnit.MILLISECONDS);
    }

    @Override
    public void handleFailedServiceRequest(Request request, long responseTime, Throwable t) {
        this.circuitBreaker.onError(responseTime, TimeUnit.MILLISECONDS, t);
    }

    @Override
    public Set<ServiceInstance> getOpenInstances(Request request) {
        return null;
    }

    @Override
    public ICircuitBreakerService.State getState(Request request) {
        return circuitBreaker.getState();
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public void init(ConfigContext conf) throws FemasRuntimeException {

    }

    @Override
    public void destroy() {

    }
}
