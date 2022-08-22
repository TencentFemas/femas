package com.tencent.tsf.femas.entity.rule.lane;

import com.tencent.tsf.femas.entity.Page;

/**
 * @Author: cody
 * @Date: 2022/7/28
 * @Descriptioin
 */
public class LaneInfoModel extends Page {

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
}
