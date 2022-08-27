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

package com.tencent.tsf.femas.plugin.context;


import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.plugin.Plugin;
import com.tencent.tsf.femas.plugin.PluginFactory;


import java.util.List;

/**
 * 插件管理器
 *
 * @author leoziltong
 */
public interface AbstractSDKContext extends PluginFactory {

    /**
     * 初始化插件列表
     *
     * @param context 插件初始化上下文
     * @param types 插件类型
     * @throws Exception 插件初始化过程中抛出异常
     */
    void initPlugins(ConfigContext context, List<Class<? extends Plugin>> types) throws FemasRuntimeException;

    /**
     * 销毁已初始化的插件列表
     */
    void destroyPlugins();

}
