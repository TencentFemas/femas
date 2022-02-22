package com.tencent.tsf.femas.common.context;

/**
 * Context使用到的常量
 *
 * @author zhixinzxliu
 */
public abstract class ContextConstant {

    /**
     * header的system-tags key
     */
    public static final String USER_TAGS_HEADER_KEY = "_ut";

    public static final String SERVICE_NAME = "_sn";

    public static final String NAMESPACE_NAME = "_nn";

    public static final String UPSTREAM_ADDRESS_IP = "_uai";

    /**
     * header的user-tags key
     */
    public static final String SYSTEM_TAGS_HEADER_KEY = "_st";

    public static final String SOURCE = "source.";
    public static final String DESTINATION = "destination.";

    public static final String LANE_ID_TAG = "lane.id";

    public static final String TRACING_LOG_PATH_KEY = "trace.log.path";
    public static final String MONITOR_LOG_PATH_KEY = "monitor.log.path";

    public String getInterface() {
        throw new UnsupportedOperationException("ContextConstant method getInterface has no implementation");
    }

    public String getGroupId() {
        throw new UnsupportedOperationException("ContextConstant method getGroupId has no implementation");
    }

    public String getNamespaceId() {
        throw new UnsupportedOperationException("ContextConstant method getNamespaceId has no implementation");
    }

    public String getNamespaceIdKey() {
        throw new UnsupportedOperationException("ContextConstant method getNamespaceIdKey has no implementation");
    }

    public String getDestinationInterface() {
        throw new UnsupportedOperationException("ContextConstant method getDestinationInterface has no implementation");
    }

    public String getDestinationServiceName() {
        throw new UnsupportedOperationException(
                "ContextConstant method getDestinationServiceName has no implementation");
    }

    public String getDestinationNamespaceId() {
        throw new UnsupportedOperationException(
                "ContextConstant method getDestinationNamespaceId has no implementation");
    }

    public String getInstanceId() {
        throw new UnsupportedOperationException("ContextConstant method getInstanceId has no implementation");
    }

    public String getSourceInterface() {
        throw new UnsupportedOperationException("ContextConstant method getSourceInterface has no implementation");
    }

    public String getApplicationVersion() {
        throw new UnsupportedOperationException("ContextConstant method getApplicationVersion has no implementation");
    }

    public String getMetaApplicationVersionKey() {
        throw new UnsupportedOperationException("ContextConstant method applicationVersion has no implementation");
    }

    public String getServiceName() {
        throw new UnsupportedOperationException("ContextConstant method getServiceName has no implementation");
    }

    public String getApiMetas() {
        throw new UnsupportedOperationException("ContextConstant method getApiMetas has no implementation");
    }

    public String getRequestHttpMethod() {
        throw new UnsupportedOperationException("ContextConstant method getRequestHttpMethod has no implementation");
    }

    public String getLocalIp() {
        throw new UnsupportedOperationException("ContextConstant method getLocalIp has no implementation");
    }

    public String getLocalPort() {
        throw new UnsupportedOperationException("ContextConstant method getLocalPort has no implementation");
    }

    public String getMetaInstanceIdKey() {
        throw new UnsupportedOperationException("ContextConstant method getMetaInstanceIdKey has no implementation");
    }

    public String getMetaGroupIdKey() {
        throw new UnsupportedOperationException("ContextConstant method getMetaGroupIdKey has no implementation");
    }

    public String getMetaNamespaceIdKey() {
        throw new UnsupportedOperationException("ContextConstant method getMetaNamespaceIdKey has no implementation");
    }

    public String getMetaRegionKey() {
        throw new UnsupportedOperationException("ContextConstant method getMetaRegiondKey has no implementation");
    }

    public String getMetaZoneKey() {
        throw new UnsupportedOperationException("ContextConstant method getMetaRegiondKey has no implementation");
    }

    public String getDefaultServerConnectorClient() {
        throw new UnsupportedOperationException(
                "ContextConstant method getDefaultServerConnectorClient has no implementation");
    }

    public String getDefaultLoadBalancer() {
        throw new UnsupportedOperationException("ContextConstant method getDefaultLoadBalancer has no implementation");
    }

    public String getDefaultAuth() {
        throw new UnsupportedOperationException("ContextConstant method getDefaultAuth has no implementation");
    }

    public String getDefaultMetrics() {
        throw new UnsupportedOperationException("ContextConstant method getDefaultMetrics has no implementation");
    }

    public String getDefaultMetricsExporter() {
        throw new UnsupportedOperationException(
                "ContextConstant method getDefaultMetricsExporter has no implementation");
    }

    public String getDefaultMetricsExporterAddr() {
        throw new UnsupportedOperationException(
                "ContextConstant method getDefaultMetricsExporterAddr has no implementation");
    }

    public String getDefaultMetricsTransformer() {
        throw new UnsupportedOperationException(
                "ContextConstant method getDefaultMetricsTransformer has no implementation");
    }

    public String getDefaultRateLimiter() {
        throw new UnsupportedOperationException("ContextConstant method getDefaultRateLimiter has no implementation");
    }

    public String getDefaultCircuitBreaker() {
        throw new UnsupportedOperationException(
                "ContextConstant method getDefaultCircuitBreaker has no implementation");
    }

    public String getDefaultLane() {
        throw new UnsupportedOperationException("ContextConstant method getDefaultLane has no implementation");
    }

}
