package com.tencent.tsf.femas.adaptor.paas.common;

import com.tencent.tsf.femas.common.context.ContextConstant;

public class FemasConstant extends ContextConstant {

    public static final String FEMAS_DEFAULT_SERVICE_NAME_PREFIX = "femas_mock_service_";

    /**
     * 在单进程多Service场景下使用
     */
    public static final String FEMAS_MULTI_SERVICE_NAME = "femas.multi.service.name";
    public static final String SOURCE_FEMAS_MULTI_SERVICE_NAME = "source.femas.multi.service.name";

    /**
     * header的system-tags key
     */
    public static final String FEMAS_API_METAS = "FEMAS_API_METAS";

    public static final String FEMAS_SERVICE_PORT = "service.port";

    public static final String FEMAS_INTERFACE = "interface";

    public static final String FEMAS_SERVICE_NAME = "service.name";
    public static final String FEMAS_SERVICE_NAME_KEY = "spring.application.name";

    public static final String FEMAS_META_INSTANCE_ID_KEY = "FEMAS_INSTANCE_ID";
    public static final String FEMAS_INSTANCE_ID = "instance.id";
    public static final String FEMAS_INSTANCE_ID_KEY = "femas_instance_id";

    public static final String FEMAS_META_ZONE_KEY = "FEMAS_ZONE";
    public static final String FEMAS_ZONE = "zone";
    public static final String FEMAS_ZONE_KEY = "femas_zone";

    public static final String FEMAS_META_NAMESPACE_ID_KEY = "FEMAS_NAMESPACE_ID";
    public static final String FEMAS_CLIENT_SDK_VERSION = "FEMAS_CLIENT_SDK_VERSION";

    public static final String FEMAS_CLIENT_SDK_VERSION_KEY = "femas.sdk.version";

    public static final String FEMAS_CLIENT_SDK_DEFAULT_VERSION = "2.0.0-RELEASE";

    public static final String FEMAS_NAMESPACE_ID = "namespace.id";
    public static final String FEMAS_NAMESPACE_ID_KEY = "femas_namespace_id";

    public static final String FEMAS_META_APPLICATION_ID_KEY = "FEMAS_APPLICATION_ID";
    public static final String FEMAS_APPLICATION_ID = "application.id";
    public static final String FEMAS_APPLICATION_ID_KEY = "femas_application_id";

    public static final String FEMAS_META_APPLICATION_VERSION_KEY = "FEMAS_PROG_VERSION";
    public static final String FEMAS_APPLICATION_VERSION = "application.version";
    public static final String FEMAS_APPLICATION_VERSION_KEY = "femas_prog_version";

    /**
     * 用户主账户ID
     */
    public static final String FEMAS_CONSUL_APP_ID = "FEMAS_APP_ID";
    public static final String FEMAS_APP_ID = "app.id";
    public static final String FEMAS_APP_ID_KEY = "femas_app_id";

    public static final String FEMAS_REGION = "region";
    public static final String FEMAS_META_REGION_KEY = "FEMAS_REGION";
    public static final String FEMAS_REGION_KEY = "femas_region";

    public static final String FEMAS_GROUP_ID = "group.id";
    public static final String FEMAS_META_GROUP_ID_KEY = "FEMAS_GROUP_ID";
    public static final String FEMAS_GROUP_ID_KEY = "femas_group_id";

    public static final String FEMAS_LOCAL_IP = "connection.ip";
    public static final String FEMAS_LOCAL_IP_KEY = "femas_local_ip";

    public static final String FEMAS_LOCAL_PORT = "connection.port";
    public static final String FEMAS_LOCAL_PORT_KEY = "femas_local_port";

    public static final String FEMAS_META_CLUSTER_ID_KEY = "FEMAS_CLUSTER_ID";
    public static final String FEMAS_CLUSTER_ID = "cluster.id";
    public static final String FEMAS_CLUSTER_ID_KEY = "femas_cluster_id";

    public static final String FEMAS_TOKEN_KEY = "femas_token";

    public static final String FEMAS_CONSUL_IP_KEY = "femas_consul_ip";

    public static final String FEMAS_REGISTRY_IP_KEY = "femas_registry_ip";

    public static final String FEMAS_REGISTRY_PORT_KEY = "femas_registry_port";

    public static final String FEMAS_REGISTRY_TYPE_KEY = "femas_registry_type";

    public static final String FEMAS_CONFIG_PAAS = "paas";

    /**
     * 请求发起方的应用 ID
     */
    public static final String SOURCE_APPLICATION_ID = "source.application.id";

    /**
     * 请求发起方的zone
     */
    public static final String SOURCE_ZONE = "source.zone";

    /**
     * 请求发起方的用户APPID
     */
    public static final String SOURCE_APP_ID = "source.app.id";

    /**
     * 请求发起方的REGION
     */
    public static final String SOURCE_REGION = "source.region";

    /**
     * 请求发起方的Cluster id
     */
    public static final String SOURCE_CLUSTER_ID = "source.cluster.id";

    /**
     * 请求发起方的应用版本号
     */
    public static final String SOURCE_APPLICATION_VERSION = "source.application.version";

    /**
     * 请求发起方的命名空间ID
     */
    public static final String SOURCE_NAMESPACE_ID = "source.namespace.id";

    /**
     * 请求发起方的实例 ID
     */
    public static final String SOURCE_INSTANCE_ID = "source.instance.id";

    /**
     * 请求发起方的部署组 ID
     */
    public static final String SOURCE_GROUP_ID = "source.group.id";

    /**
     * 请求发起方 IP
     */
    public static final String SOURCE_CONNECTION_IP = "source.connection.ip";

    /**
     * 请求发起方的服务名
     */
    public static final String SOURCE_SERVICE_NAME = "source.service.name";

    /**
     * 请求发起方的 Namespace/serviceName
     */
    public static final String SOURCE_NAMESPACE_SERVICE_NAME = "source.namespace.service.name";


    public static final String NAMESPACE_SERVICE_NAME = "namespace.service.name";

    /**
     * 请求发起方的服务 token，鉴权模块使用
     */
    public static final String SOURCE_SERVICE_TOKEN = "source.service.token";

    /**
     * 请求发起方被它的上游调用的接口（如果有）
     */
    public static final String SOURCE_INTERFACE = "source.interface";

    /**
     * 请求接收方的服务名
     */
    public static final String DESTINATION_SERVICE_NAME = "destination.service.name";

    /**
     * 请求接收方被调用的接口
     */
    public static final String DESTINATION_INTERFACE = "destination.interface";


    /**
     * 请求接收方的应用
     */
    public static final String DESTINATION_APPLICATION_ID = "destination.application.id";

    /**
     * 请求接收方被调用的接口
     */
    public static final String DESTINATION_APPLICATION_VERSION = "destination.application.version";

    /**
     * 请求接收方的部署组 ID
     */
    public static final String DESTINATION_GROUP_ID = "destination.group.id";

    /**
     * 请求接收方的命名空间ID
     */
    public static final String DESTINATION_NAMESPACE_ID = "destination.namespace.id";

    /**
     * 请求所使用的 HTTP 方法
     */
    public static final String REQUEST_HTTP_METHOD = "request.http.method";

    /**
     * 请求接收方的zone
     */
    public static final String DESTINATION_ZONE = "destination.zone";

    /**
     * 请求接收方的用户APPID
     */
    public static final String DESTINATION_APP_ID = "destination.app.id";

    /**
     * 请求接收方的REGIOn
     */
    public static final String DESTINATION_REGION = "destination.region";

    /**
     * 请求接收方的Cluster id
     */
    public static final String DESTINATION_CLUSTER_ID = "destination.cluster.id";

    public static final String CONSUL_ACCESS_TOKEN = "consulAccessToken";

    public String getInterface() {
        return FEMAS_INTERFACE;
    }

    public String getNamespaceId() {
        return FEMAS_NAMESPACE_ID;
    }

    public String getNamespaceIdKey() {
        return FEMAS_NAMESPACE_ID_KEY;
    }

    public String getDestinationInterface() {
        return DESTINATION_INTERFACE;
    }

    public String getDestinationServiceName() {
        return DESTINATION_SERVICE_NAME;
    }

    public String getDestinationNamespaceId() {
        return DESTINATION_NAMESPACE_ID;
    }

    public String getInstanceId() {
        return FEMAS_INSTANCE_ID;
    }

    public String getSourceInterface() {
        return SOURCE_INTERFACE;
    }

    public String getGroupId() {
        return FEMAS_GROUP_ID;
    }

    public String getApplicationVersion() {
        return FEMAS_APPLICATION_VERSION;
    }

    public String getMetaApplicationVersionKey() {
        return FEMAS_META_APPLICATION_VERSION_KEY;
    }

    public String getServiceName() {
        return FEMAS_SERVICE_NAME;
    }

    public String getApiMetas() {
        return FEMAS_API_METAS;
    }

    public String getRequestHttpMethod() {
        return REQUEST_HTTP_METHOD;
    }

    public String getLocalIp() {
        return FEMAS_LOCAL_IP;
    }

    public String getLocalPort() {
        return FEMAS_SERVICE_PORT;
    }

    public String getMetaInstanceIdKey() {
        return FEMAS_META_INSTANCE_ID_KEY;
    }

    public String getMetaGroupIdKey() {
        return FEMAS_META_GROUP_ID_KEY;
    }

    public String getMetaNamespaceIdKey() {
        return FEMAS_META_NAMESPACE_ID_KEY;
    }

    @Override
    public String getDefaultServerConnectorClient() {
        return "httpClient";
    }

    @Override
    public String getDefaultLoadBalancer() {
        return "random";
    }

    @Override
    public String getDefaultAuth() {
        return "femasAuthenticate";
    }

    @Override
    public String getDefaultMetrics() {
        return "microMeter";
    }

    @Override
    public String getDefaultMetricsExporter() {
        return "prometheus";
    }

    @Override
    public String getDefaultMetricsExporterAddr() {
        return "127.0.0.1:8080";
    }

    @Override
    public String getDefaultMetricsTransformer() {
        return "femas";
    }

    @Override
    public String getDefaultRateLimiter() {
        return "femasRateLimit";
    }

    @Override
    public String getDefaultCircuitBreaker() {
        return "femasCircuitBreaker";
    }

    @Override
    public String getDefaultLane() {
        return "mockLane";
    }
}
