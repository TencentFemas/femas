package com.tencent.tsf.femas.service.impl;

import static com.tencent.tsf.femas.constant.PromConstants.REMOTE_VERSION_TAG;

import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.entity.metrix.TimeSeries;
import com.tencent.tsf.femas.entity.metrix.model.MetricModel;
import com.tencent.tsf.femas.entity.metrix.prom.MetricResult;
import com.tencent.tsf.femas.entity.metrix.prom.PromResponse;
import com.tencent.tsf.femas.entity.metrix.view.RateLimitMetricVo;
import com.tencent.tsf.femas.entity.metrix.view.RouteMetricVo;
import com.tencent.tsf.femas.service.MetricService;
import com.tencent.tsf.femas.util.AdminTimeUtil;
import com.tencent.tsf.femas.util.PromDataUtil;
import java.util.HashMap;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @author Cody
 * @date 2021 2021/8/22 8:18 下午
 */
@Service
public class MetricServiceImpl implements MetricService {


    private final PromDataUtil promDataUtil;

    public MetricServiceImpl(PromDataUtil promDataUtil) {
        this.promDataUtil = promDataUtil;
    }

    /**
     * 路由实例流量统计
     *
     * @param metricModel
     * @return
     */
    @Override
    public Result<RateLimitMetricVo> fetchRateLimitMetric(MetricModel metricModel) {
        // 限制率查询
        String limitRateSql =
                "round(sum(rate(femas_http_ratelimit_count_total{local_namespace=\"" + metricModel.getNamespaceId()
                        + "\",local_service=\"" + metricModel.getServiceName() + "\"}[1m]))*100" +
                        "/sum(rate(femas_http_server_requests_seconds_count{local_namespace=\"" + metricModel
                        .getNamespaceId() + "\",local_service=\"" + metricModel.getServiceName() + "\"}[1m])),0.01)";
        PromResponse<MetricResult> rateLimitResponse = promDataUtil
                .queryByRange(limitRateSql, metricModel.getStartTime(), metricModel.getEndTime(),
                        metricModel.getStep());
        MetricResult rateLimitMetric = null;
        RateLimitMetricVo rateLimitMetricVo = new RateLimitMetricVo();
        if (rateLimitResponse != null && !CollectionUtil.isEmpty(rateLimitResponse.getData().getResult())) {
            rateLimitMetric = (MetricResult) rateLimitResponse.getData().getResult().get(0);
            rateLimitMetricVo.setLimitMetric(AdminTimeUtil.metricData2TimeSeries(rateLimitMetric.getValues()));
        }
        // 服务请求指标查询
        MetricResult requestMetric = promDataUtil
                .queryServiceRequestCountMetric(metricModel.getNamespaceId(), metricModel.getServiceName(),
                        metricModel.getStartTime(), metricModel.getEndTime(), metricModel.getStep());
        List<TimeSeries> timeSeries = AdminTimeUtil.metricData2TimeSeries(requestMetric.getValues());
        rateLimitMetricVo.setNamespaceId(metricModel.getNamespaceId());
        rateLimitMetricVo.setServiceName(metricModel.getServiceName());
        rateLimitMetricVo.setRequestMetric(timeSeries);
        return Result.successData(rateLimitMetricVo);
    }

    @Override
    public Result<RouteMetricVo> fetchRouteMetric(MetricModel metricModel) {
        String routeSql =
                "round(sum(increase(femas_http_client_requests_seconds_count{remote_namespace=\"" + metricModel
                        .getNamespaceId() + "\",remote_service=\"" + metricModel.getServiceName() + "\"}[1m])) by("
                        + REMOTE_VERSION_TAG + "),1)";
        PromResponse<MetricResult> response = promDataUtil
                .queryByRange(routeSql, metricModel.getStartTime(), metricModel.getEndTime(), metricModel.getStep());
        if (response == null || response.getData() == null) {
            return Result.successData(null);
        }
        List<MetricResult> routeMetric = response.getData().getResult();
        HashMap<String, List<TimeSeries>> versionMetric = new HashMap<>();
        if (!CollectionUtil.isEmpty(routeMetric)) {
            for (MetricResult metricResult : routeMetric) {
                if (!StringUtils.isEmpty(metricResult.getMetric().get(REMOTE_VERSION_TAG))) {
                    versionMetric.put(metricResult.getMetric().get(REMOTE_VERSION_TAG),
                            AdminTimeUtil.metricData2TimeSeries(metricResult.getValues()));
                }
            }
        }
        RouteMetricVo routeMetricVo = new RouteMetricVo();
        routeMetricVo.setFlowMetric(versionMetric);
        routeMetricVo.setServiceName(metricModel.getServiceName());
        routeMetricVo.setNamespaceId(metricModel.getNamespaceId());
        return Result.successData(routeMetricVo);
    }
}
