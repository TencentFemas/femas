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

package com.tencent.tsf.femas.entity.registry.nacos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/5/6 19:21
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NacosServer {

    private List<Server> servers;

    public List<Server> getServers() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Server {

        private String ip;

        private String port;
        private String state;

        private String address;
        private ExtendInfo extendInfo;

        public Server() {
        }

        public Server(String ip, String port, String state, String address, ExtendInfo extendInfo) {
            this.ip = ip;
            this.port = port;
            this.state = state;
            this.address = address;
            this.extendInfo = extendInfo;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }


        public ExtendInfo getExtendInfo() {
            return extendInfo;
        }

        public void setExtendInfo(ExtendInfo extendInfo) {
            this.extendInfo = extendInfo;
        }


        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ExtendInfo {

            private long lastRefreshTime;
            private String raftPort;
            private String version;

            private RaftMetaData raftMetaData;

            public long getLastRefreshTime() {
                return lastRefreshTime;
            }

            public void setLastRefreshTime(long lastRefreshTime) {
                this.lastRefreshTime = lastRefreshTime;
            }

            public String getRaftPort() {
                return raftPort;
            }

            public void setRaftPort(String raftPort) {
                this.raftPort = raftPort;
            }

            public String getVersion() {
                return version;
            }

            public void setVersion(String version) {
                this.version = version;
            }

            public RaftMetaData getRaftMetaData() {
                return raftMetaData;
            }

            public void setRaftMetaData(RaftMetaData raftMetaData) {
                this.raftMetaData = raftMetaData;
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class RaftMetaData {

            private MetaDataMap metaDataMap;

            public MetaDataMap getMetaDataMap() {
                return metaDataMap;
            }

            public void setMetaDataMap(MetaDataMap metaDataMap) {
                this.metaDataMap = metaDataMap;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class MetaDataMap {

//                @JsonProperty("nacos_config")
//                private Map<String, Object> nacosConfig;

                @JsonProperty("naming_persistent_service")
                private Map<String, Object> namingPersistentService;

//                @JsonProperty("naming_instance_metadata")
//                private Map<String, Object> namingInstanceMetadata;
//
//                @JsonProperty("naming_persistent_service_v2")
//                private Map<String, Object> namingPersistentServiceV2;
//
//                @JsonProperty("naming_service_metadata")
//                private Map<String, Object> namingServiceMetadata;

                public Map<String, Object> getNamingPersistentService() {
                    return namingPersistentService;
                }

                public void setNamingPersistentService(Map<String, Object> namingPersistentService) {
                    this.namingPersistentService = namingPersistentService;
                }
            }

        }
    }
}
