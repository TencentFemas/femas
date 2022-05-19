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
package com.tencent.tsf.femas.config;

import com.tencent.tsf.femas.common.constant.FemasConstant;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2022/5/19 18:21
 */
public class ConfigServiceTest {

    @Test
    public void test(){
        try {
            String type = "nacos";
            Map<String, String> configs =new HashMap<>();
            configs.put("registryHost", "127.0.0.1");
            configs.put("registryPort", "8080");
                    ConfigService.createConfig(type,configs);
        }catch(Exception e){}
    }

}
