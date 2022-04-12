package com.tencent.tsf.femas.entity.metrix.view;

import com.tencent.tsf.femas.entity.metrix.TimeSeries;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;

/**
 * @author Cody
 * @date 2021 2021/8/19 5:10 下午
 */
public class RateLimitMetricVo {

    @ApiModelProperty("命名空间")
    private String namespaceId;

    @ApiModelProperty("服务名")
    private String serviceName;

    @ApiModelProperty("请求指标")
    private List<TimeSeries> requestMetric;

    @ApiModelProperty("限制指标")
    private List<TimeSeries> limitMetric;

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

    public List<TimeSeries> getRequestMetric() {
        return requestMetric;
    }

    public void setRequestMetric(List<TimeSeries> requestMetric) {
        this.requestMetric = requestMetric;
    }

    public List<TimeSeries> getLimitMetric() {
        return limitMetric;
    }

    public void setLimitMetric(List<TimeSeries> limitMetric) {
        this.limitMetric = limitMetric;
    }
}
