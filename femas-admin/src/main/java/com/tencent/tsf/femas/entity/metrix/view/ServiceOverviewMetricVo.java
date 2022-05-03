package com.tencent.tsf.femas.entity.metrix.view;

import com.tencent.tsf.femas.common.entity.Service;

/**
 * @author Cody
 * @date 2021 2021/8/15 9:07 下午
 */
public class ServiceOverviewMetricVo {

    private Service service;

    private Long requestCount;

    private Double errorRate;

    private Double aveResponseTime;

    public Long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(Long requestCount) {
        this.requestCount = requestCount;
    }

    public Double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(Double errorRate) {
        this.errorRate = errorRate;
    }

    public Double getAveResponseTime() {
        return aveResponseTime;
    }

    public void setAveResponseTime(Double aveResponseTime) {
        this.aveResponseTime = aveResponseTime;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }
}
