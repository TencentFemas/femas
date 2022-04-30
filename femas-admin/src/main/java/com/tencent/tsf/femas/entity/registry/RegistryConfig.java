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

import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.exception.RegistryConfigErrorException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;


/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/4/27 20:19
 */
@ApiModel(value = "注册中心配置对象")
public class RegistryConfig implements Serializable {


    public static final String REGISTRY_HOST = "registryHost";

    public static final String REGISTRY_PORT = "registryPort";

    /**
     * registry id   ex:ins-xxx
     */
    @ApiModelProperty(value = "注册中心ID,后端生成（新增不传 修改时传）")
    private String registryId;

    /**
     * cluster addr ex:http://127.0.0.1:8848
     */
    @ApiModelProperty(value = "注册中心地址,ex: 127.0.0.1:8500,127.0.0.2:8500,127.0.0.3:8500")
    private String registryCluster;

    /**
     * cluster name
     */
    @ApiModelProperty(value = "注册中心名称")
    private String registryName;

    /**
     * cluster type ex:consul
     */
    @ApiModelProperty(value = "注册中心类型，ex:consul")
    private String registryType;

    @ApiModelProperty("注册中心状态 1：运行中 2：异常")
    private Integer status;

    @ApiModelProperty("实例数")
    private Integer instanceCount;

    /**
     * k8s认证方式，ex:config、account
     */
    @ApiModelProperty(value = "k8s认证方式，ex:config、account")
    private String certificateType;

    /**
     * k8s配置信息
     */
    @ApiModelProperty(value = "k8s配置信息")
    private String kubeConfig;

    /**
     * k8s secret
     */
    @ApiModelProperty(value = "k8s secret")
    private String secret;

    /**
     * api地址信息
     */
    @ApiModelProperty(value = "k8s api地址信息")
    private String apiServerAddr;

    @ApiModelProperty(value = "nacos username")
    private String username;

    @ApiModelProperty(value = "nacos password")
    private String password;


    public String getApiServerAddr() {
        return apiServerAddr;
    }

    public void setApiServerAddr(String apiServerAddr) {
        this.apiServerAddr = apiServerAddr;
    }

    public String getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(String certificateType) {
        this.certificateType = certificateType;
    }

    public String getKubeConfig() {
        return kubeConfig;
    }

    public void setKubeConfig(String kubeConfig) {
        this.kubeConfig = kubeConfig;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Integer getInstanceCount() {
        return instanceCount;
    }

    public void setInstanceCount(Integer instanceCount) {
        this.instanceCount = instanceCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRegistryId() {
        return registryId;
    }

    public void setRegistryId(String registryId) {
        this.registryId = registryId;
    }

    public String getRegistryCluster() {
        return registryCluster;
    }

    public void setRegistryCluster(String registryCluster) {
        this.registryCluster = registryCluster;
    }

    public String getRegistryName() {
        return registryName;
    }

    public void setRegistryName(String registryName) {
        this.registryName = registryName;
    }

    public String getRegistryType() {
        return registryType;
    }

    public List<String> convertClusterList() {
        List<String> urlList = null;
        if (StringUtils.isNotEmpty(registryCluster)) {
            String[] clusters = registryCluster.split(",");
            if (clusters.length > 0) {
                urlList = new ArrayList<>(clusters.length);
//                Collections.addAll(urlList, clusters);
                for (String url: clusters) {
                    urlList.add(convertUrl(url));
                }
            }
        }
        return urlList;
    }

    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistryConfig that = (RegistryConfig) o;
        return Objects.equals(registryId, that.registryId) &&
                Objects.equals(registryCluster, that.registryCluster) &&
                Objects.equals(registryName, that.registryName) &&
                Objects.equals(registryType, that.registryType) &&
                Objects.equals(status, that.status) &&
                Objects.equals(instanceCount, that.instanceCount) &&
                Objects.equals(certificateType, that.certificateType) &&
                Objects.equals(kubeConfig, that.kubeConfig) &&
                Objects.equals(secret, that.secret) &&
                Objects.equals(apiServerAddr, that.apiServerAddr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(registryId, registryCluster, registryName, registryType, status, instanceCount, certificateType, kubeConfig, secret, apiServerAddr);
    }

    private String convertUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        if (url.endsWith("/")) {
            url = url.substring(url.lastIndexOf("/"));
        }
        return url;
    }
}
