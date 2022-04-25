/*
 * Tencent is pleased to support the open source community by making Femas available.
 *
 * Copyright (C) 2021, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */   
package com.tencent.tsf.femas.entity.registry.polaris;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
* <pre>  
* 文件名称：PolarisServer.java  
* 创建时间：Dec 28, 2021 5:10:55 PM   
* @author juanyinyang  
* 类说明：  
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolarisServer {
    
    /** 六位返回码 */
    private Integer code;
    /** 
     * 本次查询返回的服务个数，例如查询命名空间为default的服务，总数为1000，本次返回100条，则size为100
     */
    private Integer size;
    /** 
     * 符合此查询条件的服务总数，例如查询命名空间为default的服务，总数为1000，本次返回100条，则amount为1000
     */
    private Integer amount;
    /** 返回信息 */
    private String info;
    
    private List<PolarisInstance> instances;
    
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public List<PolarisInstance> getInstances() {
        return instances;
    }

    public void setInstances(List<PolarisInstance> instances) {
        this.instances = instances;
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PolarisInstance {

        private String id;
        private String service;
        private String namespace;
        private String host;
        private Integer port;
        private String protocol;
        private String version;
        private Integer weight;
        private boolean healthy;
        private boolean isolate;
        @JsonProperty("logic_set")
        private String logicSet;
        private Map<String, String> metadata;
        private String ctime;
        private String mtime;
        
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getService() {
            return service;
        }
        public void setService(String service) {
            this.service = service;
        }
        public String getNamespace() {
            return namespace;
        }
        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }
        public String getHost() {
            return host;
        }
        public void setHost(String host) {
            this.host = host;
        }
        public Integer getPort() {
            return port;
        }
        public void setPort(Integer port) {
            this.port = port;
        }
        public String getProtocol() {
            return protocol;
        }
        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }
        public String getVersion() {
            return version;
        }
        public void setVersion(String version) {
            this.version = version;
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
        public boolean isIsolate() {
            return isolate;
        }
        public void setIsolate(boolean isolate) {
            this.isolate = isolate;
        }
        public String getLogicSet() {
            return logicSet;
        }
        public void setLogicSet(String logicSet) {
            this.logicSet = logicSet;
        }
        public Map<String, String> getMetadata() {
            return metadata;
        }
        public void setMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
        }
        public String getCtime() {
            return ctime;
        }
        public void setCtime(String ctime) {
            this.ctime = ctime;
        }
        public String getMtime() {
            return mtime;
        }
        public void setMtime(String mtime) {
            this.mtime = mtime;
        }
    }
    
}
  