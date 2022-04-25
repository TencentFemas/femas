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

package com.tencent.tsf.femas.entity.namespace;

import com.tencent.tsf.femas.common.util.StringUtils;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/5/7 11:34
 */
public class Namespace {

    @ApiModelProperty("命名空间名称")
    private String name;
    @ApiModelProperty("命名空间id")
    private String namespaceId;
    // 使用list存放registry 后期可能扩展为绑定多注册中心
    @ApiModelProperty("注册中心id")
    private List<String> registryId;
    @ApiModelProperty("备注信息")
    private String desc;
    @ApiModelProperty("关联服务数(不需要传)")
    private Integer serviceCount;

    public Integer getServiceCount() {
        return serviceCount;
    }

    public void setServiceCount(Integer serviceCount) {
        this.serviceCount = serviceCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public List<String> getRegistryId() {
        return registryId;
    }

    public void setRegistryId(List<String> registryId) {
        this.registryId = registryId;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public int hashCode() {
        return namespaceId.hashCode();
    }


    @Override
    public String toString() {
        return "注册中心：" + registryId + (StringUtils.isEmpty(namespaceId) ? "" : ("，命名空间：" + namespaceId)) + "，描述："
                + desc;
    }


}
