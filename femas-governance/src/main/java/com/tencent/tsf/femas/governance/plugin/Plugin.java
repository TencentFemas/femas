/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.tsf.femas.governance.plugin;


import com.tencent.tsf.femas.common.spi.SpiExtensionClass;

/**
 * 插件需要继承的接口
 * plugin感觉需要抽象一个更高的维度，不应该放在gov层
 *
 * @Author leoziltong
 * @Date: 2021/5/28 11:35
 * @Version 1.0
 */
public interface Plugin<T> extends SpiExtensionClass, Lifecycle {

    /**
     * 不强制提供此机制，根据需求实现
     */
    default void onRefresh() {
    }

    /**
     * 初始化之后，提供动态调整机制，也不强制
     *
     * @param config
     */
    default void freshFactoryBySpecifyConfig(final T config) {
    }

}