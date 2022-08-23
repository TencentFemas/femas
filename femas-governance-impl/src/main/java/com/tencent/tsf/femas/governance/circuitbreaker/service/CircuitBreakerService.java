package com.tencent.tsf.femas.governance.circuitbreaker.service;

import com.tencent.tsf.femas.common.entity.Request;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.governance.circuitbreaker.FemasCircuitBreakerIsolationLevelEnum;
import com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService;
import com.tencent.tsf.femas.plugin.PluginDefinitionReader;
import com.tencent.tsf.femas.plugin.context.ConfigContext;
import com.tencent.tsf.femas.plugin.impl.config.CircuitBreakerConfigImpl;
import com.tencent.tsf.femas.plugin.impl.config.rule.circuitbreaker.CircuitBreakerRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 熔断逻辑
 * <p>
 *
 * @author zhixinzxliu
 */
public class CircuitBreakerService implements ICircuitBreakerService<CircuitBreakerRule> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CircuitBreakerService.class);



    /**
     * 单例，不考虑序列化问题
     *
     * @return CircuitBreakerService
     */

    private Map<Service, ServiceCircuitBreakerService> serviceCircuitBreakerServiceMap = new ConcurrentHashMap<>();

    @Override
    public void updateCircuitBreakerRule(List<CircuitBreakerRule> rules) {
        if (rules == null) {
            return;
        }

        for (CircuitBreakerRule rule : rules) {
            updateCircuitBreakerRule(rule);
        }
    }

    @Override
    public boolean updateCircuitBreakerRule(CircuitBreakerRule rule) {
        if (rule == null) {
            return false;
        }

        if (!rule.validate()) {
            LOGGER.info("[FEMAS CIRCUIT BREAKER] CircuitBreakerRule validate failed. rule={}", rule.toString());
            return false;
        }

        Service targetService = rule.getTargetService();

        /**
         * 更新熔断规则
         */
        serviceCircuitBreakerServiceMap.put(targetService, new ServiceCircuitBreakerService(rule));

        LOGGER.info("[FEMAS CIRCUIT BREAKER] CircuitBreakerRule {} update successful.", rule);
        return true;
    }


    @Override
    public boolean tryAcquirePermission(Request request) {
        Service service = request.getTargetService();
        if (service == null) {
            return true;
        }

        ICircuitBreakerService circuitBreakerService = serviceCircuitBreakerServiceMap.get(service);
        if (circuitBreakerService == null) {
            LOGGER.debug("[FEMAS CIRCUIT BREAKER DEBUG] CircuitBreaker Request {} is not exist.", request);
            return true;
        } else {
            return circuitBreakerService.tryAcquirePermission(request);
        }
    }

    @Override
    public void handleSuccessfulServiceRequest(Request request, long responseTime) {
        Service service = request.getTargetService();
        if (service == null) {
            return;
        }

        ICircuitBreakerService circuitBreakerService = serviceCircuitBreakerServiceMap.get(service);
        if (circuitBreakerService != null) {
            circuitBreakerService.handleSuccessfulServiceRequest(request, responseTime);
        }
    }

    @Override
    public void handleFailedServiceRequest(Request request, long responseTime, Throwable t) {
        Service service = request.getTargetService();
        if (service == null) {
            return;
        }

        ICircuitBreakerService circuitBreakerService = serviceCircuitBreakerServiceMap.get(service);
        if (circuitBreakerService != null) {
            circuitBreakerService.handleFailedServiceRequest(request, responseTime, t);
        }
    }

    @Override
    public Set<ServiceInstance> getOpenInstances(Request request) {
        Service service = request.getTargetService();
        if (service == null || !serviceCircuitBreakerServiceMap.containsKey(service)) {
            return null;
        }

        ICircuitBreakerService circuitBreakerService = serviceCircuitBreakerServiceMap.get(service);
        return circuitBreakerService.getOpenInstances(request);
    }

    @Override
    public ICircuitBreakerService.State getState(Request request) {
        Service service = request.getTargetService();
        if (service == null || !serviceCircuitBreakerServiceMap.containsKey(service)) {
            return ICircuitBreakerService.State.UNREGISTERED;
        }

        ICircuitBreakerService circuitBreakerService = serviceCircuitBreakerServiceMap.get(service);
        return circuitBreakerService.getState(request);
    }

    @Override
    public FemasCircuitBreakerIsolationLevelEnum getServiceCircuitIsolationLevel(Service service) {
        if (service == null) {
            return FemasCircuitBreakerIsolationLevelEnum.SERVICE;
        }

        ServiceCircuitBreakerService serviceCircuitBreakerService = serviceCircuitBreakerServiceMap.get(service);
        if (serviceCircuitBreakerService != null) {
            return serviceCircuitBreakerService.getIsolationLevel();
        }

        return FemasCircuitBreakerIsolationLevelEnum.SERVICE;
    }

    @Override
    public void disableCircuitBreaker(Service service) {
        if (service == null) {
            return;
        }

        serviceCircuitBreakerServiceMap.remove(service);
        LOGGER.info("Remove CIrcuit-Breaker rule. Service : " + service);
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public void init(ConfigContext conf) throws FemasRuntimeException {
        CircuitBreakerConfigImpl circuitBreakerConfig = (CircuitBreakerConfigImpl) conf.getConfig().getCircuitBreaker();
        if(circuitBreakerConfig == null || circuitBreakerConfig.getCircuitBreakerRule() == null){
            return;
        }
        Service service = new Service();
        PluginDefinitionReader pluginDefinitionReader = new PluginDefinitionReader();
        service.setName(pluginDefinitionReader.getProperty("serviceName") + "");
        service.setNamespace(System.getProperty("femas_namespace_id"));
        List<CircuitBreakerRule> circuitBreakerRule = circuitBreakerConfig.getCircuitBreakerRule();
        try {
            updateCircuitBreakerRule(circuitBreakerRule);
        } catch (Exception e){
            throw new FemasRuntimeException("circuit breaker init refresh error");
        }
        LOGGER.info("init circuit breaker rule: {}", circuitBreakerRule.toString());
    }

    @Override
    public String getName() {
        return "femasCircuitBreaker";
    }

    @Override
    public void destroy() {

    }

    /**
     * 删除全部规则，慎用
     */
    public void disableAllCircuitBreaker() {
        serviceCircuitBreakerServiceMap.clear();
    }
}
