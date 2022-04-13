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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
* <pre>  
* 文件名称：PolarisService.java  
* 创建时间：Dec 30, 2021 5:05:04 PM   
* @author juanyinyang  
* 类说明：  
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolarisService {
    
    private List<Service> services;

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
    
    public List<Service> getServices() {
        return services;
    }
    
    public void setServices(List<Service> services) {
        this.services = services;
    }

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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Service {

        private String name;
        private String namespace;
        private String ports;
        private String business;
        private String department;
        private String comment;
        private String owners;
        private String ctime;
        private String mtime;
        @JsonProperty("total_instance_count")
        private Integer totalInstanceCount;
        @JsonProperty("healthy_instance_count")
        private Integer healthyInstanceCount;
        
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getNamespace() {
            return namespace;
        }
        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }
        public String getPorts() {
            return ports;
        }
        public void setPorts(String ports) {
            this.ports = ports;
        }
        public String getBusiness() {
            return business;
        }
        public void setBusiness(String business) {
            this.business = business;
        }
        public String getDepartment() {
            return department;
        }
        public void setDepartment(String department) {
            this.department = department;
        }
        public String getComment() {
            return comment;
        }
        public void setComment(String comment) {
            this.comment = comment;
        }
        public String getOwners() {
            return owners;
        }
        public void setOwners(String owners) {
            this.owners = owners;
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
        public Integer getTotalInstanceCount() {
            return totalInstanceCount;
        }
        public void setTotalInstanceCount(Integer totalInstanceCount) {
            this.totalInstanceCount = totalInstanceCount;
        }
        public Integer getHealthyInstanceCount() {
            return healthyInstanceCount;
        }
        public void setHealthyInstanceCount(Integer healthyInstanceCount) {
            this.healthyInstanceCount = healthyInstanceCount;
        }
    }
}
  