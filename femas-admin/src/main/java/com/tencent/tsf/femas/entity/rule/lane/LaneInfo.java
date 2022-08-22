package com.tencent.tsf.femas.entity.rule.lane;


import com.tencent.tsf.femas.entity.ServiceInfo;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @Author: cody
 * @Date: 2022/7/26/17:04
 * @Descriptioin
 */
public class LaneInfo {

    /**
     * 泳道ID
     */
    @ApiModelProperty("泳道ID")
    private String laneId;

    /**
     * 泳道名称
     */
    @ApiModelProperty("泳道名称")
    private String laneName;

    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String remark;

    /**
     * 规则创建时间
     */
    @ApiModelProperty("规则创建时间(不需要前端传)")
    private Long createTime = System.currentTimeMillis();

    /**
     * 规则更新时间
     */
    @ApiModelProperty("规则更新时间(不需要前端传)")
    private Long updateTime = System.currentTimeMillis();

    /**
     * 泳道服务列表
     */
    @ApiModelProperty("")
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
}
