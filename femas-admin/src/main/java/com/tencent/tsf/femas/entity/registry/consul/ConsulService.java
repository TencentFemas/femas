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
import java.util.Map;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/5/11 19:33
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsulService {

    @JsonProperty("Service")
    private Service service;

    @JsonProperty("Node")
    private Node node;

    @JsonProperty("Checks")
    private List<Check> checks;

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public List<Check> getChecks() {
        return checks;
    }

    public void setChecks(List<Check> checks) {
        this.checks = checks;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Node {

        @JsonProperty("Address")
        private String address;
        @JsonProperty("Datacenter")
        private String datacenter;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getDatacenter() {
            return datacenter;
        }

        public void setDatacenter(String datacenter) {
            this.datacenter = datacenter;
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Service {

        @JsonProperty("ID")
        private String id;

        @JsonProperty("Tags")
        private List<String> tags;

        @JsonProperty("Service")
        private String service;

        @JsonProperty("Address")
        private String address;

        @JsonProperty("Port")
        private Integer port;

        @JsonProperty("Meta")
        private Map<String, String> meta;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public String getService() {
            return service;
        }

        public void setService(String service) {
            this.service = service;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public Map<String, String> getMeta() {
            return meta;
        }

        public void setMeta(Map<String, String> meta) {
            this.meta = meta;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Check {

        @JsonProperty("Node")
        private String node;

        @JsonProperty("CheckID")
        private String checkId;

        @JsonProperty("Name")
        private String name;

        @JsonProperty("Status")
        private String status;

        @JsonProperty("Notes")
        private String notes;

        @JsonProperty("Output")
        private String output;

        @JsonProperty("ServiceID")
        private String serviceId;

        @JsonProperty("ServiceName")
        private String serviceName;

        @JsonProperty("Type")
        private String type;

        @JsonProperty("CreateIndex")
        private Integer createIndex;

        @JsonProperty("ModifyIndex")
        private Integer modifyIndex;

        public String getNode() {
            return node;
        }

        public void setNode(String node) {
            this.node = node;
        }

        public String getCheckId() {
            return checkId;
        }

        public void setCheckId(String checkId) {
            this.checkId = checkId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public String getOutput() {
            return output;
        }

        public void setOutput(String output) {
            this.output = output;
        }

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Integer getCreateIndex() {
            return createIndex;
        }

        public void setCreateIndex(Integer createIndex) {
            this.createIndex = createIndex;
        }

        public Integer getModifyIndex() {
            return modifyIndex;
        }

        public void setModifyIndex(Integer modifyIndex) {
            this.modifyIndex = modifyIndex;
        }
    }

}
