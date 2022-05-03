package com.tencent.tsf.femas.endpoint.metric;

import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.endpoint.adaptor.AbstractBaseEndpoint;
import com.tencent.tsf.femas.entity.metrix.model.MetricModel;
import com.tencent.tsf.femas.entity.metrix.model.ServiceMetricModel;
import com.tencent.tsf.femas.entity.metrix.model.ServiceOverviewMetricModel;
import com.tencent.tsf.femas.entity.metrix.view.RateLimitMetricVo;
import com.tencent.tsf.femas.entity.metrix.view.RouteMetricVo;
import com.tencent.tsf.femas.entity.metrix.view.ServiceMetricVo;
import com.tencent.tsf.femas.entity.metrix.view.ServiceOverviewMetricVo;
import com.tencent.tsf.femas.service.MetricService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Cody
 * @date 2021 2021/8/15 5:51 下午
 */
@RestController
@RequestMapping("/atom/v1/metric")
@Api(tags = "性能监控模块")
public class MetricEndpoint extends AbstractBaseEndpoint {


    private final MetricService metricService;
    @Value("${femas.metrics.grafana.addr}")
    private String metricsAddress;

    public MetricEndpoint(MetricService metricService) {
        this.metricService = metricService;
    }

    @PostMapping("fetchServiceOverViewMetric")
    public Result<List<ServiceOverviewMetricVo>> fetchServiceOverViewMetric(
            @RequestBody ServiceOverviewMetricModel serviceOverviewMetricModel) {
        return null;
    }

    @PostMapping("fetchInboundServiceMetric")
    public Result<ServiceMetricVo> fetchInboundServiceMetric(@RequestBody ServiceMetricModel serviceMetricModel) {
        return null;
    }

    @PostMapping("fetchOutboundServiceMetric")
    public Result<ServiceMetricVo> fetchOutboundServiceMetric(@RequestBody ServiceMetricModel serviceMetricModel) {
        return null;
    }

    @PostMapping("fetchRouteMetric")
    @ApiOperation("查询路由指标")
    public Result<RouteMetricVo> fetchRouteMetric(@RequestBody MetricModel metricModel) {
        return executor.process(() -> {
            return metricService.fetchRouteMetric(metricModel);
        });
    }

    @PostMapping("fetchRateLimitMetric")
    @ApiOperation("查询限流指标")
    public Result<RateLimitMetricVo> fetchRateLimitMetric(@RequestBody MetricModel metricModel) {
        return executor.process(() -> {
            return metricService.fetchRateLimitMetric(metricModel);
        });
    }

    @RequestMapping("fetchMetricGrafanaAddress")
    public Result<String> fetchMetricGrafanaAddress() {
        return Result.successData(metricsAddress);
    }
}
