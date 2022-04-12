package com.tencent.tsf.femas.entity.dcfg;

import com.tencent.tsf.femas.util.MapUtil;

/**
 * @author jianzhi
 * @date 2021/8/16 11:03
 */
public class ConfigVersion {

    private String configVersionId;
    private String configId;

    /**
     * 版本号
     */
    private int configVersion;
    private String configValue;
    private long createTime;
    private long releaseTime;
    /**
     * U: 未发布； S：发布成功；F：发布失败；RS：回滚成功；RF：回滚失败；DS：删除成功；DF：删除失败；
     */
    private String releaseStatus;

    public String getConfigVersionId() {
        return configVersionId;
    }

    public void setConfigVersionId(String configVersionId) {
        this.configVersionId = configVersionId;
    }

    public int getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(int configVersion) {
        this.configVersion = configVersion;
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getReleaseStatus() {
        return releaseStatus;
    }

    public void setReleaseStatus(String releaseStatus) {
        this.releaseStatus = releaseStatus;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
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
