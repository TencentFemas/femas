package com.tencent.tsf.femas.api;

import com.tencent.tsf.femas.common.RegistryConstants;
import com.tencent.tsf.femas.common.annotation.AdaptorComponent;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.RpcContext;
import com.tencent.tsf.femas.common.context.TracingContext;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.discovery.DiscoveryService;
import com.tencent.tsf.femas.common.discovery.ServiceDiscoveryClient;
import com.tencent.tsf.femas.common.discovery.ServiceNotifyListener;
import com.tencent.tsf.femas.common.entity.ErrorStatus;
import com.tencent.tsf.femas.common.entity.Request;
import com.tencent.tsf.femas.common.entity.Response;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.header.AbstractRequestMetaUtils;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistryMetadata;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistryMetadataFactory;
import com.tencent.tsf.femas.common.serviceregistry.RegistryService;
import com.tencent.tsf.femas.common.serviceregistry.ServiceRegistry;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.common.util.TimeUtil;
import com.tencent.tsf.femas.config.AbstractConfigHttpClientManager;
import com.tencent.tsf.femas.config.AbstractConfigHttpClientManagerFactory;
import com.tencent.tsf.femas.governance.auth.AuthorizationManager;
import com.tencent.tsf.femas.governance.circuitbreaker.FemasCircuitBreakerIsolationLevelEnum;
import com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService;
import com.tencent.tsf.femas.governance.config.FemasPluginContext;
import com.tencent.tsf.femas.governance.lane.LaneService;
import com.tencent.tsf.femas.governance.loadbalance.LoadbalancerManager;
import com.tencent.tsf.femas.governance.loadbalance.exception.FemasNoAvailableInstanceException;
import com.tencent.tsf.femas.governance.metrics.Counter;
import com.tencent.tsf.femas.governance.metrics.IMeterRegistry;
import com.tencent.tsf.femas.governance.metrics.MetricsConstant;
import com.tencent.tsf.femas.governance.metrics.Timer;
import com.tencent.tsf.femas.governance.plugin.config.ConfigHandlerUtils;
import com.tencent.tsf.femas.governance.plugin.config.enums.ConfigHandlerTypeEnum;
import com.tencent.tsf.femas.governance.ratelimit.RateLimiterManager;
import com.tencent.tsf.femas.governance.route.RouterManager;
import com.tencent.tsf.femas.governance.trace.TraceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.tencent.tsf.femas.common.RegistryConstants.REGISTRY_HOST;
import static com.tencent.tsf.femas.common.RegistryConstants.REGISTRY_PORT;

@AdaptorComponent
public class CommonExtensionLayer implements IExtensionLayer {

    private static Logger logger = LoggerFactory.getLogger(CommonExtensionLayer.class);

    private static ICircuitBreakerService circuitBreakerService = FemasPluginContext.getCircuitBreakers().get(0);

    private static IMeterRegistry meterRegistry = FemasPluginContext.getMeterRegistry().get(0);

    private AbstractConfigHttpClientManager manager = AbstractConfigHttpClientManagerFactory
            .getConfigHttpClientManager();

    private volatile static Context commonContext = ContextFactory.getContextInstance();
    private volatile static ContextConstant contextConstant = ContextFactory.getContextConstantInstance();
    private String namespace = Context.getSystemTag(contextConstant.getNamespaceId());

    private volatile AbstractServiceRegistryMetadata serviceRegistryMetadata = AbstractServiceRegistryMetadataFactory
            .getServiceRegistryMetadata();

    private volatile ServiceRegistry serviceRegistry;
    private volatile ServiceDiscoveryClient serviceDiscoveryClient;

    /**
     * 处理某个服务的初始化工作
     * 包括但不限于 Context，各个治理组件，Config等
     *
     * @param service
     */
    public void init(Service service, Integer port) {
        String registryUrl = commonContext.getRegistryConfigMap().get(REGISTRY_HOST)
                + ":" + commonContext.getRegistryConfigMap().get(REGISTRY_PORT);
        this.init(service, port, registryUrl);
    }

    public void init(Service service, Integer port, String registryUrl) {
        commonContext.init(service.getName(), port);
        manager.initNamespace(registryUrl, service.getNamespace());

        /**
         * 初始化Auth逻辑
         */
        ConfigHandlerUtils.subscribeServiceConfigFromMap(ConfigHandlerTypeEnum.AUTH.getType(), service);

        /**
         * 初始化熔断逻辑
         */
        ConfigHandlerUtils.subscribeServiceConfigFromMap(ConfigHandlerTypeEnum.CIRCUIT_BREAKER.getType(), service);

        /**
         * 初始化限流逻辑
         */
        ConfigHandlerUtils.subscribeServiceConfigFromMap(ConfigHandlerTypeEnum.RATE_LIMITER.getType(), service);

        /**
         * 初始化泳道
         */
        ConfigHandlerUtils.subscribeServiceConfigFromMap(ConfigHandlerTypeEnum.LANE.getType(), service);

        /**
         * 增加监听路由规则
         */
        ConfigHandlerUtils.subscribeServiceConfigFromMap(ConfigHandlerTypeEnum.ROUTER.getType(), service);

        /**
         * 初始化LB
         */
        ConfigHandlerUtils.subscribeServiceConfigFromMap(ConfigHandlerTypeEnum.LOAD_BALANCER.getType(), service);

        /**
         * 初始化监控
         */
        ConfigHandlerUtils.subscribeServiceConfigFromMap(ConfigHandlerTypeEnum.METRICS_EXPORTER.getType(), service);
        ConfigHandlerUtils.subscribeServiceConfigFromMap(ConfigHandlerTypeEnum.METRICS_TRANSFORMER.getType(), service);
    }

    @Override
    public void destroy(Service service) {

    }

    @Override
    public void register(ServiceInstance instance) {
        //需要注册时才初始化，spring cloud 可以使用原生的 register
        initRegistry();
        Map<String, String> instanceMetadata = instance.getAllMetadata();
        Map<String, String> registerMetadata = serviceRegistryMetadata.getRegisterMetadataMap();
        if (instanceMetadata == null) {
            instance.setAllMetadata(registerMetadata);
        } else {
            instanceMetadata.putAll(registerMetadata);
        }
        serviceRegistry.register(instance);
    }

    private void initRegistry() {
        if (serviceRegistry == null) {
            synchronized (this) {
                if (serviceRegistry == null) {
                    String registryType = commonContext.getRegistryConfigMap().get(RegistryConstants.REGISTRY_TYPE);
                    serviceRegistry = RegistryService
                            .createRegistry(registryType, commonContext.getRegistryConfigMap());
                    serviceDiscoveryClient = DiscoveryService
                            .createDiscoveryClient(registryType, commonContext.getRegistryConfigMap());
                }
            }
        }
    }

    @Override
    public void deregister(ServiceInstance instance) {
        serviceRegistry.deregister(instance);
    }

    @Override
    public List<ServiceInstance> subscribe(Service service, List<ServiceNotifyListener> listeners) {
        serviceDiscoveryClient.subscribe(service);
        if (!CollectionUtil.isEmpty(listeners)) {
            for (ServiceNotifyListener serviceNotifyListener : listeners) {
                serviceDiscoveryClient.addNotifyListener(serviceNotifyListener);
            }
        }
        return serviceDiscoveryClient.getInstances(service);
    }

    /**
     * ns 隔离：不支持 ns 的注册中心，通过 meta 信息进行 sdk 过滤,后续如果要支持跨 ns 的, 这里在需要处理,
     * nacos 等原本就支持 ns 的注册中心, 也进行改逻辑处理
     */
    public ServiceInstance chooseServiceInstance(Request request, List<ServiceInstance> instances) {
        List<ServiceInstance> nsFileterInstances = new ArrayList<>(instances.size());
        for (ServiceInstance serviceInstance : instances) {
            // 当前有 ns，被调服务也需要有
            if (StringUtils.isNotEmpty(namespace)) {
                if (serviceInstance.getService() != null && namespace
                        .equals(serviceInstance.getService().getNamespace())) {
                    nsFileterInstances.add(serviceInstance);
                }
            } else {
                // 当前没有 ns，被调服务也不能有
                if (serviceInstance.getService() == null || StringUtils
                        .isEmpty(serviceInstance.getService().getNamespace())) {
                    nsFileterInstances.add(serviceInstance);
                }
            }
        }

        /**
         * 剔除熔断状态处于打开的实例
         */
        Set<ServiceInstance> openInstances = circuitBreakerService.getOpenInstances(request);
        if (!CollectionUtil.isEmpty(openInstances)) {
            nsFileterInstances.removeAll(openInstances);
        }

        /**
         * 泳道实例过滤
         */
        List<ServiceInstance> filteredLaneProviders = LaneService
                .filterInstancesWithLane(request.getTargetService(), nsFileterInstances);

        /**
         * 路由
         */
        List<ServiceInstance> routeInstances = (List<ServiceInstance>) RouterManager
                .route(request.getTargetService(), filteredLaneProviders);

        /**
         * load-balance
         */
        ServiceInstance instance = null;
        try {
            instance = LoadbalancerManager.select(routeInstances);
        } catch (FemasNoAvailableInstanceException e) {
            logger.warn("error in lb select, ", e);
            return null;
        }
        request.setTargetServiceInstance(instance);

        return instance;
    }

    @Override
    public List<ServiceInstance> getInstance(String serviceName, String namespace) {
        return serviceDiscoveryClient.getInstances(new Service(namespace, serviceName));
    }

    @Override
    public List<String> getAllServices() {
        return serviceDiscoveryClient.getAllServices();
    }

    @Override
    public RpcContext beforeServerInvoke(Request request,
                                         AbstractRequestMetaUtils headerUtils) {
        //重置上下文
        commonContext.reset();

        // 解析 header
        commonContext.getSerializeTagsFromRequestMeta(headerUtils);

        headerUtils.getUniqueInfo();

        RpcContext rpcContext = new RpcContext();
        TracingContext tracingContext = new TracingContext();
        tracingContext.setStartTime(System.currentTimeMillis());
        rpcContext.setTracingContext(tracingContext);
        /**
         * 授权
         */
        if (!AuthorizationManager.authenticate(request.getTargetService())) {
            rpcContext.setErrorStatus(ErrorStatus.UNAUTHENTICATED);
            return rpcContext;
        }
        /**
         * 限流
         */
        if (!RateLimiterManager.acquire(request.getTargetService())) {
            rpcContext.setErrorStatus(ErrorStatus.RESOURCE_EXHAUSTED);
            return rpcContext;
        }
        return rpcContext;
    }

    @Override
    public void afterServerInvoke(Response response, RpcContext rpcContext) {
        ErrorStatus statusCode = ErrorStatus.OK;
        if (response.hasError()) {
            statusCode = ErrorStatus.INTERNAL;
        }
        // trace, metrics finish
        TraceAdapter.setSpanAttribute(rpcContext);
        ((Timer) meterRegistry.timer(MetricsConstant.FEMAS_HTTP_SERVER_REQUESTS,
                meterRegistry.buildTags(response, rpcContext, statusCode)))
                .record(System.currentTimeMillis() - rpcContext.getTracingContext().getStartTime(),
                        TimeUnit.MILLISECONDS);
        // 限流统计
        if (ErrorStatus.RESOURCE_EXHAUSTED.equals(rpcContext.getErrorStatus())) {
            ((Counter) meterRegistry.counter(MetricsConstant.FEMAS_HTTP_RATELIMIT_COUNT,
                    meterRegistry.buildTags(response, rpcContext, ErrorStatus.RESOURCE_EXHAUSTED))).increment();
        }
    }

    @Override
    public RpcContext beforeClientInvoke(Request request,
                                         AbstractRequestMetaUtils headerUtils) {
        RpcContext rpcContext = new RpcContext();
        TracingContext tracingContext = new TracingContext();
        tracingContext.setStartTime(System.currentTimeMillis());
        rpcContext.setTracingContext(tracingContext);

        // 设置 header
        headerUtils.preprocess();
        // 序列化 tags
        headerUtils.setRequestMetas(commonContext.getRequestMetaSerializeTags());

        // 如果已经选择路由实例，需要进行熔断级别判断
        if (request.isDoneChooseInstance() && !circuitBreakerService.tryAcquirePermission(request)) {
            FemasCircuitBreakerIsolationLevelEnum isolationLevel = circuitBreakerService
                    .getServiceCircuitIsolationLevel(request.getTargetService());
            rpcContext.setErrorStatus(new ErrorStatus(ErrorStatus.Code.CIRCUIT_BREAKER, isolationLevel.name()));
        }
        return rpcContext;
    }

    @Override
    public void afterClientInvoke(Request request, Response response, RpcContext rpcContext) {
        long duration = TimeUtil.currentTimeMillis() - rpcContext.getTracingContext().getStartTime();
        // actual status from response
        ErrorStatus statusCode = response.getErrorStatus() != null ? response.getErrorStatus() : ErrorStatus.OK;

        if (response.getError() != null) {
            circuitBreakerService.handleFailedServiceRequest(request, duration, response.getError());
        } else {
            circuitBreakerService.handleSuccessfulServiceRequest(request, duration);
        }

        // trace, metrics finish
        TraceAdapter.setSpanAttribute(rpcContext);
        ((Timer) meterRegistry.timer(MetricsConstant.FEMAS_HTTP_CLIENT_REQUESTS,
                meterRegistry.buildTags(request, response, rpcContext, statusCode)))
                .record(System.currentTimeMillis() - rpcContext.getTracingContext().getStartTime(),
                        TimeUnit.MILLISECONDS);
    }

    @Override
    public Context getCommonContext() {
        return commonContext;
    }
}