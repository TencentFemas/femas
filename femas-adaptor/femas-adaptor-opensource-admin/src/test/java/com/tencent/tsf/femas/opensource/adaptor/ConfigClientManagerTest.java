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
package com.tencent.tsf.femas.opensource.adaptor;

import com.tencent.tsf.femas.adaptor.paas.config.FemasPaasConfigManager;
import com.tencent.tsf.femas.config.ConfigChangeListener;
import com.tencent.tsf.femas.config.impl.paas.PaasConfig;
import com.tencent.tsf.femas.config.model.ConfigChangeEvent;
import org.junit.Test;

import java.util.List;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2022/5/19 17:50
 */
public class ConfigClientManagerTest {

    @Test
    public void testConfigClient(){
        PaasConfig config= FemasPaasConfigManager.getConfig();
        config.subscribe("key",new ConfigChangeListener(){
            @Override
            public void onChange(ConfigChangeEvent changeEvent) {

            }

            @Override
            public void onChange(List list) {

            }
        });
    }
}
