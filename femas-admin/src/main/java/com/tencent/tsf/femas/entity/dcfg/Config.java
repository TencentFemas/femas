package com.tencent.tsf.femas.entity.dcfg;

import com.tencent.tsf.femas.util.MapUtil;

/**
 * @author jianzhi
 * @date 2021/8/16 11:03
 */
public class Config {

    private String configId;
    private String configName;
    private String namespaceId;
    private String namespaceName;
    private String serviceName;
    private String systemTag;
    private String ConfigDesc;
    private String configType;
    private long createTime;
    private long releaseTime;

    private int versionCount;

    /**
     * 当前发布版本
     */
    private ConfigVersion currentReleaseVersion;

    private String currentReleaseVersionId;
    /**
     * 上次发布版本
     */
    private ConfigVersion lastReleaseVersion;

    private String lastReleaseVersionId;


    public String getCurrentReleaseVersionId() {
        return currentReleaseVersionId;
    }

    public void setCurrentReleaseVersionId(String currentReleaseVersionId) {
        this.currentReleaseVersionId = currentReleaseVersionId;
    }

    public String getLastReleaseVersionId() {
        return lastReleaseVersionId;
    }

    public void setLastReleaseVersionId(String lastReleaseVersionId) {
        this.lastReleaseVersionId = lastReleaseVersionId;
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getSystemTag() {
        return systemTag;
    }

    public void setSystemTag(String systemTag) {
        this.systemTag = systemTag;
    }

    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String namespaceName) {
        this.namespaceName = namespaceName;
    }

    public String getConfigDesc() {
        return ConfigDesc;
    }

    public void setConfigDesc(String configDesc) {
        ConfigDesc = configDesc;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }


    public int getVersionCount() {
        return versionCount;
    }

    public void setVersionCount(int versionCount) {
        this.versionCount = versionCount;
    }


    public ConfigVersion getCurrentReleaseVersion() {
        return currentReleaseVersion;
    }

    public void setCurrentReleaseVersion(ConfigVersion currentReleaseVersion) {
        this.currentReleaseVersion = currentReleaseVersion;
    }

    public ConfigVersion getLastReleaseVersion() {
        return lastReleaseVersion;
    }

    public void setLastReleaseVersion(ConfigVersion lastReleaseVersion) {
        this.lastReleaseVersion = lastReleaseVersion;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(long releaseTime) {
        this.releaseTime = releaseTime;
    }

    @Override
    public String toString() {
        return MapUtil.getMapValue(true, this).toString();
    }

}
