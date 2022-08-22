package com.tencent.tsf.femas.entity.rule.lane;

import com.tencent.tsf.femas.entity.ServiceInfo;

/**
 * @Author: cody
 * @Date: 2022/7/26
 * @Descriptioin
 */
public class LaneServiceInfo {

    /**
     * 服务
     */
    private ServiceInfo serviceInfo;

    /**
     * 是否入口应用
     */
    private Boolean entrance;

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public Boolean getEntrance() {
        return entrance;
    }

    public void setEntrance(Boolean entrance) {
        this.entrance = entrance;
    }
}
