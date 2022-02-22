package com.tencent.tsf.femas.governance.circuitbreaker.service;

import static com.tencent.tsf.femas.governance.circuitbreaker.FemasCircuitBreakerIsolationLevelEnum.API;
import static com.tencent.tsf.femas.governance.circuitbreaker.FemasCircuitBreakerIsolationLevelEnum.INSTANCE;
import static com.tencent.tsf.femas.governance.circuitbreaker.FemasCircuitBreakerIsolationLevelEnum.SERVICE;

import com.tencent.tsf.femas.common.entity.Request;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.governance.circuitbreaker.FemasCircuitBreakerIsolationLevelEnum;
import com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService;
import com.tencent.tsf.femas.governance.circuitbreaker.core.CircuitBreakerFactory;
import com.tencent.tsf.femas.governance.circuitbreaker.rule.CircuitBreakerRule;
import com.tencent.tsf.femas.governance.circuitbreaker.rule.CircuitBreakerStrategy;
import com.tencent.tsf.femas.governance.plugin.context.ConfigContext;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author zhixinzxliu
 */
public class ServiceCircuitBreakerService implements ICircuitBreakerService<CircuitBreakerRule> {

    private static final Logger logger = LoggerFactory.getLogger(ServiceCircuitBreakerService.class);

    private FemasCircuitBreakerIsolationLevelEnum isolationLevel;
    private ICircuitBreakerService circuitBreakerService;

    protected ServiceCircuitBreakerService(CircuitBreakerRule rule) {
        this.isolationLevel = rule.getIsolationLevel();

        // 如果是Service级别熔断，则初始化熔断器
        if (isolationLevel == SERVICE) {
            CircuitBreakerStrategy strategy = rule.getStrategyList().get(0);
            this.circuitBreakerService = new SingleFemasCircuitBreakerService(
                    CircuitBreakerFactory.newCircuitBreaker(rule.getTargetService().toString(), rule, strategy));
        }

        // 实例级别则初始化实例熔断服务
        if (isolationLevel == INSTANCE) {
            this.circuitBreakerService = new InstanceCircuitBreakerService(rule);
        }

        // API级别
        if (isolationLevel == API) {
            this.circuitBreakerService = new ApiCircuitBreakerService(rule);
        }
    }

    @Override
    public boolean tryAcquirePermission(Request request) {
        return this.circuitBreakerService.tryAcquirePermission(request);
    }

    @Override
    public void handleSuccessfulServiceRequest(Request request, long responseTime) {
        this.circuitBreakerService.handleSuccessfulServiceRequest(request, responseTime);
    }

    @Override
    public void handleFailedServiceRequest(Request request, long responseTime, Throwable t) {
        this.circuitBreakerService.handleFailedServiceRequest(request, responseTime, t);
    }

    @Override
    public Set<ServiceInstance> getOpenInstances(Request request) {
        return circuitBreakerService.getOpenInstances(request);
    }

    @Override
    public ICircuitBreakerService.State getState(Request request) {
        return circuitBreakerService.getState(request);
    }


    public FemasCircuitBreakerIsolationLevelEnum getIsolationLevel() {
        return isolationLevel;
    }

    public Set<String> getAllApiName() {
        if (isolationLevel == API) {
            return ((ApiCircuitBreakerService) circuitBreakerService).getAllApiName();
        }

        return null;
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
