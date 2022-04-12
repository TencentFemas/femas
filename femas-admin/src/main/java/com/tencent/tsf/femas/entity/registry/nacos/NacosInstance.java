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
import java.util.List;
import java.util.Map;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/5/7 16:28
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NacosInstance {

    private List<Instance> list;

    private Integer count;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<Instance> getList() {
        return list;
    }

    public void setList(List<Instance> list) {
        this.list = list;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Instance {

        private String instanceId;
        private String ip;
        private String port;
        private Integer weight;
        private boolean healthy;
        private boolean enabled;
        private boolean ephemeral;
        private String clusterName;
        private String serviceName;
        private Map<String, String> metadata;
        private String app;
        private Long lastBeat;
        private boolean marked;
        private Long instanceHeartBeatInterval;
        private Long instanceHeartBeatTimeOut;
        private Long ipDeleteTimeout;

        public String getInstanceId() {
            return instanceId;
        }

        public void setInstanceId(String instanceId) {
            this.instanceId = instanceId;
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

        public Integer getWeight() {
            return weight;
        }

        public void setWeight(Integer weight) {
            this.weight = weight;
        }

        public boolean isHealthy() {
            return healthy;
        }

        public void setHealthy(boolean healthy) {
            this.healthy = healthy;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEphemeral() {
            return ephemeral;
        }

        public void setEphemeral(boolean ephemeral) {
            this.ephemeral = ephemeral;
        }

        public String getClusterName() {
            return clusterName;
        }

        public void setClusterName(String clusterName) {
            this.clusterName = clusterName;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getApp() {
            return app;
        }

        public void setApp(String app) {
            this.app = app;
        }

        public Long getLastBeat() {
            return lastBeat;
        }

        public void setLastBeat(Long lastBeat) {
            this.lastBeat = lastBeat;
        }

        public boolean isMarked() {
            return marked;
        }

        public void setMarked(boolean marked) {
            this.marked = marked;
        }

        public Long getInstanceHeartBeatInterval() {
            return instanceHeartBeatInterval;
        }

        public void setInstanceHeartBeatInterval(Long instanceHeartBeatInterval) {
            this.instanceHeartBeatInterval = instanceHeartBeatInterval;
        }

        public Long getInstanceHeartBeatTimeOut() {
            return instanceHeartBeatTimeOut;
        }

        public void setInstanceHeartBeatTimeOut(Long instanceHeartBeatTimeOut) {
            this.instanceHeartBeatTimeOut = instanceHeartBeatTimeOut;
        }

        public Long getIpDeleteTimeout() {
            return ipDeleteTimeout;
        }

        public void setIpDeleteTimeout(Long ipDeleteTimeout) {
            this.ipDeleteTimeout = ipDeleteTimeout;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
        }
    }
}
