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
 * 文件名称：FemasConfigManagerFactory.java
 * 创建时间：Aug 16, 2021 9:25:39 PM
 * @author juanyinyang
 * 类说明：
 */
public class FemasConfigManagerFactory {

    public static FemasConfigManager getConfigManagerInstance() {
        return FemasConfigManagerHolder.configManagerInstance;
    }

    private static class FemasConfigManagerHolder {

        static FemasConfigManager configManagerInstance = null;

        static {

            // SPI加载并初始化实现类
            ServiceLoader<FemasConfigManager> femasConfigManagerLoader = ServiceLoader.load(FemasConfigManager.class);
            Iterator<FemasConfigManager> femasConfigManagerIterator = femasConfigManagerLoader.iterator();
            // 一般就一个实现类，如果有多个，那么加载的是最后一个
            while (femasConfigManagerIterator.hasNext()) {
                configManagerInstance = femasConfigManagerIterator.next();
            }
        }
    }
}
  