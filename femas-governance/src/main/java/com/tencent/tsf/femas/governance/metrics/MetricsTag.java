package com.tencent.tsf.femas.governance.metrics;

/**
 * @Author p_mtluo
 * @Date 2021-08-17 15:04
 * @Description metrics上报数据的tag列表
 **/
public class MetricsTag {

    private static final String INSTANCE_ID = "local.instance.id";
    private static final String LOCAL_SERVICE = "local.service";
    private static final String LOCAL_INTERFACE = "local.interface";
    private static final String LOCAL_HOST = "local.host";
    private static final String LOCAL_PORT = "local.port";
    private static final String LOCAL_VERSION = "local.version";
    private static final String LOCAL_NAMESPACE = "local.namespace";
    private static final String REMOTE_SERVICE = "remote.service";
    private static final String REMOTE_INTERFACE = "remote.interface";
    private static final String REMOTE_HOST = "remote.host";
    private static final String REMOTE_PORT = "remote.port";
    private static final String REMOTE_NAMESPACE = "remote.namespace";
    private static final String REMOTE_INSTANCE_ID = "remote.instance.id";
    private static final String REMOTE_VERSION = "remote.version";
    private static final String LOCAL_HTTP_METHOD = "local.http.method";
    private static final String REMOTE_HTTP_METHOD = "remote.http.method";
    private static final String HTTP_STATUS = "http.status";


    public static String getInstanceId() {
        return INSTANCE_ID;
    }

    public static String getLocalService() {
        return LOCAL_SERVICE;
    }

    public static String getLocalInterface() {
        return LOCAL_INTERFACE;
    }

    public static String getLocalHost() {
        return LOCAL_HOST;
    }

    public static String getLocalPort() {
        return LOCAL_PORT;
    }

    public static String getRemoteService() {
        return REMOTE_SERVICE;
    }

    public static String getRemoteInterface() {
        return REMOTE_INTERFACE;
    }

    public static String getRemoteHost() {
        return REMOTE_HOST;
    }

    public static String getRemotePort() {
        return REMOTE_PORT;
    }

    public static String getHttpStatus() {
        return HTTP_STATUS;
    }

    public static String getLocalHttpMethod() {
        return LOCAL_HTTP_METHOD;
    }

    public static String getRemoteHttpMethod() {
        return REMOTE_HTTP_METHOD;
    }

    public static String getLocalVersion() {
        return LOCAL_VERSION;
    }

    public static String getLocalNamespace() {
        return LOCAL_NAMESPACE;
    }

    public static String getRemoteNamespace() {
        return REMOTE_NAMESPACE;
    }

    public static String getRemoteInstanceId() {
        return REMOTE_INSTANCE_ID;
    }

    public static String getRemoteVersion() {
        return REMOTE_VERSION;
    }
}
