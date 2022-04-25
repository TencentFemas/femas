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

package com.tencent.tsf.femas.entity.registry.consul;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/5/11 19:05
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsulServer {

    @JsonProperty("Servers")
    private List<Server> servers;

    public List<Server> getServers() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Server {

        @JsonProperty("ID")

        private String id;
        @JsonProperty("Node")

        private String node;
        @JsonProperty("Address")

        private String address;
        @JsonProperty("Leader")

        private boolean leader;
        @JsonProperty("ProtocolVersion")

        private String protocolVersion;
        @JsonProperty("Voter")

        private boolean voter;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNode() {
            return node;
        }

        public void setNode(String node) {
            this.node = node;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public boolean isLeader() {
            return leader;
        }

        public void setLeader(boolean leader) {
            this.leader = leader;
        }

        public String getProtocolVersion() {
            return protocolVersion;
        }

        public void setProtocolVersion(String protocolVersion) {
            this.protocolVersion = protocolVersion;
        }

        public boolean isVoter() {
            return voter;
        }

        public void setVoter(boolean voter) {
            this.voter = voter;
        }
    }
}
