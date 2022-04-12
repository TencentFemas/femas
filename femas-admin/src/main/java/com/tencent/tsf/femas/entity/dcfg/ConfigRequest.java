package com.tencent.tsf.femas.entity.dcfg;

import com.tencent.tsf.femas.entity.Page;
import com.tencent.tsf.femas.util.MapUtil;
import java.util.List;

/**
 * @author jianzhi
 * @date 2021/8/16 11:03
 */
public class ConfigRequest extends Page {

    private String configId;
    private String configName;
    private String namespaceId;
    private String serviceName;
    private String systemTag;
    private String ConfigDesc;
    private String configType;
    private String configValue;
    private String configVersionId;
    private String command;
    private String searchWord;
    private String releaseStatus;
    private int orderType;
    private String orderBy;
    private List<String> configIdList;
    private List<String> configVersionIdList;
    private long createTime = System.currentTimeMillis();
    private long releaseTime;
    private String lastReleaseVersionId;
    private String currentReleaseVersionId;

    public String getLastReleaseVersionId() {
        return lastReleaseVersionId;
    }

    public void setLastReleaseVersionId(String lastReleaseVersionId) {
        this.lastReleaseVersionId = lastReleaseVersionId;
    }

    public String getCurrentReleaseVersionId() {
        return currentReleaseVersionId;
    }

    public void setCurrentReleaseVersionId(String currentReleaseVersionId) {
        this.currentReleaseVersionId = currentReleaseVersionId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public List<String> getConfigIdList() {
        return configIdList;
    }

    public void setConfigIdList(List<String> configIdList) {
        this.configIdList = configIdList;
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

    public String getConfigDesc() {
        return ConfigDesc;
    }

    public void setConfigDesc(String configDesc) {
        ConfigDesc = configDesc;
    }

    public int getOrderType() {
        return orderType;
    }

    public void setOrderType(int orderType) {
        this.orderType = orderType;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigVersionId() {
        return configVersionId;
    }

    public void setConfigVersionId(String configVersionId) {
        this.configVersionId = configVersionId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getSearchWord() {
        return searchWord;
    }

    public void setSearchWord(String searchWord) {
        this.searchWord = searchWord;
    }

    public String getReleaseStatus() {
        return releaseStatus;
    }

    public void setReleaseStatus(String releaseStatus) {
        this.releaseStatus = releaseStatus;
    }

    public List<String> getConfigVersionIdList() {
        return configVersionIdList;
    }

    public void setConfigVersionIdList(List<String> configVersionIdList) {
        this.configVersionIdList = configVersionIdList;
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
