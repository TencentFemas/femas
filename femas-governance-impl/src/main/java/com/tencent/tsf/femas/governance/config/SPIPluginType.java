package com.tencent.tsf.femas.governance.config;

import com.tencent.tsf.femas.governance.auth.IAuthentication;
import com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService;
import com.tencent.tsf.femas.governance.lane.LaneFilter;
import com.tencent.tsf.femas.governance.loadbalance.Loadbalancer;
import com.tencent.tsf.femas.governance.metrics.IMeterRegistry;
import com.tencent.tsf.femas.governance.metrics.MetricsExporter;
import com.tencent.tsf.femas.governance.metrics.MetricsTransformer;
import com.tencent.tsf.femas.governance.plugin.Plugin;
import com.tencent.tsf.femas.governance.ratelimit.RateLimiter;
import com.tencent.tsf.femas.governance.route.Router;

/**
 * The plugin types that our framework support.
 */
public enum SPIPluginType {

    /**
     * 鉴权扩展点
     */
    AUTH(IAuthentication.class, 7),

    /**
     * 熔断扩展点
     */
    CIRCUIT_BREAKER(ICircuitBreakerService.class, 0),

    /**
     * 泳道扩展点
     */
    LANE(LaneFilter.class, 6),

    /**
     * 服务路由扩展点
     */
    SERVICE_ROUTER(Router.class, 1),

    /**
     * 负载均衡扩展点
     */
    LOAD_BALANCER(Loadbalancer.class, 2),

    /**
     * metrics
     */
    METRICS(IMeterRegistry.class, 2),

    /**
     * MetricsExporter
     */
    METRICS_EXPORTER(MetricsExporter.class, 2),

    /**
     * MetricsTransformer
     */
    METRICS_TRANSFORMER(MetricsTransformer.class, 2),

    /**
     * 限流扩展点
     */
    RATE_LIMITER(RateLimiter.class, 3);

    Class<? extends Plugin> interfaces;

    /**
     * 初始化优先级
     */
    Integer initPriority;

    SPIPluginType(Class<? extends Plugin> interfaces, Integer initPriority) {
        this.interfaces = interfaces;
        this.initPriority = initPriority;
    }

    public Integer getInitPriority() {
        return initPriority;
    }

    public void setInitPriority(Integer initPriority) {
        this.initPriority = initPriority;
    }

    public Class<? extends Plugin> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(Class<? extends Plugin> interfaces) {
        this.interfaces = interfaces;
    }
}
