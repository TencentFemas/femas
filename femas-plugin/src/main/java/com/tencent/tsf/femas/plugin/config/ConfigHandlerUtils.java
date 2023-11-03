/*
 * Tencent is pleased to support the open source community by making Femas available.
 *
 * Copyright (C) 2021, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.tencent.tsf.femas.plugin.config;

import com.tencent.tsf.femas.common.entity.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ConfigHandlerUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConfigHandlerUtils.class);

    public static void subscribeServiceConfig(ConfigHandler configHandler, Service service) {
        if (configHandler != null) {
            configHandler.subscribeServiceConfig(service);
        }
    }

    public static void subscribeServiceConfigFromMap(String type, Service service) {
        Map<String, ConfigHandler> configHandlerMap = ConfigHandlerFactory.getConfigHandlerMap();
        ConfigHandler configHandler = configHandlerMap.get(type);
        if (configHandler != null) {
            configHandler.subscribeServiceConfig(service);
        } else {
            logger.warn("config handler is disable, type:{}", type);
        }
    }

    public static void subscribeNamespaceConfig(ConfigHandler configHandler, String namespace) {
        if (configHandler != null) {
            configHandler.subscribeNamespaceConfig(namespace);
        }
    }

    public static void subscribeNamespaceConfig(String type, String namespace) {
        Map<String, ConfigHandler> configHandlerMap = ConfigHandlerFactory.getConfigHandlerMap();
        ConfigHandler configHandler = configHandlerMap.get(type);
        if (configHandler != null) {
            configHandler.subscribeNamespaceConfig(namespace);
        } else {
            logger.warn("config handler is disable, type:{}", type);
        }
    }
}
  