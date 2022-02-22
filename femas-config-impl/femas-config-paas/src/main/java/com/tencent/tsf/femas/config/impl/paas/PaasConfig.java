package com.tencent.tsf.femas.config.impl.paas;

import static java.util.concurrent.Executors.newCachedThreadPool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.common.util.MD5Util;
import com.tencent.tsf.femas.common.util.NamedThreadFactory;
import com.tencent.tsf.femas.common.util.TimeUtil;
import com.tencent.tsf.femas.config.AbstractConfigHttpClientManager;
import com.tencent.tsf.femas.config.AbstractConfigHttpClientManagerFactory;
import com.tencent.tsf.femas.config.enums.PropertyChangeType;
import com.tencent.tsf.femas.config.internals.AbstractStringConfig;
import com.tencent.tsf.femas.config.model.ConfigChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaasConfig extends AbstractStringConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaasConfig.class);
    /**
     * default watch timeout in second
     */
    private static final int DEFAULT_WATCH_TIMEOUT = 55;
    private volatile static ObjectMapper mapper = new ObjectMapper();
    private AbstractConfigHttpClientManager manager;
    private String token;
    private ExecutorService notifierExecutor = newCachedThreadPool(
            new NamedThreadFactory("femas-paas-config-notifier", true));
    private Map<String, Notifier> notifiers = new ConcurrentHashMap<>();

    public PaasConfig(Map<String, String> configMap) {
        this.manager = AbstractConfigHttpClientManagerFactory.getConfigHttpClientManager();
    }

    @Override
    protected void doSubscribe(String key) {
        LOGGER.info("[Femas paas Config Client] Start to subscribe key : " + key);

        ValueChangeNotifier notifier = new ValueChangeNotifier(key, this);
        notifierExecutor.submit(notifier);
        notifiers.put(key, notifier);

        LOGGER.info("[Femas Paas Config Client] subscribe key : " + key + " success.");
    }

    @Override
    protected void doSubscribeDirectory(String key) {

    }

    @Override
    protected void doUnSubscribe(String key) {
        Notifier notifier = notifiers.remove(key);
        notifier.stop();
    }

    @Override
    protected String doGetProperty(String key) {
        try {
            return manager.fetchKVValue(key, "");
        } catch (Exception e) {
            LOGGER.error("paas config Get Property failed", e);
        }
        return null;
    }

    private interface Notifier extends Runnable {

        void stop();
    }

    private class ValueChangeNotifier implements Notifier {

        private String key;
        private long index = -1;
        private volatile boolean running;
        private PaasConfig config;
        private volatile Map<String, GetValue> oldValue = new HashMap<>();

        ValueChangeNotifier(String key, PaasConfig config) {
            this.key = key;
            this.running = true;
            this.config = config;
        }

        @Override
        public void run() {
            while (this.running) {
                try {
                    processValue();
                    TimeUtil.silentlySleep(5000);
                } catch (Exception e) {
                    LOGGER.debug("Femas PAAS CONFIG] Process Paas Config Value failed.", e);
                }
            }
        }

        private void processValue() {
            final String strValue = manager.fetchKVValue(key, "");
            Integer currentIndex = MD5Util.getIndex(strValue);

            if (currentIndex != null && currentIndex != index) {
                LOGGER.info("[Femas paas Config Client] Key : " + key + ", current index = " + currentIndex
                        + ", last index = " + index);
                index = currentIndex;
                List<GetValue> getValues = null;
                try {
                    getValues = mapper.readValue(strValue, new TypeReference<List<GetValue>>() {
                    });
                } catch (JsonProcessingException e) {
                    LOGGER.warn("Femas PAAS CONFIG] Process Paas Config Value failed.", e);
                }
                Map<String, GetValue> curValueMap = new HashMap();
                if (!CollectionUtil.isEmpty(getValues)) {
                    Iterator var1 = getValues.iterator();

                    while (var1.hasNext()) {
                        GetValue curValue = (GetValue) var1.next();
                        curValueMap.put(curValue.getKey(), curValue);
                    }
                }
                List<ConfigChangeEvent<String>> configChangeEvents = new ArrayList();
                Iterator oldIterator = this.oldValue.entrySet().iterator();
                while (oldIterator.hasNext()) {
                    Map.Entry oldEntry = (Map.Entry) oldIterator.next();
                    String curKey = (String) oldEntry.getKey();
                    GetValue oldValue = (GetValue) oldEntry.getValue();
                    GetValue newValue = (GetValue) curValueMap.get(curKey);
                    ConfigChangeEvent event;
                    if (newValue != null) {
                        Integer newIndex = MD5Util.getIndex(newValue.getValue());
                        Integer oldIndex = MD5Util.getIndex(oldValue.getValue());
                        if (!newIndex.equals(oldIndex)) {
                            event = new ConfigChangeEvent(curKey, oldValue.getValue(), newValue.getValue(),
                                    PropertyChangeType.MODIFIED);
                            configChangeEvents.add(event);
                        }
                    } else {
                        event = new ConfigChangeEvent(curKey, oldValue.getValue(), (Object) null,
                                PropertyChangeType.DELETED);
                        configChangeEvents.add(event);
                    }
                }
                Iterator<Map.Entry<String, GetValue>> curIterator = curValueMap.entrySet().iterator();
                while (curIterator.hasNext()) {
                    Map.Entry<String, GetValue> curEntry = curIterator.next();
                    String newKey = curEntry.getKey();
                    GetValue newValue = curEntry.getValue();
                    if (!oldValue.containsKey(newKey)) {
                        ConfigChangeEvent<String> event = new ConfigChangeEvent<>(newKey, null, newValue.getValue(),
                                PropertyChangeType.MODIFIED);
                        configChangeEvents.add(event);
                    }
                }
                oldValue = curValueMap;
                LOGGER.info("[Femas paas Config Client] Fire Change events with key : " + key + ", Changed event : "
                        + configChangeEvents);
                config.fireDirectoryChange(key, configChangeEvents);
            }
        }


        public void stop() {
            this.running = false;
        }
    }

}
