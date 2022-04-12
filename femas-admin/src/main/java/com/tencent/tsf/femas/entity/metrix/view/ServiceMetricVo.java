package com.tencent.tsf.femas.entity.metrix.view;

import com.tencent.tsf.femas.entity.metrix.CallKindMetric;
import com.tencent.tsf.femas.entity.metrix.HttpStatusMetric;
import com.tencent.tsf.femas.entity.metrix.ResponseMetric;
import com.tencent.tsf.femas.entity.metrix.TimeSeries;
import java.util.List;

/**
 * @author Cody
 * @date 2021 2021/8/15 10:01 下午
 */
public class ServiceMetricVo {

    private List<TimeSeries> requestCount;

    private List<TimeSeries> errorRate;

    private ResponseMetric responseTime;

    private HttpStatusMetric httpStatusMetrics;

    private CallKindMetric callKindMetric;

    // 响应耗时分布


    public List<TimeSeries> getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(List<TimeSeries> requestCount) {
        this.requestCount = requestCount;
    }

    public List<TimeSeries> getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(List<TimeSeries> errorRate) {
        this.errorRate = errorRate;
    }

    public ResponseMetric getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(ResponseMetric responseTime) {
        this.responseTime = responseTime;
    }

    public HttpStatusMetric getHttpStatusMetrics() {
        return httpStatusMetrics;
    }

    public void setHttpStatusMetrics(HttpStatusMetric httpStatusMetrics) {
        this.httpStatusMetrics = httpStatusMetrics;
    }

    public CallKindMetric getCallKindMetric() {
        return callKindMetric;
    }

    public void setCallKindMetric(CallKindMetric callKindMetric) {
        this.callKindMetric = callKindMetric;
    }
}
