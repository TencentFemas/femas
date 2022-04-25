package com.tencent.tsf.femas.common.context;

import com.tencent.tsf.femas.common.RegistryConstants;
import com.tencent.tsf.femas.common.constant.FemasConstant;
import com.tencent.tsf.femas.common.header.AbstractRequestMetaUtils;
import com.tencent.tsf.femas.common.util.AddressUtils;
import com.tencent.tsf.femas.common.util.GsonUtil;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.config.FemasConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.tencent.tsf.femas.config.impl.paas.PaasConstants.PAAS_SERVER_ADDRESS;


public class FemasContext extends Context {

    public static final String FEMAS_TAG_PLUGIN_HEADER_PREFIX = "femas-ut-";
    public static final String DEFAULT_NAMESPACE = "ns-default";
    private static final  Logger logger = LoggerFactory.getLogger(FemasContext.class);
    private static final  AtomicBoolean isLoggerPrinted = new AtomicBoolean(true);
    public static Map<String, String> REGISTRY_CONFIG_MAP = new ConcurrentHashMap<>();
    private static String TOKEN;
    private static volatile  String SERVICE_NAME;

    /**
     * 初始化，从环境变量中读取config值放入SYS_TAG
     */
    static {
        /**
         * SYS_TAG
         */
        String zone = FemasConfig.getProperty(FemasConstant.FEMAS_ZONE_KEY);
        if (!StringUtils.isEmpty(zone)) {
            SYSTEM_TAGS.put(FemasConstant.FEMAS_ZONE, zone);
        }

        String instanceId = FemasConfig.getProperty(FemasConstant.FEMAS_INSTANCE_ID_KEY);
        if (!StringUtils.isEmpty(instanceId)) {
            SYSTEM_TAGS.put(FemasConstant.FEMAS_INSTANCE_ID, instanceId);
        }

        String namespace = FemasConfig.getProperty(FemasConstant.FEMAS_NAMESPACE_ID_KEY);
        // 默认命名空间
        if (StringUtils.isEmpty(namespace)) {
            namespace = DEFAULT_NAMESPACE;
        }
        SYSTEM_TAGS.put(FemasConstant.FEMAS_NAMESPACE_ID, namespace);

        String applicationId = FemasConfig.getProperty(FemasConstant.FEMAS_APPLICATION_ID_KEY);
        if (!StringUtils.isEmpty(applicationId)) {
            SYSTEM_TAGS.put(FemasConstant.FEMAS_APPLICATION_ID, applicationId);
        }

        String applicationVersion = FemasConfig.getProperty(FemasConstant.FEMAS_APPLICATION_VERSION_KEY);
        if (!StringUtils.isEmpty(applicationVersion)) {
            SYSTEM_TAGS.put(FemasConstant.FEMAS_APPLICATION_VERSION, applicationVersion);
        }

        String appId = FemasConfig.getProperty(FemasConstant.FEMAS_APP_ID_KEY);
        if (!StringUtils.isEmpty(appId)) {
            SYSTEM_TAGS.put(FemasConstant.FEMAS_APP_ID, appId);
        }

        String region = FemasConfig.getProperty(FemasConstant.FEMAS_REGION_KEY);
        if (!StringUtils.isEmpty(region)) {
            SYSTEM_TAGS.put(FemasConstant.FEMAS_REGION, region);
        }

        String group = FemasConfig.getProperty(FemasConstant.FEMAS_GROUP_ID_KEY);
        if (!StringUtils.isEmpty(group)) {
            SYSTEM_TAGS.put(FemasConstant.FEMAS_GROUP_ID, group);
        }

        String clusterId = FemasConfig.getProperty(FemasConstant.FEMAS_CLUSTER_ID_KEY);
        if (!StringUtils.isEmpty(clusterId)) {
            SYSTEM_TAGS.put(FemasConstant.FEMAS_CLUSTER_ID, clusterId);
        }

        String ip = FemasConfig.getProperty(FemasConstant.FEMAS_LOCAL_IP_KEY);
        if (StringUtils.isEmpty(ip)) {
            ip = AddressUtils.getValidLocalHost();
        }
        SYSTEM_TAGS.put(FemasConstant.FEMAS_LOCAL_IP, ip);

        String port = FemasConfig.getProperty(FemasConstant.FEMAS_LOCAL_PORT_KEY);
        if (!StringUtils.isEmpty(port)) {
            SYSTEM_TAGS.put(FemasConstant.FEMAS_LOCAL_PORT, port);
        }

        /**
         * 后续按需求放入其他config
         */
        String registerHost = FemasConfig.getProperty(FemasConstant.FEMAS_REGISTRY_IP_KEY);
        REGISTRY_CONFIG_MAP.put(RegistryConstants.REGISTRY_HOST, registerHost);

        String registerPort = String.valueOf(FemasConfig.getProperty(FemasConstant.FEMAS_REGISTRY_PORT_KEY));
        REGISTRY_CONFIG_MAP.put(RegistryConstants.REGISTRY_PORT, registerPort);

        String registerType = String.valueOf(FemasConfig.getProperty(FemasConstant.FEMAS_REGISTRY_TYPE_KEY));
        REGISTRY_CONFIG_MAP.put(RegistryConstants.REGISTRY_TYPE, registerType);

        String token = FemasConfig.getProperty(FemasConstant.FEMAS_TOKEN_KEY);
        String domain = FemasConfig.getProperty(PAAS_SERVER_ADDRESS);

        if (!StringUtils.isEmpty(domain)) {
            REGISTRY_CONFIG_MAP.put(PAAS_SERVER_ADDRESS, domain);
        }

        if (!StringUtils.isEmpty(token)) {
            REGISTRY_CONFIG_MAP.put(FemasConstant.CONSUL_ACCESS_TOKEN, token);
            TOKEN = token;
        }

        /**
         * 初始化RPC_INFO 序列化key
         */
        RPC_INFO_SERIALIZE_TAGS.add(FemasConstant.FEMAS_INTERFACE);
        RPC_INFO_SERIALIZE_TAGS.add(FemasConstant.LANE_ID_TAG);
        RPC_INFO_SERIALIZE_TAGS.add("grpc-trace-bin");
        RPC_INFO_SERIALIZE_TAGS.add("grpc-tags-bin");
    }

    /**
     * value为String的Tag map，用于存放用户标签
     */
    private static ThreadLocalContext<String> USER_TAGS = new ThreadLocalContext<>();
    private static ThreadLocal<String> RAW_USER_TAGS_CONTENT = new ThreadLocal<>();

    public static void init() {
    }

    /**
     * 将注册到注册中心的ip放入SYSTEM_TAG中
     */
    public static void putLocalIp(String localIp) {
        FemasContext.putSystemTag(FemasConstant.FEMAS_LOCAL_IP, localIp);
    }

    public static String getServiceName() {
        /**
         * 适配单进程暴露多接口的服务
         */
        String currentServiceName = FemasContext.getRpcInfo().get(FemasConstant.FEMAS_MULTI_SERVICE_NAME);
        if (StringUtils.isNotEmpty(currentServiceName)) {
            return currentServiceName;
        }

        /**
         * 针对只有Consumer类型的应用做的补偿逻辑
         *
         * eg: Dubbo-client
         */
        if (StringUtils.isEmpty(SERVICE_NAME)) {
            synchronized (FemasContext.class) {
                if (StringUtils.isEmpty(SERVICE_NAME)) {
                    String serviceName = FemasContext.getSystemTag(FemasConstant.FEMAS_SERVICE_NAME);
                    serviceName =
                            StringUtils.isEmpty(serviceName) ? FemasConstant.FEMAS_DEFAULT_SERVICE_NAME_PREFIX + UUID
                                    .randomUUID() : serviceName;
                    FemasContext.putSystemTag(FemasConstant.FEMAS_SERVICE_NAME, serviceName);
                    SERVICE_NAME = serviceName;
                }
            }
        }
        return SERVICE_NAME;
    }

    /**
     * 内部使用
     *
     * @param key
     * @param value
     * @param tags
     */
    static void putTag0(String key, Object value, ThreadLocalContext tags) {
        tags.put(key, value);
    }

    static String getTag0(String key, ThreadLocalContext<String> tags) {
        return tags.get(key);
    }

    public void init(String serviceName, Integer port) {
        if (!StringUtils.isEmpty(serviceName)) {
            FemasContext.putSystemTag(FemasConstant.FEMAS_SERVICE_NAME, serviceName);
        }

        FemasContext.putSystemTag(FemasConstant.FEMAS_SERVICE_PORT, port.toString());

        String namespace = SYSTEM_TAGS.get(FemasConstant.FEMAS_NAMESPACE_ID);
        if (!StringUtils.isEmpty(namespace)) {
            FemasContext.putSystemTag(FemasConstant.NAMESPACE_SERVICE_NAME, namespace + "/" + serviceName);
        }
        if (StringUtils.isEmpty(SYSTEM_TAGS.get(FemasConstant.FEMAS_INSTANCE_ID))) {
            // 设置默认实例id，单应用注册多服务时需要调整
            FemasContext.putSystemTag(FemasConstant.FEMAS_INSTANCE_ID,
                    String.format("%s-%s-%s-%d", namespace, serviceName, SYSTEM_TAGS.get(FemasConstant.FEMAS_LOCAL_IP),
                            port));
        }
    }

    public String getToken() {
        return TOKEN;
    }

    public void putCurrentServiceName(String currentServiceName) {
        FemasContext.getRpcInfo().put(FemasConstant.FEMAS_MULTI_SERVICE_NAME, currentServiceName);
        FemasContext.getRpcInfo().put(FemasConstant.FEMAS_SERVICE_NAME, currentServiceName);
    }

    public String getSourceServiceName() {
        /**
         * 适配单进程暴露多接口的服务
         */
        String sourceServiceName = FemasContext.getRpcInfo().get(FemasConstant.SOURCE_FEMAS_MULTI_SERVICE_NAME);
        if (StringUtils.isNotEmpty(sourceServiceName)) {
            return sourceServiceName;
        }

        sourceServiceName = FemasContext.getRpcInfo().get(FemasConstant.SOURCE_SERVICE_NAME);

        return sourceServiceName;
    }

    public String getServiceNameFromContext() {
        return getServiceName();
    }

    public Map<String, String> getRegistryConfigMap() {
        return REGISTRY_CONFIG_MAP;
    }

    @Override
    public Map<String, String> getRequestMetaSerializeTags() {
        Map<String, String> allHeadersTags = new HashMap<>();
        String userTags = serializeUserTags();
        if (!StringUtils.isEmpty(userTags)) {
            allHeadersTags.put(ContextConstant.USER_TAGS_HEADER_KEY, userTags);
        }

        String systemTags = Context.serializeSystemTags();
        if (!StringUtils.isEmpty(systemTags)) {
            allHeadersTags.put(ContextConstant.SYSTEM_TAGS_HEADER_KEY, systemTags);
        }
        return allHeadersTags;
    }

    @Override
    public boolean isEmptyPaasServer() {
        String address = FemasConfig.getProperty(PAAS_SERVER_ADDRESS);
        boolean flag = StringUtils.isEmpty(address);
        if (flag && isLoggerPrinted.compareAndSet(true, false)) {
            logger.error(
                    "could not find the paas address profile , please check your configuration property <paas_server_address>");
        }
        return flag;
    }

    /**
     * 对用户暴露
     * Tags放入USER_TAG中
     *
     * @param tagMap
     */
    public void putTags(Map<String, String> tagMap) {
        for (Map.Entry<String, String> entry : tagMap.entrySet()) {
            putTag(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 对用户暴露
     * Tag放入USER_TAG中
     */
    public static void putTag(String key, String value) {
        checkUserTags();
        putTag0(key, value, USER_TAGS);
    }

    /**
     * 对用户暴露，从上下文中取出上游传递的TAG
     *
     * @param key
     * @return
     */
    public static String getTag(String key) {
        checkUserTags();
        return getTag0(key, USER_TAGS);
    }


    /**
     * 对用户暴露，从上下文中取出所有的用户TAG
     *
     * @return
     */
    public Map<String, String> getAllTags() {
        checkUserTags();
        return USER_TAGS.getAll();
    }


    /**
     * 对用户暴露
     * Tag从USER_TAG中移除
     */
    public void removeTag(String key) {
        checkUserTags();
        USER_TAGS.remove(key);
    }


    /**
     * 这里只是将userTags放入ThreadLocal
     * 如果在当前请求过程中，没有putUserTag或者getUserTag，则不需要序列化和反序列化
     *
     * @param userTags
     */
    public void deserializeUserTags(String userTags) {
        if (StringUtils.isEmpty(userTags)) {
            USER_TAGS.reset();
        }

        RAW_USER_TAGS_CONTENT.set(userTags);
    }

    /**
     * 从Header中读取出String类型的SourceTag
     * 解析后放入RpcInfo
     *
     * @param sourceSystemTags
     */
    public static void deserializeSourceSystemTagsToRpcInfo(String sourceSystemTags) {
        Map<String, String> sourceSystemTagsMap = GsonUtil.deserialize(sourceSystemTags, Map.class);

        if (sourceSystemTags == null || sourceSystemTagsMap == null) {
            return;
        }

        for (Map.Entry<String, String> entry : sourceSystemTagsMap.entrySet()) {
            FemasContext.getRpcInfo().put(ContextConstant.SOURCE + entry.getKey(), entry.getValue());
        }
    }


    /**
     * 当次调用没有使用UserTags，则直接上游的raw string
     *
     * @return
     */
    public String serializeUserTags() {
        String userTags = RAW_USER_TAGS_CONTENT.get();
        if (userTags != null) {
            return userTags;
        }

        Map<String, String> map = USER_TAGS.getAll();
        if (map.isEmpty()) {
            return "";
        }

        return GsonUtil.serializeToJson(map);
    }


    /**
     * 延迟反序列化
     */
    private static void checkUserTags() {
        String userTags = RAW_USER_TAGS_CONTENT.get();
        if (userTags != null) {
            RAW_USER_TAGS_CONTENT.set(null);
            // 如果是空，直接不进行反序列化
            if (StringUtils.isEmpty(userTags)) {
                USER_TAGS.reset();
                return;
            }

            Map<String, String> userTagsMap = GsonUtil.deserialize(userTags, Map.class);
            if (userTagsMap == null) {
                return;
            }
            for (Map.Entry<String, String> entry : userTagsMap.entrySet()) {
                putTag0(entry.getKey(), entry.getValue(), USER_TAGS);
            }
        }
    }

    public void reset() {
        USER_TAGS.reset();
        RAW_USER_TAGS_CONTENT.remove();
        super.reset();
    }

    @Override
    public Map<String, String> getUpstreamTags() {
        return getAllTags();
    }

    @Override
    public Map<String, String> getCurrentTags() {
        // 对于 femas，暂时不区分
        return getAllTags();
    }


    @Override
    public void getSerializeTagsFromRequestMeta(AbstractRequestMetaUtils headerUtils) {
        String userTags = headerUtils.getRequestMeta(ContextConstant.USER_TAGS_HEADER_KEY);
        String systemTags = headerUtils.getRequestMeta(ContextConstant.SYSTEM_TAGS_HEADER_KEY);

        if(logger.isDebugEnabled()) {
            logger.debug("get original upstream userTags:{}, systemTags:{}", userTags, systemTags);
        }

        if (!StringUtils.isEmpty(userTags)) {
            deserializeUserTags(userTags);
        }

        if (!StringUtils.isEmpty(systemTags)) {
            deserializeSourceSystemTagsToRpcInfo(systemTags);
        }

        // 后解析 tag 插件，如果存在相同 tag，这里优先
        Map<String, String> femasTagHeaders =
                headerUtils.getPrefixRequestMetas(FEMAS_TAG_PLUGIN_HEADER_PREFIX);
        if(logger.isDebugEnabled()) {
            logger.debug("get femasTagHeaders:{}", femasTagHeaders);
        }
        for(Map.Entry<String, String> headers: femasTagHeaders.entrySet()) {
            putTag(headers.getKey().substring(FEMAS_TAG_PLUGIN_HEADER_PREFIX.length()),
                    headers.getValue());
        }
    }

}