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

package com.tencent.tsf.femas.governance.plugin;


import com.tencent.tsf.femas.common.exception.FemasRuntimeException;

/**
 * @Author leoziltong
 * @Date: 2021/4/19 17:19
 * @Version 1.0
 */
public interface PluginFactory {

    /**
     * 获取插件实例
     *
     * @param type 插件类型
     * @param name 插件名
     * @return 插件实例
     * @throws FemasRuntimeException 获取失败抛出异常
     */
    Plugin getPlugin(Class<? extends Plugin> type, String name) throws FemasRuntimeException;

}
