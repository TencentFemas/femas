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

package com.tencent.tsf.femas.entity.registry.eureka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/5/10 20:05
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EurekaServer {

    private ApplicationState applicationStats;
    private InstanceInfo instanceInfo;

    public ApplicationState getApplicationStats() {
        return applicationStats;
    }

    public void setApplicationStats(ApplicationState applicationStats) {
        this.applicationStats = applicationStats;
    }

    public InstanceInfo getInstanceInfo() {
        return instanceInfo;
    }

    public void setInstanceInfo(InstanceInfo instanceInfo) {
        this.instanceInfo = instanceInfo;
    }

    public static class ApplicationState {

        @JsonProperty("registered-replicas")
        private String registeredReplicas;
        @JsonProperty("available-replicas")
        private String availableReplicas;
        @JsonProperty("unavailable-replicas")
        private String unavailableReplicas;

        public String getRegisteredReplicas() {
            return registeredReplicas;
        }

        public void setRegisteredReplicas(String registeredReplicas) {
            this.registeredReplicas = registeredReplicas;
        }

        public String getAvailableReplicas() {
            return availableReplicas;
        }

        public void setAvailableReplicas(String availableReplicas) {
            this.availableReplicas = availableReplicas;
        }

        public String getUnavailableReplicas() {
            return unavailableReplicas;
        }

        public void setUnavailableReplicas(String unavailableReplicas) {
            this.unavailableReplicas = unavailableReplicas;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InstanceInfo {

        private String ipAddr;
        private String hostName;
        private String status;
        private String homePageUrl;

        private Long lastUpdatedTimestamp;

        public String getIpAddr() {
            return ipAddr;
        }

        public void setIpAddr(String ipAddr) {
            this.ipAddr = ipAddr;
        }

        public String getHostName() {
            return hostName;
        }

        public void setHostName(String hostName) {
            this.hostName = hostName;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getHomePageUrl() {
            return homePageUrl;
        }

        public void setHomePageUrl(String homePageUrl) {
            this.homePageUrl = homePageUrl;
        }

        public Long getLastUpdatedTimestamp() {
            return lastUpdatedTimestamp;
        }

        public void setLastUpdatedTimestamp(Long lastUpdatedTimestamp) {
            this.lastUpdatedTimestamp = lastUpdatedTimestamp;
        }
    }
}
