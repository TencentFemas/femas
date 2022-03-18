package com.tencent.tsf.femas.config.impl.nacos;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.tencent.tsf.femas.common.serialize.JSONSerializer;
import com.tencent.tsf.femas.common.util.NamedThreadFactory;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.config.enums.PropertyChangeType;
import com.tencent.tsf.femas.config.internals.AbstractStringConfig;
import com.tencent.tsf.femas.config.model.ConfigChangeEvent;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FemasNacosConfig extends AbstractStringConfig {

    public static final String NUMBER_SIGN_SEPARATOR = "#";
    private static final Logger LOGGER = LoggerFactory.getLogger(FemasNacosConfig.class);
    private static final String DEFAULT_NACOS_SERVER_ADDR = "localhost:8848";
    private static final String DEFAULT_NACOS_USERNAME = "nacos";
    private static final String DEFAULT_NACOS_PASSWORD = "nacos";
    /**
     * default watch timeout in second
     */
    private static final int DEFAULT_WATCH_TIMEOUT = 3000;
    /**
     * <femas的命名空间ID, ConfigService对象>
     */
    private volatile Map<String, ConfigService> femasNacosConfigServiceMap = new ConcurrentHashMap<>();
    /**
     * <femas的命名空间ID, FemasNacosListener对象>
     */
    private volatile Map<String, FemasNacosListener> femasNacosListenerMap = new ConcurrentHashMap<>();
    private NacosConfigProperties nacosConfigProperties = null;
    private ExecutorService notifierExecutor = new ThreadPoolExecutor(0, 200,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(),
            new NamedThreadFactory("femas-nacos-config-notifier", true));

    public FemasNacosConfig() {
        new FemasNacosConfig(null);
    }

    public FemasNacosConfig(Map<String, String> configMap) {
        // 从spring上下文中获取应用配置（这里的应用配置指的是bootstrap.properties中的配置）
        NacosConfigManager nacosConfigManager = SpringApplicationContextUtil.getBean(NacosConfigManager.class);
        if (nacosConfigManager != null) {
            nacosConfigProperties = nacosConfigManager.getNacosConfigProperties();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized boolean publishConfig(Object[] params) {
        boolean publishResult = false;
        String paramsStr = null;
        try {
            String namespaceId = (String) params[0];
            String configId = (String) params[1];
            String systemTag = (String) params[2];
            String configValue = (String) params[3];
            String configType = (String) params[4];
            String serviceName = (String) params[5];
            String serverAddr = (String) params[6];

            String dataId = configId;
            if (StringUtils.isNotBlank(serviceName)) {
                dataId = serviceName;
            }
            String group = "DEFAULT_GROUP";
            if (StringUtils.isNotEmpty(systemTag)) {
                Map<String, String> map = JSONSerializer.deserializeStr(Map.class, systemTag);
                if (map != null) {
                    if (StringUtils.isNotEmpty(map.get("dataId"))) {
                        dataId = map.get("dataId");
                    }
                    if (StringUtils.isNotEmpty(map.get("group"))) {
                        group = map.get("group");
                    }
                }
            }

            paramsStr = JSONSerializer.serializeStr(params);
            LOGGER.info("[Femas Nacos Config Client] Start to publishConfig, params: " + paramsStr);
            ConfigService configService = createAndGetNacosConfigService(namespaceId, serverAddr);
            LOGGER.info(
                    "[Atom Nacos Config Client] publishConfig namespaceId:{}, dataId:{}, group:{}, content:{}, type:{}",
                    namespaceId, dataId, group, configValue, configType);
            publishResult = configService.publishConfig(dataId, group, configValue, configType);
            LOGGER.info("[Femas Nacos Config Client] publishConfig params: " + paramsStr + " success.");
        } catch (Throwable e) {
            LOGGER.error("[Femas Nacos Config Client] publishConfig throwable, params:{}", paramsStr, e);
        }
        return publishResult;
    }


    /**
     * key为namespaceId#dataId#group，比如：ns-abcde#provider-demo.yaml#DEFAULT_GROUP
     *
     * @see com.tencent.tsf.femas.config.internals.AbstractConfig#doSubscribe(java.lang.String)
     */
    @Override
    protected void doSubscribe(String key) {
        try {
            LOGGER.info("[Femas Nacos Config Client] Start to subscribe key : " + key);
            FemasNacosListener femasNacosListener = createAndGetFemasNacosListener(key);
            FemasNacosConfigProp femasNacosConfigProp = getFemasNacosConfigProp(key);
            ConfigService configService = createAndGetNacosConfigService(femasNacosConfigProp.getNamespaceId(), null);
            configService
                    .addListener(femasNacosConfigProp.getDataId(), femasNacosConfigProp.getGroup(), femasNacosListener);
            LOGGER.info("[Femas Nacos Config Client] subscribe key : " + key + " success.");
        } catch (Throwable e) {
            LOGGER.error("[Femas Nacos Config Client] doSubscribe throwable, key:{}", key, e);
        }
    }

    @Override
    protected void doSubscribeDirectory(String key) {
    }

    @Override
    protected void doUnSubscribe(String key) {
        try {
            FemasNacosListener femasNacosListener = createAndGetFemasNacosListener(key);
            FemasNacosConfigProp femasNacosConfigProp = getFemasNacosConfigProp(key);
            ConfigService configService = createAndGetNacosConfigService(femasNacosConfigProp.getNamespaceId(), null);
            configService.removeListener(femasNacosConfigProp.getDataId(), femasNacosConfigProp.getGroup(),
                    femasNacosListener);
        } catch (Throwable e) {
            LOGGER.error("[Femas Nacos Config Client] doUnSubscribe throwable, key:{}", key, e);
        }
    }

    @Override
    protected String doGetProperty(String key) {
        String result = null;
        try {
            FemasNacosConfigProp femasNacosConfigProp = getFemasNacosConfigProp(key);
            ConfigService configService = createAndGetNacosConfigService(femasNacosConfigProp.getNamespaceId(), null);
            result = configService.getConfig(femasNacosConfigProp.getDataId(), femasNacosConfigProp.getGroup(),
                    DEFAULT_WATCH_TIMEOUT);
        } catch (Throwable e) {
            LOGGER.error("[Femas Nacos Config Client] doUnSubscribe throwable, key:{}", key, e);
        }
        return result;
    }

    private synchronized ConfigService createAndGetNacosConfigService(String namespaceId, String serverAddr) {
        ConfigService configService = null;
        try {
            configService = femasNacosConfigServiceMap.get(namespaceId);
            if (configService == null) {
                Properties properties = assembleNacosConfigProperties(namespaceId, serverAddr);
                configService = NacosFactory.createConfigService(properties);
                femasNacosConfigServiceMap.put(namespaceId, configService);
            }
        } catch (Throwable e) {
            LOGGER.error("FemasNacosConfig createAndGetNacosConfigService throwable, namespaceId:{}", namespaceId, e);
        }
        return configService;
    }

    private Properties assembleNacosConfigProperties(String namespaceId, String newServerAddr) {
        // 某个命名空间的Properties只会初始化一次，后续直接从femasNacosConfigServiceMap获取
        // 所以这里不必太担心getProperty和nacosConfigProperties.assembleConfigServiceProperties()的性能问题
        Properties properties = null;
        // nacosConfigProperties只要是SpringBoot托管启动的项目就不会是null
        if (nacosConfigProperties == null) {
            // 非SpringBoot方式启动初始化配置
            properties = new Properties();
            String serverAddr = System.getProperty("spring.cloud.nacos.config.server-addr", DEFAULT_NACOS_SERVER_ADDR);
            String userName = System.getProperty("spring.cloud.nacos.username", DEFAULT_NACOS_USERNAME);
            String password = System.getProperty("spring.cloud.nacos.password", DEFAULT_NACOS_PASSWORD);
            properties.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
            properties.put(PropertyKeyConst.USERNAME, userName);
            properties.put(PropertyKeyConst.PASSWORD, password);
        } else {
            // SpringBoot方式启动初始化配置
            properties = nacosConfigProperties.assembleConfigServiceProperties();
        }
        // 默认使用的是nacosConfigProperties.assembleConfigServiceProperties里获取到的应用配置中的namespaceId
        // 当传入的namespaceId不为空时，设置为传入的namespaceId
        if (StringUtils.isNotBlank(namespaceId)) {
            properties.put(PropertyKeyConst.NAMESPACE, namespaceId);
        }
        // 当传入的newServerAddr(nacos地址)不为空时，设置为传入的newServerAddr
        if (StringUtils.isNotBlank(newServerAddr)) {
            properties.put(PropertyKeyConst.SERVER_ADDR, newServerAddr);
        }
        return properties;
    }

    private synchronized FemasNacosListener createAndGetFemasNacosListener(String key) {
        FemasNacosListener femasNacosListener = null;
        try {
            FemasNacosConfigProp femasNacosConfigProp = getFemasNacosConfigProp(key);
            String namespaceId = femasNacosConfigProp.getNamespaceId();
            femasNacosListener = femasNacosListenerMap.get(namespaceId);
            if (femasNacosListener == null) {
                String oldValue = doGetProperty(key);
                femasNacosListener = new FemasNacosListener(key, oldValue);
                femasNacosListenerMap.put(namespaceId, femasNacosListener);
            }
        } catch (Throwable e) {
            LOGGER.error("FemasNacosConfig createAndGetFemasNacosListener throwable, key:{}", key, e);
        }
        return femasNacosListener;
    }

    private FemasNacosConfigProp getFemasNacosConfigProp(String key) {
        String[] keyArr = key.split(NUMBER_SIGN_SEPARATOR);
        String namespaceId = keyArr[0];
        String dataId = keyArr[1];
        String group = keyArr[2];
        return new FemasNacosConfigProp(namespaceId, dataId, group);
    }

    class FemasNacosConfigProp {

        private String namespaceId;
        private String dataId;
        private String group;

        public FemasNacosConfigProp(String namespaceId, String dataId, String group) {
            super();
            this.namespaceId = namespaceId;
            this.dataId = dataId;
            this.group = group;
        }

        public String getNamespaceId() {
            return namespaceId;
        }

        public void setNamespaceId(String namespaceId) {
            this.namespaceId = namespaceId;
        }

        public String getDataId() {
            return dataId;
        }

        public void setDataId(String dataId) {
            this.dataId = dataId;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }
    }

    class FemasNacosListener implements Listener {

        private String key;
        private String oldValue;

        FemasNacosListener(String key, String oldValue) {
            this.key = key;
            this.oldValue = oldValue;
        }

        /**
         * @see com.alibaba.nacos.api.config.listener.Listener#getExecutor()
         */
        @Override
        public Executor getExecutor() {
            return notifierExecutor;
        }

        /**
         * @see com.alibaba.nacos.api.config.listener.Listener#receiveConfigInfo(java.lang.String)
         */
        @Override
        public void receiveConfigInfo(String receiveValue) {
            LOGGER.info("[Femas Nacos Config Client] FemasNacosListener receive with key:{}, value:{}", key,
                    receiveValue);
            if (StringUtils.isBlank(receiveValue)) {
                ConfigChangeEvent<String> event = new ConfigChangeEvent<>(key, oldValue, null,
                        PropertyChangeType.DELETED);
                fireValueChange(key, event);
                LOGGER.info("[Femas Consul Config Client] FemasNacosListener Fire Change events with key : " + key
                        + ", Changed event : " + event);
                return;
            }
            String newValue = receiveValue;
            ConfigChangeEvent<String> event = new ConfigChangeEvent<>(key, oldValue, newValue,
                    PropertyChangeType.MODIFIED);
            fireValueChange(key, event);
            oldValue = receiveValue;
            LOGGER.info("[Femas Consul Config Client] FemasNacosListener Fire Change events with key : " + key
                    + ", Changed event : " + event);
        }

    }
}
