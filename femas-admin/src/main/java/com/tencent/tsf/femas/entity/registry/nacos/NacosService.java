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

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/5/7 15:47
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NacosService {

    private List<Service> serviceList;

    private Integer count;

    public List<Service> getServiceList() {
        return serviceList;
    }

    public void setServiceList(List<Service> serviceList) {
        this.serviceList = serviceList;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Service {

        private String name;
        private String groupName;
        private Integer clusterCount;
        private Integer ipCount;
        private Integer healthyInstanceCount;
        private String triggerFlag;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public Integer getClusterCount() {
            return clusterCount;
        }

        public void setClusterCount(Integer clusterCount) {
            this.clusterCount = clusterCount;
        }

        public Integer getIpCount() {
            return ipCount;
        }

        public void setIpCount(Integer ipCount) {
            this.ipCount = ipCount;
        }

        public Integer getHealthyInstanceCount() {
            return healthyInstanceCount;
        }

        public void setHealthyInstanceCount(Integer healthyInstanceCount) {
            this.healthyInstanceCount = healthyInstanceCount;
        }

        public String getTriggerFlag() {
            return triggerFlag;
        }

        public void setTriggerFlag(String triggerFlag) {
            this.triggerFlag = triggerFlag;
        }
    }
}
