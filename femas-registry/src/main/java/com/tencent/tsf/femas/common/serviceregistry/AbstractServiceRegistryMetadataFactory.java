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

package com.tencent.tsf.femas.common.serviceregistry;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * <pre>
 * 文件名称：AbstractServiceRegistryMetadataFactory.java
 * @author juanyinyang
 * 类说明：
 */
public class AbstractServiceRegistryMetadataFactory {

    public static AbstractServiceRegistryMetadata getServiceRegistryMetadata() {
        return AbstractServiceRegistryMetadataFactoryHolder.serviceRegistryMetadata;
    }

    private static class AbstractServiceRegistryMetadataFactoryHolder {

        static AbstractServiceRegistryMetadata serviceRegistryMetadata = null;

        static {
            // SPI加载并初始化实现类
            ServiceLoader<AbstractServiceRegistryMetadata> contextServiceLoader = ServiceLoader
                    .load(AbstractServiceRegistryMetadata.class);
            Iterator<AbstractServiceRegistryMetadata> contextIterator = contextServiceLoader.iterator();
            // 一般就一个实现类，如果有多个，那么加载的是最后一个
            while (contextIterator.hasNext()) {
                serviceRegistryMetadata = contextIterator.next();
            }
        }
    }

}
  