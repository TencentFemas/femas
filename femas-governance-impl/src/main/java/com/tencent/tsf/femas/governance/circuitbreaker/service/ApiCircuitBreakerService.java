package com.tencent.tsf.femas.governance.circuitbreaker.service;

import com.tencent.tsf.femas.common.entity.Request;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService;

import com.tencent.tsf.femas.plugin.context.ConfigContext;
import com.tencent.tsf.femas.plugin.impl.config.rule.circuitbreaker.CircuitBreakerRule;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhixinzxliu
 */
public class ApiCircuitBreakerService implements ICircuitBreakerService {

    private static final Logger logger = LoggerFactory.getLogger(ApiCircuitBreakerService.class);

    private Map<String, ICircuitBreakerService> apiCircuitBreakerMap = new ConcurrentHashMap<>();
    private CircuitBreakerAPITrie trie = new CircuitBreakerAPITrie();

    protected ApiCircuitBreakerService(CircuitBreakerRule rule) {
        trie.buildTrie(rule);
    }

    @Override
    public boolean tryAcquirePermission(Request request) {
        String targetMethod = request.getTargetMethodSig();
        if (StringUtils.isEmpty(targetMethod)) {
            return true;
        }

        ICircuitBreakerService circuitBreakerService = trie.search(targetMethod);
        if (circuitBreakerService != null) {
            return circuitBreakerService.tryAcquirePermission(request);
        } else {
            return true;
        }
    }

    @Override
    public void handleSuccessfulServiceRequest(Request request, long responseTime) {
        String targetMethod = request.getTargetMethodSig();
        if (StringUtils.isEmpty(targetMethod)) {
            return;
        }

        ICircuitBreakerService circuitBreakerService = trie.search(targetMethod);
        if (circuitBreakerService != null) {
            circuitBreakerService.handleSuccessfulServiceRequest(request, responseTime);
        }
    }

    @Override
    public void handleFailedServiceRequest(Request request, long responseTime, Throwable t) {
        // 后续需要考虑 rpc 类型的 api
        String targetMethod = request.getTargetMethodSig();
        if (StringUtils.isEmpty(targetMethod)) {
            return;
        }

        ICircuitBreakerService circuitBreakerService = trie.search(targetMethod);
        if (circuitBreakerService != null) {
            circuitBreakerService.handleFailedServiceRequest(request, responseTime, t);
        }
    }

    @Override
    public Set<ServiceInstance> getOpenInstances(Request request) {
        return null;
    }

    @Override
    public ICircuitBreakerService.State getState(Request request) {
        String targetMethod = request.getTargetMethodSig();
        if (StringUtils.isEmpty(targetMethod)) {
            return ICircuitBreakerService.State.UNREGISTERED;
        }

        ICircuitBreakerService circuitBreakerService = trie.search(targetMethod);
        if (circuitBreakerService != null) {
            return circuitBreakerService.getState(request);
        }

        return ICircuitBreakerService.State.UNREGISTERED;
    }

    public Set<String> getAllApiName() {
        return apiCircuitBreakerMap.keySet();
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