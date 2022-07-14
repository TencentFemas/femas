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
package com.tencent.tsf.femas.agent.config;

import com.tencent.tsf.femas.agent.classloader.AgentPackagePathScanner;
import com.tencent.tsf.femas.agent.tools.AgentLogger;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;

/**
 * @Author leoziltong
 * @Date: 2022/7/11 17:35
 * @Version 1.0
 */
public class MechaRuntimePluginsSniffer {

    private static final AgentLogger LOG = AgentLogger.getLogger(MechaRuntimePluginsSniffer.class);

    private static final String DIR_PREFIX = AgentPackagePathScanner.getPath() + "/config/";

    public static List<InterceptPluginConfig> sniffRuntimeAvailablePlugins(String... dirs) {
        if (dirs == null || dirs.length == 0) {
            return fetchDirInterceptPlugins(AgentContext.getAvailablePluginsDirWithDefault());
        }
        List<InterceptPluginConfig> interceptPluginConfigs = new ArrayList<>();
        Arrays.asList(dirs).stream().forEach(e -> {
            interceptPluginConfigs.addAll(fetchDirInterceptPlugins(e));
        });
        return interceptPluginConfigs;
    }

    private static List<InterceptPluginConfig> fetchDirInterceptPlugins(String dir) {
        if (StringUtils.isEmpty(dir)) {
            LOG.warn("[femas-core]:Mecha Runtime Plugins Sniffer fetch Intercept Plugins failed ,target module not found");
            return Collections.emptyList();
        }
        File file = new File(DIR_PREFIX.concat(dir));
        List<InterceptPluginConfig> interceptPluginConfigs = new ArrayList<>();
        File[] fs = file.listFiles();
        for (File f : fs) {
            if (!f.isDirectory()) {
                AtomicInterceptorPluginsLoader loader = new AtomicInterceptorPluginsLoader(f.getAbsolutePath());
                if (Optional.ofNullable(loader).map(l -> l.getInterceptConfig()).isPresent()) {
                    interceptPluginConfigs.addAll(loader.getInterceptConfig());
                }
            }
        }
        return interceptPluginConfigs;
    }

}
