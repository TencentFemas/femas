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

package com.tencent.tsf.femas.config;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * <pre>
 * 文件名称：AbstractConfigHttpClientManagerFactory.java
 * 创建时间：Jul 29, 2021 5:50:34 PM
 * @author juanyinyang
 * 类说明：
 */
public class AbstractConfigHttpClientManagerFactory {

    public static AbstractConfigHttpClientManager getConfigHttpClientManager() {
        return AbstractConfigHttpClientManagerHolder.configHttpClientManager;
    }

    private static class AbstractConfigHttpClientManagerHolder {

        static AbstractConfigHttpClientManager configHttpClientManager = null;

        static {

            // SPI加载并初始化实现类
            ServiceLoader<AbstractConfigHttpClientManager> configHttpClientManagerServiceLoader = ServiceLoader
                    .load(AbstractConfigHttpClientManager.class);
            Iterator<AbstractConfigHttpClientManager> configHttpClientManagerIterator = configHttpClientManagerServiceLoader
                    .iterator();
            // 一般就一个实现类，如果有多个，那么加载的是最后一个
            while (configHttpClientManagerIterator.hasNext()) {
                configHttpClientManager = configHttpClientManagerIterator.next();
            }
        }
    }

}
  