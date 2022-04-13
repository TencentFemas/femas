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
 * @Date: 2021/5/7 15:55
 */
public class RegistryPageService {

    private List<ServiceBriefInfo> serviceBriefInfos;
    private Integer count;
    private Integer pageNo;
    private Integer pageSize;
    private String registryId;

    public List<ServiceBriefInfo> getServiceBriefInfos() {
        return serviceBriefInfos;
    }

    public void setServiceBriefInfos(List<ServiceBriefInfo> serviceBriefInfos) {
        this.serviceBriefInfos = serviceBriefInfos;
    }

    public String getRegistryId() {
        return registryId;
    }

    public void setRegistryId(String registryId) {
        this.registryId = registryId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
