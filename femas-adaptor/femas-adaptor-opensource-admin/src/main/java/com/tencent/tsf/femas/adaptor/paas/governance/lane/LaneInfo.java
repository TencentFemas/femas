package com.tencent.tsf.femas.adaptor.paas.governance.lane;

import java.util.List;
import java.util.Objects;

/**
 * 泳道
 */
public class LaneInfo {

    /**
     * 泳道ID
     */
    private String laneId;

    /**
     * 泳道名称
     */
    private String laneName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 规则创建时间
     */
    private Long createTime;

    /**
     * 规则更新时间
     */
    private Long updateTime;

    /**
     * 泳道服务列表
     */
    private List<ServiceInfo> laneServiceList;


    public String getLaneId() {
        return laneId;
    }

    public void setLaneId(String laneId) {
        this.laneId = laneId;
    }

    public String getLaneName() {
        return laneName;
    }

    public void setLaneName(String laneName) {
        this.laneName = laneName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public List<ServiceInfo> getLaneServiceList() {
        return laneServiceList;
    }

    public void setLaneServiceList(List<ServiceInfo> laneServiceList) {
        this.laneServiceList = laneServiceList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LaneInfo laneInfo = (LaneInfo) o;
        return Objects.equals(laneId, laneInfo.laneId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(laneId);
    }
}
