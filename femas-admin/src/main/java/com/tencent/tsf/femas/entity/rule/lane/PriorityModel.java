package com.tencent.tsf.femas.entity.rule.lane;

/**
 * @Author: cody
 * @Date: 2022/9/4
 * @Descriptioin
 */
public class PriorityModel {

    String laneId;

    String targetLaneId;


    public String getLaneId() {
        return laneId;
    }

    public void setLaneId(String laneId) {
        this.laneId = laneId;
    }


    public String getTargetLaneId() {
        return targetLaneId;
    }

    public void setTargetLaneId(String targetLaneId) {
        this.targetLaneId = targetLaneId;
    }

}
