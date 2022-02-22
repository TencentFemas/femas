package com.tencent.tsf.femas.common.context;

import com.tencent.tsf.femas.common.util.StringUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * 存储于ThreadLocal的Map, 用于存储上下文.<br/>
 *
 * @author zhixinzxliu
 */
class ThreadLocalContext<T> {

    private Map<String, T> initMap;
    private ThreadLocal<Map<String, T>> contextMap = new ThreadLocal<Map<String, T>>() {
        @Override
        protected Map<String, T> initialValue() {
            // 降低loadFactory减少冲突
            HashMap<String, T> map = new HashMap<String, T>(16, 0.5f);

            if (initMap != null) {
                map.putAll(initMap);
            }

            return map;
        }
    };

    ;

    public ThreadLocalContext() {
    }

    public ThreadLocalContext(Map<String, T> initMap) {
        this.initMap = initMap;
    }

    /**
     * 放入ThreadLocal的上下文信息.
     */
    public void put(String key, T value) {
        contextMap.get().put(key, value);
    }

    public void putAll(Map<String, T> map) {
        contextMap.get().putAll(map);
    }

    /**
     * 取出ThreadLocal的上下文信息.
     */
    @SuppressWarnings("unchecked")
    public T get(String key) {
        return contextMap.get().get(key);
    }

    /**
     * 只提供给Context使用
     *
     * @return
     */
    protected Map<String, T> getAll() {
        return contextMap.get();
    }

    protected T remove(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }

        return contextMap.get().remove(key);
    }

    /**
     * 清理ThreadLocal的Context内容.
     */
    public void reset() {
        contextMap.get().clear();
//
//        if (initMap != null) {
//            contextMap.get().putAll(initMap);
//        }
    }

    protected void refreshInitMap(Map<String, T> initMap) {
        this.initMap = initMap;
    }
}
