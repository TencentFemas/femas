package com.tencent.tsf.femas.adaptor.paas.config;

import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.config.grpc.paas.*;
import com.tencent.tsf.femas.governance.connector.server.ServerConnectorManager;
import com.tencent.tsf.femas.plugin.config.global.ServerConnectorEnum;
import com.tencent.tsf.femas.plugin.context.ConfigContext;
import com.tencent.tsf.femas.plugin.impl.config.ServerConnectorConfigImpl;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class FemasConfigGrpcClientManager implements ServerConnectorManager {

    private static final Logger log = LoggerFactory.getLogger(FemasConfigGrpcClientManager.class);
    private volatile Context context = ContextFactory.getContextInstance();

    private ManagedChannel channel;

    public FemasConfigGrpcClientManager() {
        String host = GrpcHepler.getPaasServerHost();
        if(StringUtils.isNotEmpty(host)){
            int port = GrpcHepler.getPaasGrpcPort();
            channel = NettyChannelBuilder
                    .forAddress(host, port)
                    .usePlaintext()
                    .build();
        }else {
            log.warn("skipping initialization of grpc channel, because doesn't found the femas address configuration");
        }
    }

    @Override
    public String getType() {
        return ServerConnectorEnum.GRPC.name();
    }

    @Override
    public String getName() {
        return ServerConnectorEnum.GRPC.name();
    }

    @Override
    public void init(ConfigContext conf) throws FemasRuntimeException {
        ServerConnectorConfigImpl connectorConfig = (ServerConnectorConfigImpl) conf.getConfig().getGlobal().getServerConnector();
        if (connectorConfig == null || CollectionUtil.isEmpty(connectorConfig.getAddresses()) || StringUtils.isEmpty(connectorConfig.getProtocol())) {
            log.warn("server connector init failed,Lack of necessary configuration！！！");
            return;
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public void reportEvent(Service service, String eventId, String data) {
        if (context.isEmptyPaasServer()) {
            log.debug("reportEvent failed, could not find the paas address profile");
            return;
        }
        ReportEventRequest eventRequest = ReportEventRequest.newBuilder()
                .setNamespaceId(service.getNamespace())
                .setEventId(eventId)
                .setServiceName(service.getName())
                .setData(data)
                .build();
        try {
            PollingResult result = createStub().reportServiceEvent(eventRequest);
            boolean success = GrpcHepler.success(result);
            if (!success) {
                log.error("init namespace failed,message={}", result.getMessage());
            }
        } catch (Exception e) {
            log.error("init namespace failed", e);
        }

    }

    @Override
    public void reportApis(String namespaceId, String serviceName, String applicationVersion, String data) {
        if (context.isEmptyPaasServer()) {
            log.debug("reportApis failed ,could not find the paas address profile");
            return;
        }
        ServiceApiRequest serviceApiRequest = ServiceApiRequest.newBuilder()
                .setNamespaceId(namespaceId)
                .setServiceName(serviceName)
                .setApplicationVersion(applicationVersion)
                .setData(data).build();
        try {
            PollingResult pollingResult = createStub().reportServiceApi(serviceApiRequest);
            boolean success = GrpcHepler.success(pollingResult);
            if (!success) {
                log.error("reportApis failed,message={}", pollingResult.getMessage());
            }
        } catch (Exception e) {
            // 无配置 paas server 时，报错不打印
            log.warn("config http manager reportApis failed, msg:{}", e.getMessage());
        }
    }

    @Override
    public String fetchKVValue(String key, String namespaceId) {
        if (context.isEmptyPaasServer()) {
            log.debug("fetchKVValue failed , could not find the paas address profile");
            return null;
        }
        try {
            SimpleParam simpleParam = SimpleParam.newBuilder().setParam(key).build();
            PollingResult pollingResult = createStub().fetchBreakerRule(simpleParam);
            boolean success = GrpcHepler.success(pollingResult);
            if (!success) {
                log.error("config http manager fetchKVValue failed,message={}", pollingResult.getMessage());
            } else {
                return pollingResult.getResult();
            }
        } catch (Exception e) {
            log.error("config http manager fetchKVValue failed", e);
        }
        return null;
    }


    @Override
    public void initNamespace(String registryAddress, String namespaceId) {
        if (StringUtils.isEmpty(namespaceId)) {
            log.error("namespace is empty");
        }
        if (context.isEmptyPaasServer()) {
            log.debug("initNamespace failed , could not find the paas address profile");
            return;
        }
        InitNamespaceRequest initNamespaceRequest = InitNamespaceRequest.newBuilder()
                .setNamespaceId(namespaceId)
                .setRegistryAddress(registryAddress)
                .build();
        try {
            PollingResult pollingResult = createStub().initNamespace(initNamespaceRequest);
            boolean success = GrpcHepler.success(pollingResult);
            if (!success) {
                log.error("init namespace failed,message={}", pollingResult.getMessage());
            }
        } catch (Exception e) {
            log.error("init namespace failed, msg:{}", e.getMessage());
        }
    }

    private PaasPollingGrpc.PaasPollingBlockingStub createStub() {
        return PaasPollingGrpc.newBlockingStub(channel).withDeadlineAfter(10, TimeUnit.SECONDS);
    }
}
