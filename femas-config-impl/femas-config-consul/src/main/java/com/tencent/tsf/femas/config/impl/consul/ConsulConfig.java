package com.tencent.tsf.femas.config.impl.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.common.util.CommonUtils;
import com.tencent.tsf.femas.common.util.NamedThreadFactory;
import com.tencent.tsf.femas.common.util.TimeUtil;
import com.tencent.tsf.femas.config.enums.PropertyChangeType;
import com.tencent.tsf.femas.config.internals.AbstractStringConfig;
import com.tencent.tsf.femas.config.model.ConfigChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newCachedThreadPool;

public class ConsulConfig extends AbstractStringConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsulConfig.class);
    /**
     * default watch timeout in second
     */
    private static final int DEFAULT_WATCH_TIMEOUT = 55;
    private final ConsulClient client;
    private final String token;
    private ExecutorService notifierExecutor = newCachedThreadPool(
            new NamedThreadFactory("femas-consul-config-notifier", true));
    private Map<String, Notifier> notifiers = new ConcurrentHashMap<>();

    public ConsulConfig(Map<String, String> configMap) {
        String host = CommonUtils
                .checkNotNull(ConsulConstants.REGISTRY_HOST, configMap.get(ConsulConstants.REGISTRY_HOST));

        String portString = CommonUtils
                .checkNotNull(ConsulConstants.REGISTRY_PORT, configMap.get(ConsulConstants.REGISTRY_PORT));
        Integer port = Integer.parseInt(portString);

        this.token = configMap.get(ConsulConstants.CONSUL_ACCESS_TOKEN);
        this.client = new ConsulClient(host, port);
        LOGGER.info("[ConsulConfig] init consul host:{}, port:{}, token:{}", host, port, token);
    }

    @Override
    protected void doSubscribe(String key) {
        LOGGER.info("[Femas Consul Config Client] Start to subscribe key : " + key);

        ValueChangeNotifier notifier = new ValueChangeNotifier(key, this);
        notifierExecutor.submit(notifier);
        notifiers.put(key, notifier);

        LOGGER.info("[Femas Consul Config Client] subscribe key : " + key + " success.");
    }

    @Override
    protected void doSubscribeDirectory(String key) {
        LOGGER.info("[Femas Consul Config Client] Start to subscribe path : " + key);

        DirChangeNotifier notifier = new DirChangeNotifier(key, this);
        notifierExecutor.submit(notifier);
        notifiers.put(key, notifier);

        LOGGER.info("[Femas Consul Config Client] subscribe path : " + key + " success.");
    }

    @Override
    public List<ConfigChangeEvent<String>> getDirectory(String dir) {
        try {
            Response<List<GetValue>> response = client.getKVValues(dir, token, QueryParams.DEFAULT);
            List<GetValue> curValues = response.getValue();
            Map<String, GetValue> curValueMap = new HashMap<>();

            if (!CollectionUtil.isEmpty(curValues)) {
                for (GetValue curValue : curValues) {
                    curValueMap.put(curValue.getKey(), curValue);
                }

            }
            List<ConfigChangeEvent<String>> configChangeEvents = new ArrayList<>();
            for (Map.Entry<String, GetValue> curEntry : curValueMap.entrySet()) {
                String newKey = curEntry.getKey();
                GetValue newValue = curEntry.getValue();
                // 都是新增
                ConfigChangeEvent<String> event = new ConfigChangeEvent<>(newKey, null, newValue.getDecodedValue(), PropertyChangeType.ADDED);
                configChangeEvents.add(event);
            }

            LOGGER.debug("[Femas Consul Config Client] Fire Change events with path : " + dir + ", Changed events : " + configChangeEvents);

            return configChangeEvents;
        } catch (Exception e) {
            LOGGER.error("error in getDirectory:{}", dir, e);
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    protected void doUnSubscribe(String key) {
        Notifier notifier = notifiers.remove(key);
        notifier.stop();
    }

    @Override
    protected String doGetProperty(String key) {
        Response<GetValue> response = client.getKVValue(key, token, new QueryParams(-1, -1));
        return response.getValue().getDecodedValue();
    }

    private interface Notifier extends Runnable {

        void stop();
    }

    private class ValueChangeNotifier implements Notifier {

        private String key;
        private long consulIndex = -1;
        private volatile boolean running;
        private ConsulConfig consulConfig;
        private String oldValue;

        ValueChangeNotifier(String key, ConsulConfig consulConfig) {
            this.key = key;
            this.running = true;
            this.consulConfig = consulConfig;
        }

        @Override
        public void run() {
            while (this.running) {
                try {
                    processValue();
                    TimeUtil.silentlySleep(500);
                } catch (Exception e) {
                    LOGGER.debug("Femas CONSUL CONFIG] Process Consul Config Value failed.", e);
                }
            }
        }

        private void processValue() {
            Response<GetValue> response = client
                    .getKVValue(key, token, new QueryParams(DEFAULT_WATCH_TIMEOUT, consulIndex));

            Long currentIndex = response.getConsulIndex();
            if (currentIndex != null && currentIndex != consulIndex) {
                LOGGER.debug("[Femas Consul Config Client] Key : " + key + ", current index = " + currentIndex
                        + ", last index = " + consulIndex);
                consulIndex = currentIndex;

                GetValue value = response.getValue();
                if (value == null) {
                    ConfigChangeEvent<String> event = new ConfigChangeEvent<>(key, oldValue, null,
                            PropertyChangeType.DELETED);
                    oldValue = null;
                    consulConfig.fireValueChange(key, event);

                    return;
                }

                String newValue = value.getDecodedValue();
                ConfigChangeEvent<String> event = new ConfigChangeEvent<>(key, oldValue, newValue,
                        PropertyChangeType.MODIFIED);
                oldValue = newValue;

                LOGGER.debug("[Femas Consul Config Client] Fire Change events with key : " + key + ", Changed event : "
                        + event);
                consulConfig.fireValueChange(key, event);
            }
        }

        public void stop() {
            this.running = false;
        }
    }

    private class DirChangeNotifier implements Notifier {

        private String dir;
        private long consulIndex = -1;
        private volatile boolean running;
        private ConsulConfig consulConfig;

        private volatile Map<String, GetValue> lastValueMap = new HashMap<>();

        DirChangeNotifier(String dir, ConsulConfig consulConfig) {
            this.dir = dir;
            this.running = true;
            this.consulConfig = consulConfig;
        }

        @Override
        public void run() {
            while (this.running) {
                try {
                    processDir();
                    TimeUtil.silentlySleep(500);
                } catch (Exception e) {
                    LOGGER.debug("[Femas CONSUL CONFIG] Process Consul Config Dir failed.", e);
                }
            }
        }

        private void processDir() {
            Response<List<GetValue>> response = client
                    .getKVValues(dir, token, new QueryParams(DEFAULT_WATCH_TIMEOUT, consulIndex));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[DirChangeNotifier] dir:{}, old index:{}, response:{}", dir, consulIndex, response);
            }
            Long currentIndex = response.getConsulIndex();
            if (currentIndex != null && currentIndex != consulIndex) {
                LOGGER.debug("[Femas Consul Config Client] Path : " + dir + ", current index = " + currentIndex
                        + ", last index = " + consulIndex);
                consulIndex = currentIndex;

                List<GetValue> curValues = response.getValue();
                Map<String, GetValue> curValueMap = new HashMap<>();

                if (!CollectionUtil.isEmpty(curValues)) {
                    for (GetValue curValue : curValues) {
                        curValueMap.put(curValue.getKey(), curValue);
                    }

                }

                List<ConfigChangeEvent<String>> configChangeEvents = new ArrayList<>();

                for (Map.Entry<String, GetValue> oldEntry : lastValueMap.entrySet()) {
                    String oldKey = oldEntry.getKey();
                    GetValue oldValue = oldEntry.getValue();
                    GetValue newValue = curValueMap.get(oldKey);

                    if (newValue != null) {
                        // 有变化
                        if (newValue.getModifyIndex() > oldValue.getModifyIndex()) {
                            ConfigChangeEvent<String> event = new ConfigChangeEvent<>(oldKey,
                                    oldValue.getDecodedValue(), newValue.getDecodedValue(),
                                    PropertyChangeType.MODIFIED);
                            configChangeEvents.add(event);
                        }
                    } else {
                        ConfigChangeEvent<String> event = new ConfigChangeEvent<>(oldKey, oldValue.getDecodedValue(),
                                null, PropertyChangeType.DELETED);
                        configChangeEvents.add(event);
                    }
                }

                for (Map.Entry<String, GetValue> curEntry : curValueMap.entrySet()) {
                    String newKey = curEntry.getKey();
                    GetValue newValue = curEntry.getValue();

                    if (!lastValueMap.containsKey(newKey)) {
                        ConfigChangeEvent<String> event = new ConfigChangeEvent<>(newKey, null,
                                newValue.getDecodedValue(), PropertyChangeType.ADDED);
                        configChangeEvents.add(event);
                    }
                }

                LOGGER.debug(
                        "[Femas Consul Config Client] Fire Change events with path : " + dir + ", Changed events : "
                                + configChangeEvents);
                consulConfig.fireDirectoryChange(dir, configChangeEvents);
                lastValueMap = curValueMap;
            }
        }

        public void stop() {
            this.running = false;
        }
    }
}
