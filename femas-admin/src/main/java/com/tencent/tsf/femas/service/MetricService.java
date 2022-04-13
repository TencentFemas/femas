package com.tencent.tsf.femas.service;

import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.entity.metrix.model.MetricModel;
import com.tencent.tsf.femas.entity.metrix.view.RateLimitMetricVo;
import com.tencent.tsf.femas.entity.metrix.view.RouteMetricVo;


/**
 * @author Cody
 * @date 2021 2021/8/15 5:58 下午
 */
public interface MetricService<T> {

    /**
     * 限流指标统计
     *
     * @return
     */
    Result<RateLimitMetricVo> fetchRateLimitMetric(MetricModel metricModel);

    /**
     * 路由指标统计
     *
     * @param metricModel
     * @return
     */
    Result<RouteMetricVo> fetchRouteMetric(MetricModel metricModel);
}
