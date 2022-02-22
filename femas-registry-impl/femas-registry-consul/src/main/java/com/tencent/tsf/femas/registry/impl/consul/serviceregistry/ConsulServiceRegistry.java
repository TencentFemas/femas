package com.tencent.tsf.femas.registry.impl.consul.serviceregistry;

import com.ecwid.consul.ConsulException;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.ConsulRawClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.health.HealthChecksForServiceRequest;
import com.ecwid.consul.v1.health.model.Check;
import com.tencent.tsf.femas.common.entity.EndpointStatus;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistry;
import com.tencent.tsf.femas.registry.impl.consul.ConsulConstants;
import com.tencent.tsf.femas.registry.impl.consul.config.ConsulConfig;
import com.tencent.tsf.femas.registry.impl.consul.config.ConsulHealthCheckConfig;
import com.tencent.tsf.femas.registry.impl.consul.util.NormalizeUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author zhixinzxliu
 */
public class ConsulServiceRegistry extends AbstractServiceRegistry {

    static final int DEFAULT_MAX_CONNECTIONS = 1000;
    static final int DEFAULT_MAX_PER_ROUTE_CONNECTIONS = 500;
    static final int DEFAULT_CONNECTION_TIMEOUT = 3000; // 3 sec
    // 10 minutes for read timeout due to blocking queries timeout
    // https://www.consul.io/api/index.html#blocking-queries
    static final int DEFAULT_READ_TIMEOUT = 2000; // 2 sec
    private static final Logger logger = LoggerFactory.getLogger(ConsulServiceRegistry.class);
    private final ConsulClient client;
    private final ConsulConfig consulConfig;
    private final HeartbeatScheduler heartbeatScheduler;

    public ConsulServiceRegistry(Map<String, String> configMap) {
        this.consulConfig = new ConsulConfig(configMap);
        this.client = buildConsulClient(this.consulConfig);
        this.heartbeatScheduler = new HeartbeatScheduler(this.client, consulConfig, this);
    }

    public static List<String> createTags(Map<String, String> tags) {
        List<String> tagList = new LinkedList<>();
        if (tags == null || tags.isEmpty()) {
            return tagList;
        }

        for (Map.Entry<String, String> entry : tags.entrySet()) {
            if (StringUtils.isNotEmpty(entry.getKey()) && StringUtils.isNotEmpty(entry.getValue())) {
                tagList.add(entry.getKey() + "=" + entry.getValue());
            }
        }

        return tagList;
    }

    private ConsulClient buildConsulClient(ConsulConfig consulConfig) {
        ConsulRawClient rawClient = new ConsulRawClient(consulConfig.getHost(), consulConfig.getPort(),
                getHttpClient());
        ConsulClient consulClient = new ConsulClient(rawClient);
        return consulClient;
    }

    private HttpClient getHttpClient() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(DEFAULT_MAX_CONNECTIONS);
        connectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_PER_ROUTE_CONNECTIONS);

        RequestConfig requestConfig = RequestConfig.custom().
                setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT).
                setConnectionRequestTimeout(DEFAULT_CONNECTION_TIMEOUT).
                setSocketTimeout(DEFAULT_READ_TIMEOUT).
                build();

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().
                setConnectionManager(connectionManager).
                setDefaultRequestConfig(requestConfig).
                useSystemProperties();

        return httpClientBuilder.build();
    }

    @Override
    protected void doRegister(ServiceInstance serviceInstance) {
        logger.info("Registering service with consul: " + serviceInstance);
        NewService consulService = buildService(serviceInstance);

        try {
            client.agentServiceRegister(consulService, this.consulConfig.getToken());
            if (heartbeatScheduler != null) {
                heartbeatScheduler.add(consulService);
            }
        } catch (ConsulException e) {
            if (this.consulConfig.isFailFast()) {
                logger.error("Error registering service with consul: " + serviceInstance, e);
                throw e;
            } else {
                // 添加心跳上报定时任务，在consul恢复时，可以通过心跳来触发服务注册
                if (heartbeatScheduler != null) {
                    heartbeatScheduler.add(consulService);
                }
            }

            logger.warn("FailFast is false. Error registering service with consul: " + serviceInstance, e);
        }

        logger.info("Service " + serviceInstance + " registered.");
    }

    @Override
    protected void doDeregister(ServiceInstance serviceInstance) {
        String instanceId = getInstanceId(serviceInstance);

        if (heartbeatScheduler != null) {
            heartbeatScheduler.remove(instanceId);
        }

        client.agentServiceDeregister(instanceId, this.consulConfig.getToken());
        logger.info("Deregister service with consul: " + instanceId + " success.");
    }

    public String getInstanceId(ServiceInstance serviceInstance) {
        return NormalizeUtil.normalizeForDns(serviceInstance.getId());
    }

    public NewService buildService(ServiceInstance serviceInstance) {

        NewService consulService = new NewService();

        // endpoint的id，不同上层业务系统自己设定。
        consulService.setId(getInstanceId(serviceInstance));

        // 设置host和port
        consulService.setAddress(serviceInstance.getHost());
        consulService.setPort(serviceInstance.getPort());

        // 设置服务名
        consulService.setName(serviceInstance.getService().getName());

        // 设置tags
        consulService.setTags(createTags(serviceInstance.getTags()));

        // Metadata 由业务粘合层设置给Endpoint
        if (serviceInstance.getAllMetadata() != null) {
            consulService.setMeta(serviceInstance.getAllMetadata());
        }

        consulService.setCheck(createCheck());

        return consulService;
    }

    public NewService.Check createCheck() {
        NewService.Check check = new NewService.Check();
        if (this.consulConfig.isEnableTtl()) {
            check.setTtl(this.consulConfig.getTtl() + ConsulConstants.CONSUL_TIME_UNIT);
            return check;
        }

        ConsulHealthCheckConfig healthCheckConfig = this.consulConfig.getHealthCheckConfig();
        if (StringUtils.isNotEmpty(healthCheckConfig.getHealthCheckUrl())) {
            check.setHttp(healthCheckConfig.getHealthCheckUrl());
            check.setInterval(healthCheckConfig.getHealthCheckInterval());
            check.setTimeout(healthCheckConfig.getHealthCheckTimeout());
            check.setDeregisterCriticalServiceAfter(healthCheckConfig.getHealthCheckCriticalTimeout());
            check.setTlsSkipVerify(healthCheckConfig.getHealthCheckTlsSkipVerify());
        }

        return check;
    }

    @Override
    public void setStatus(ServiceInstance serviceInstance, EndpointStatus status) {
        String instanceId = getInstanceId(serviceInstance);

        if (status == EndpointStatus.DOWN) {
            client.agentServiceSetMaintenance(instanceId, true);
        } else if (status == EndpointStatus.UP) {
            client.agentServiceSetMaintenance(instanceId, false);
        } else {
            throw new IllegalArgumentException("Unknown status: " + status);
        }

    }

    @Override
    public EndpointStatus getStatus(ServiceInstance serviceInstance) {
        String instanceId = getInstanceId(serviceInstance);

        HealthChecksForServiceRequest request = HealthChecksForServiceRequest.newBuilder()
                .setQueryParams(QueryParams.DEFAULT).build();
        Response<List<Check>> response = client
                .getHealthChecksForService(serviceInstance.getService().getName(), request);
        List<Check> checks = response.getValue();

        for (Check check : checks) {
            if (check.getServiceId().equals(instanceId)) {
                if (check.getName().equalsIgnoreCase("Service Maintenance Mode")) {
                    return EndpointStatus.DOWN;
                }
            }
        }

        return EndpointStatus.UP;
    }
}
