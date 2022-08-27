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

import com.tencent.tsf.femas.common.spi.SpiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * <pre>
 * 文件名称：ConfigHandlerFactory.java
 * 创建时间：Jul 30, 2021 11:18:32 AM
 * @author juanyinyang
 * 类说明：
 */
public class ConfigHandlerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigHandlerFactory.class);

    public static Map<String, ConfigHandler> getConfigHandlerMap() {
        return ConfigHandlerFactoryHolder.configHandlerMap;
    }

    private static class ConfigHandlerFactoryHolder {

        static Map<String, ConfigHandler> configHandlerMap = null;

        static {
            configHandlerMap = SpiService.init(ConfigHandler.class);

            LOGGER.info("ConfigHandlerFactory init spi configHandlerMap:" + configHandlerMap);
        }
    }
}
  