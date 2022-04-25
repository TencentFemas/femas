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

package com.tencent.tsf.femas.entity.registry;

import java.util.List;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/4/29 20:10
 */
public class RegistryInfo {

    private RegistryConfig config;
    private List<ClusterServer> clusterServers;
    private Integer namespaceCount;

    public RegistryInfo() {
    }

    public RegistryInfo(RegistryConfig config, List<ClusterServer> clusterServers) {
        this.config = config;
        this.clusterServers = clusterServers;
    }

    public Integer getNamespaceCount() {
        return namespaceCount;
    }

    public void setNamespaceCount(Integer namespaceCount) {
        this.namespaceCount = namespaceCount;
    }

    public RegistryConfig getConfig() {
        return config;
    }

    public void setConfig(RegistryConfig config) {
        this.config = config;
    }

    public List<ClusterServer> getClusterServers() {
        return clusterServers;
    }

    public void setClusterServers(List<ClusterServer> clusterServers) {
        this.clusterServers = clusterServers;
    }
}
