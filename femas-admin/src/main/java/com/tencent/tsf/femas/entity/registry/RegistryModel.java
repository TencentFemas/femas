package com.tencent.tsf.femas.entity.registry;

import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;

public class RegistryModel {

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
    @ApiModelProperty(value = "注册中心类型，ex:consul、K8s")
    private String registryType;

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
     * api地址信息
     */
    @ApiModelProperty(value = "k8s api地址信息")
    private String apiServerAddr;

    /**
     * k8s secret
     */
    @ApiModelProperty(value = "k8s secret")
    private String secret;


    public String getApiServerAddr() {
        return apiServerAddr;
    }

    public void setApiServerAddr(String apiServerAddr) {
        this.apiServerAddr = apiServerAddr;
    }

    public String getRegistryId() {
        return registryId;
    }

    public void setRegistryId(String registryId) {
        this.registryId = registryId;
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

    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }

    public RegistryConfig toRegistryConfig() {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setRegistryId(this.registryId);
        registryConfig.setRegistryType(this.registryType);

        if (StringUtils.isNotEmpty(this.registryCluster)) {
            registryConfig.setRegistryCluster(this.registryCluster.trim());
        }
        if (StringUtils.isNotEmpty(this.registryName)) {
            registryConfig.setRegistryName(this.registryName.trim());
        }
        if (StringUtils.isNotEmpty(this.certificateType)) {
            registryConfig.setCertificateType(this.certificateType.trim());
        }
        if (StringUtils.isNotEmpty(this.secret)) {
            registryConfig.setSecret(this.secret.trim());
        }
        if (StringUtils.isNotEmpty(this.apiServerAddr)) {
            registryConfig.setApiServerAddr(this.apiServerAddr.trim());
        }
        registryConfig.setKubeConfig(this.kubeConfig);
        return registryConfig;
    }

    @Override
    public String toString() {
        return "注册中心：" + registryName + "，注册中心地址：" + registryCluster + "，类型：" + registryType;
    }
}
