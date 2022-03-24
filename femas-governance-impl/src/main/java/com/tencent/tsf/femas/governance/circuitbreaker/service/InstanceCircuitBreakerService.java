package com.tencent.tsf.femas.governance.circuitbreaker.service;

import com.google.common.collect.Sets;
import com.tencent.tsf.femas.common.entity.Request;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService;
import com.tencent.tsf.femas.governance.circuitbreaker.core.CircuitBreaker;
import com.tencent.tsf.femas.governance.circuitbreaker.core.CircuitBreakerFactory;
import com.tencent.tsf.femas.governance.circuitbreaker.core.StateTransitionCallback;
import com.tencent.tsf.femas.governance.circuitbreaker.core.internal.CircuitBreakerMetrics;
import com.tencent.tsf.femas.governance.circuitbreaker.rule.CircuitBreakerRule;
import com.tencent.tsf.femas.governance.circuitbreaker.rule.CircuitBreakerStrategy;
import com.tencent.tsf.femas.governance.plugin.context.ConfigContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService.State.convertState;

/**
 * @author zhixinzxliu
 */
public class InstanceCircuitBreakerService implements ICircuitBreakerService {

    private static final Logger logger = LoggerFactory.getLogger(InstanceCircuitBreakerService.class);
    // 每10分钟check一次是否有server被移除了
    private static final  long CHECK_INTERVAL = 10 * 60 * 1000;
    private CircuitBreakerStrategy strategy;
    private CircuitBreakerRule rule;
    private volatile Map<ServiceInstance, CircuitBreaker> instanceMap = new ConcurrentHashMap<>();
    // 熔断器打开状态的实例
    private volatile Set<ServiceInstance> openInstances = Sets.newConcurrentHashSet();
    private float maxEjectionPercent;
    private AtomicLong nextCheckTimestamp = new AtomicLong(
            System.currentTimeMillis() + CHECK_INTERVAL + new Random(this.hashCode()).nextInt(60 * 1000));

    private Lock lock = new ReentrantLock();

    protected InstanceCircuitBreakerService(CircuitBreakerRule rule) {
        this.rule = rule;
        this.strategy = rule.getStrategyList().get(0);
        this.maxEjectionPercent = this.strategy.getMaxEjectionPercent() / 100f;
    }

    /**
     * 实例级别熔断方式和其他两种级别不太一样，会在router选择服务实例时直接剔除掉
     *
     * @return
     */
    @Override
    public boolean tryAcquirePermission(Request request) {
        return true;
    }

    @Override
    public void handleSuccessfulServiceRequest(Request request, long responseTime) {
        ServiceInstance serviceInstance = request.getTargetServiceInstance();
        if (serviceInstance == null) {
            return;
        }

        CircuitBreaker circuitBreaker = checkInstanceExistAndReturn(serviceInstance);

        if (circuitBreaker != null) {
            circuitBreaker.onSuccess(responseTime, TimeUnit.MILLISECONDS);
        } else {
            logger.error("[FEMAS CIRCUIT BREAKER] Instance {} miss in circuit breaker map. Request : {}", serviceInstance,
                    request);
        }
    }

    @Override
    public void handleFailedServiceRequest(Request request, long responseTime, Throwable t) {
        ServiceInstance serviceInstance = request.getTargetServiceInstance();
        if (serviceInstance == null) {
            return;
        }

        CircuitBreaker circuitBreaker = checkInstanceExistAndReturn(serviceInstance);

        if (circuitBreaker != null) {
            circuitBreaker.onError(responseTime, TimeUnit.MILLISECONDS, t);
        } else {
            logger.error("[FEMAS CIRCUIT BREAKER] Instance {} miss in circuit breaker map. Request : {}", serviceInstance,
                    request);
        }
    }

    private CircuitBreaker checkInstanceExistAndReturn(ServiceInstance instance) {
        // 主流程不需要加锁，即使server被remove了，最多该server对应的cb多调用一次，问题不大。
        CircuitBreaker circuitBreaker = instanceMap.get(instance);

        if (circuitBreaker == null) {
            lock.lock();

            try {
                // double check
                if (instanceMap.containsKey(instance)) {
                    return instanceMap.get(instance);
                }

                // 实例级别的熔断器需要自动从OPEN状态切换
                CircuitBreaker tmp = CircuitBreakerFactory.newCircuitBreaker(
                        instance.toString(), rule, strategy, true);
                tmp.setCircuitBreakerTargetObject(instance);
                tmp.registerCallback(new StateTransitionCallback() {
                    @Override
                    public void onTransition(ICircuitBreakerService.State from, ICircuitBreakerService.State to,
                            Object circuitBreakerObject, CircuitBreakerMetrics metrics, CircuitBreaker circuitBreaker) {
                        if (to == ICircuitBreakerService.State.OPEN) {
                            moveToOpen((ServiceInstance) circuitBreakerObject, circuitBreaker);
                        }

                        if (from == ICircuitBreakerService.State.OPEN) {
                            removeOpenInstance((ServiceInstance) circuitBreakerObject);
                        }
                    }
                });
                
                instanceMap.putIfAbsent(instance, tmp);

                return tmp;
            } catch (Exception e) {
                logger.error("[FEMAS CIRCUIT BREAKER ERROR] Instance " + instance + "'s circuit breaker construct filed.",
                        e);
                return null;
            } finally {
                lock.unlock();
            }
        } else {
            return circuitBreaker;
        }
    }

    public void moveToOpen(ServiceInstance serviceInstance, CircuitBreaker circuitBreaker) {
        if (serviceInstance == null) {
            return;
        }

        lock.lock();
        try {
            float ejectionPercent = (openInstances.size() + 1.0f) / instanceMap.size();
            if (ejectionPercent > maxEjectionPercent) {
                logger.warn(
                        "[FEMAS CIRCUIT BREAKER] {} over maxEjectionPercent:{}, id = {}, current openInstance size: {}, current instance size: {}",
                        ejectionPercent,
                        maxEjectionPercent,
                        circuitBreaker.getName(),
                        openInstances.size(),
                        instanceMap.size());
            } else {
                logger.info(
                        "[FEMAS CIRCUIT BREAKER] Instance id = {} was unavailable, move to open instances. Current ejectionPercent:{}",
                        circuitBreaker.getName(), ejectionPercent);
                openInstances.add(serviceInstance);
            }
        } catch (Exception e) {

        } finally {
            lock.unlock();
        }
    }

    public void removeOpenInstance(ServiceInstance serviceInstance) {
        lock.lock();
        try {
            if (serviceInstance != null && openInstances.contains(serviceInstance)) {
                logger.info("[FEMAS CIRCUIT BREAKER] Instance = {} move to half-open instances.", serviceInstance);
                openInstances.remove(serviceInstance);
            }
        } catch (Exception e) {

        } finally {
            lock.unlock();
        }
    }

    @Override
    public Set<ServiceInstance> getOpenInstances(Request request) {
        return Collections.unmodifiableSet(openInstances);
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

    /**
     * 实现层的状态转化成标准层的状态
     *
     * @param request
     * @return
     */
    @Override
    public State getState(Request request) {
        ServiceInstance serviceInstance = request.getTargetServiceInstance();
        if (serviceInstance == null) {
            return State.UNREGISTERED;
        }

        CircuitBreaker circuitBreaker = instanceMap.get(serviceInstance);
        if (circuitBreaker != null) {
            return convertState(circuitBreaker.getState().name());
        }

        return State.UNREGISTERED;
    }
}
