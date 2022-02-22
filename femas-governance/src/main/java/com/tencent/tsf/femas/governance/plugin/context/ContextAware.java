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

package com.tencent.tsf.femas.governance.plugin.context;

import com.tencent.tsf.femas.governance.plugin.Plugin;
import com.tencent.tsf.femas.governance.plugin.PluginFactory;

/**
 * 此处是解决插件之间的相互引用，如集群限流可能会用到网络通信插件
 * 涉及初始化优先级问题，优先级在PluginType中定义，同类插件无优先级差
 *
 * @Author leoziltong
 * @Date: 2021/5/28 15:09
 */
public abstract class ContextAware {

    private PluginFactory factory;

    public PluginFactory getFactory() {
        return factory;
    }

    public void setFactory(PluginFactory factory) {
        this.factory = factory;
    }

    public Plugin getPlugin(Class<? extends Plugin> type, String name) {
        return factory.getPlugin(type, name);
    }
}
