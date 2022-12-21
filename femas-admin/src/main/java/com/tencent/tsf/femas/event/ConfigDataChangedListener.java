package com.tencent.tsf.femas.event;

/**
 * @author mroccyen
 */
public interface ConfigDataChangedListener {
    /**
     * 配置数据变更监听器
     *
     * @param key        配置的key
     * @param updateData 更新的数据
     */
    default void onChanged(String key, String updateData) {
    }
}
