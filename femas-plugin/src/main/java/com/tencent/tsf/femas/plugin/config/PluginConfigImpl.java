/**
 * Tencent is pleased to support the open source community by making Polaris available.
 * <p>
 * Copyright (C) 2019 THL A29 Limited, a Tencent company. All rights reserved.
 * <p>
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://opensource.org/licenses/BSD-3-Clause
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.tencent.tsf.femas.plugin.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.common.util.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 插件配置对象解析
 *
 * @author andrewshan
 * @author leoziltong
 * @date 2019/8/20
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PluginConfigImpl implements PluginConfig {

    /**
     * 用于转换插件配置逻辑
     */
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    /**
     * 此处并不是插件配置管理容器概念，而是针对链式插件的不同配置管理，非链式插件忽略
     * Map<String //插件getName函数值，同一类型插件，会有不同配置 , Map<String, Object> //具体的配置项 KV>
     */
    @JsonProperty
    private Map<String, Map<String, Object>> plugin = new HashMap<>();

    @Override
    public <T> T getPluginConfig(String pluginName, Class<T> clazz) throws FemasRuntimeException {
        if (null == plugin || plugin.isEmpty()) {
            return null;
        }
        Map<String, Object> properties = plugin.get(pluginName);
        if (null == properties) {
            return null;
        }
        T result = null;
        try {
            result = mapper.convertValue(properties, clazz);
        } catch (IllegalArgumentException e) {
            throw new FemasRuntimeException(
                    String.format("fail to deserialize properties %s to clazz %s for plugin %s", properties,
                            clazz.getCanonicalName(), pluginName), e);
        }
        return result;
    }

    public Map<String, Map<String, Object>> getPlugin() {
        return plugin;
    }

    public void setPlugin(Map<String, Map<String, Object>> plugin) {
        this.plugin = plugin;
    }

    /**
     * 设置特定插件配置
     *
     * @param pluginName 插件名
     * @param config 插件配置对象
     * @throws FemasRuntimeException 设置过程出现的异常
     */
    public void setPluginConfig(String pluginName, Object config) throws FemasRuntimeException {
        Map<String, Object> configMap = null;
        try {
            configMap = Utils.objectToMap(config);
        } catch (Exception e) {
            throw new FemasRuntimeException(
                    String.format("fail to marshal plugin config for %s", pluginName), e);
        }
        if (null == config) {
            throw new FemasRuntimeException(
                    String.format("config is null, plugin name %s", pluginName));
        }
        plugin.put(pluginName, configMap);
    }
}
