package com.tencent.tsf.femas.constant;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/4/16 17:18
 * @Version 1.0
 */
public class AdminConstants {

    public static final String CONTEXT_PATH_PREFIX = "femas/";
    public static final String REGISTRY_ID_PREFIX = "ins-";
    public static final String NAMESPACE_PATH_PREFIX = "ns/";
    public static final String NAMESPACE_ID_PREFIX = "ns-";
    public static final String DCFG_ID_PREFIX = "dcfg-";
    public static final String DCFGV_ID_PREFIX = "dcfgv-";
    public static final String SERVICE_ID_PREFIX = "ms-";
    public static final String HTTP_PREFIX = "http://";
    public static final String ROCKSDB_DATA_PATH = System.getProperty("user.home").concat("/rocksdb/femas/data/");
    public static final String ROCKSDB_COLUMN_FAMILY = "femas_config_column_family";
    public static final String REGISTRY_NAMESPACE_PREFIX = "r-n-";
    public static final String RECORD_LOG = "log-";
    public static final String DEFAULT_VERSION = "VERSION-UNKNOWN";
    public static final String DB_TYPE = "dbType";
    public static final String TRACE_SERVER_TYPE = "traceServerType";
    public static final String TRACE_SERVER_SKYWALKING = "skywalking";
    public static final String NAMESPACE_ID = "FEMAS_NAMESPACE_ID";
    public static final String TRACE_SERVER_BACKEND = "traceBackend";

    public static final String USERNAME = "admin";
    public static final String PASSWORD = "123456";

    public static final String EMBEDDED = "embedded";
    public static final String EXTERNAL = "external";
    public static final String ENV_MEMORY = "dbType";
    public static final String LOCAL_IP = "local.ip";
    public static final String APPLICATION_VERSION_KEY = "application.version";
    public static final String APPLICATION_VERSION = "1.2.0";


    public static final String FEMAS_BASE_PATH = "femas_base_path";

    public static final String FEMAS_META_APPLICATION_VERSION_KEY = "FEMAS_PROG_VERSION";

    public static final String FEMAS_META_NAMESPACE_ID_KEY = "FEMAS_NAMESPACE_ID";

    public static final String FEMAS_CLIENT_SDK_VERSION = "FEMAS_CLIENT_SDK_VERSION";

    public static final String FEMAS_META_K8S_KEY = "femas-service-metadata";

    public static final String FEMAS_K8S_SELECT_LABEL_KEY = "femas-service-app=";

    public static class StorageKeyPrefix {

        public static final String REGISTRY_CONFIG_PREFIX = CONTEXT_PATH_PREFIX.concat("registry/config/");

    }

    public static class OpenApiEndpoint {

        public static final String END_POINT_SUFFIX = "Endpoint";
    }


}
