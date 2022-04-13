package com.tencent.tsf.femas.entity.metrix;


import io.swagger.annotations.ApiModelProperty;
import java.util.List;

/**
 * @author Cody
 * @date 2021 2021/8/19 5:07 下午
 */
public class ServiceMetric {

    @ApiModelProperty("命名空间")
    private String namespaceId;

    @ApiModelProperty("服务名")
    private String serviceName;

    @ApiModelProperty("时序指标")
    private List<TimeSeries> timeSeries;

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

    public List<TimeSeries> getTimeSeries() {
        return timeSeries;
    }

    public void setTimeSeries(List<TimeSeries> timeSeries) {
        this.timeSeries = timeSeries;
    }
}
