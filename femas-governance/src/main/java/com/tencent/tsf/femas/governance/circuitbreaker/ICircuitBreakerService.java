package com.tencent.tsf.femas.governance.circuitbreaker;

import com.tencent.tsf.femas.common.entity.Request;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.governance.plugin.Plugin;
import java.util.List;
import java.util.Set;

/**
 * @author zhixinzxliu
 */
public interface ICircuitBreakerService<T> extends Plugin {

    /**
     * tryAcquirePermission 方法不需要访问的实例信息
     * 如果是实例级别熔断，会在loadbalancer侧剔除相关服务实例
     *
     * @return
     */
    boolean tryAcquirePermission(Request request);

    void handleSuccessfulServiceRequest(Request request, long responseTime);

    void handleFailedServiceRequest(Request request, long responseTime, Throwable t);

    Set<ServiceInstance> getOpenInstances(Request request);

    default boolean updateCircuitBreakerRule(T rule) {
        return true;
    }

    default void updateCircuitBreakerRule(List<T> rules) {
    }

    default void disableCircuitBreaker(Service service) {
    }

    default FemasCircuitBreakerIsolationLevelEnum getServiceCircuitIsolationLevel(Service service) {
        return FemasCircuitBreakerIsolationLevelEnum.INSTANCE;
    }


    State getState(Request request);

    enum State {
        /**
         * A DISABLED breaker is not operating (no state transition, no events) and allowing all
         * requests through.
         */
        DISABLED(3, false),
        /**
         * A METRICS_ONLY breaker is collecting metrics, publishing events and allowing all requests
         * through but is not transitioning to other states.
         */
        METRICS_ONLY(5, true),
        /**
         * A CLOSED breaker is operating normally and allowing requests through.
         */
        CLOSED(0, true),
        /**
         * An OPEN breaker has tripped and will not allow requests through.
         */
        OPEN(1, true),
        /**
         * A FORCED_OPEN breaker is not operating (no state transition, no events) and not allowing
         * any requests through.
         */
        FORCED_OPEN(4, false),
        /**
         * A HALF_OPEN breaker has completed its wait interval and will allow requests
         */
        HALF_OPEN(2, true),

        UNREGISTERED(-1, true);

        public final boolean allowPublish;
        private final int order;

        /**
         * Order is a FIXED integer, it should be preserved regardless of the ordinal number of the
         * enumeration. While a State.ordinal() does mostly the same, it is prone to changing the
         * order based on how the programmer  sets the enum. If more states are added the "order"
         * should be preserved. For example, if there is a state inserted between CLOSED and
         * HALF_OPEN (say FIXED_OPEN) then the order of HALF_OPEN remains at 2 and the new state
         * takes 3 regardless of its order in the enum.
         *
         * @param order
         * @param allowPublish
         */
        State(int order, boolean allowPublish) {
            this.order = order;
            this.allowPublish = allowPublish;
        }

        public static State convertState(String name) {
            for (State state : State.values()) {
                if (state.name().equalsIgnoreCase(name)) {
                    return state;
                }
            }
            return null;
        }

        public int getOrder() {
            return order;
        }
    }
}
