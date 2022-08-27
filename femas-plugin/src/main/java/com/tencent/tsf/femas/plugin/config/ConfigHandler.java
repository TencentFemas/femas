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

import com.tencent.tsf.femas.common.annotation.SPI;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.spi.SpiExtensionClass;

/**
 * <pre>
 * 文件名称：ConfigHandler.java
 * 创建时间：Jul 29, 2021 8:40:24 PM
 * @author juanyinyang
 * 类说明：拉取服务治理相关配置
 */
@SPI
public abstract class ConfigHandler implements SpiExtensionClass {

    public synchronized void subscribeServiceConfig(final Service service) {
        throw new UnsupportedOperationException("ConfigHandler method subscribeServiceConfig has no implementation");
    }

    public synchronized void subscribeNamespaceConfig(final String namespace) {
        throw new UnsupportedOperationException("ConfigHandler method subscribeNamespaceConfig has no implementation");
    }
}
  