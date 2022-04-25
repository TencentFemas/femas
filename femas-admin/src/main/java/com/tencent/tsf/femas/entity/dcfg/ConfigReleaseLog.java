package com.tencent.tsf.femas.entity.dcfg;

import com.tencent.tsf.femas.util.MapUtil;

/**
 * @author jianzhi
 * @date 2021/8/16 11:03
 */
public class ConfigReleaseLog {

    private Long id;
    private String configId;
    private String configVersionId;
    private String lastConfigVersionId;
    private long releaseTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getConfigVersionId() {
        return configVersionId;
    }

    public void setConfigVersionId(String configVersionId) {
        this.configVersionId = configVersionId;
    }

    public String getLastConfigVersionId() {
        return lastConfigVersionId;
    }

    public void setLastConfigVersionId(String lastConfigVersionId) {
        this.lastConfigVersionId = lastConfigVersionId;
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
