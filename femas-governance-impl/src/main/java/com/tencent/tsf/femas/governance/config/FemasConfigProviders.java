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
package com.tencent.tsf.femas.governance.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.governance.plugin.Attribute;
import com.tencent.tsf.femas.governance.plugin.ConfigProvider;
import com.tencent.tsf.femas.governance.plugin.PluginDefinitionReader;
import com.tencent.tsf.femas.governance.plugin.config.Configuration;
import com.tencent.tsf.femas.governance.plugin.context.ConfigContext;

/**
 * @Author leoziltong
 * @Date: 2021/5/31 15:44
 */
public class FemasConfigProviders implements ConfigProvider {

    private static final  PluginDefinitionReader reader = new PluginDefinitionReader();

    private static final  ObjectMapper mapper = new ObjectMapper();

    @Override
    public ConfigContext getPluginConfigs() throws FemasRuntimeException {
        Configuration config = loadConfig();
        ConfigContext configContext = new ConfigContext(config);
        try {
            if (config.hasEmpty()) {
                config.setDefault();
                config.verify();
            }
        } catch (Exception e) {
            throw new FemasRuntimeException("fail to verify configuration", e);
        }
        return configContext;
    }

    private static Configuration loadConfig() throws FemasRuntimeException {
        try {
            TreeTraversingParser treeTraversingParser = new TreeTraversingParser(reader.getJsonNode());
            return mapper.readValue(treeTraversingParser, ConfigurationImpl.class);
        } catch (Exception e) {
//            throw new FemasRuntimeException(
//                    "fail to load config from stream", e);
        }
        return new ConfigurationImpl();
    }

    @Override
    public Attribute getAttr() {
        return new Attribute(Attribute.Implement.FEMAS, "femasConfig");
    }

    @Override
    public String getType() {
        return this.getClass().getTypeName();
    }

}
