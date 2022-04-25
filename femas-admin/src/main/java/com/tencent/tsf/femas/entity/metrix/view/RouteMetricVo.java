package com.tencent.tsf.femas.entity.metrix.view;

import com.tencent.tsf.femas.entity.metrix.TimeSeries;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.List;

/**
 * @author Cody
 * @date 2021 2021/8/19 6:45 下午
 */
public class RouteMetricVo {

    @ApiModelProperty("命名空间")
    private String namespaceId;

    @ApiModelProperty("服务名")
    private String serviceName;

    @ApiModelProperty("不同版本流量指标")
    private HashMap<String, List<TimeSeries>> flowMetric;

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

    public HashMap<String, List<TimeSeries>> getFlowMetric() {
        return flowMetric;
    }

    public void setFlowMetric(HashMap<String, List<TimeSeries>> flowMetric) {
        this.flowMetric = flowMetric;
    }
}
