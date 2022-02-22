package com.tencent.tsf.femas.governance.lane;

import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.governance.config.FemasPluginContext;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 泳道能力是给全链路灰度发布来使用的
 * 需要搭配控制台来发布规则
 *
 * 目前要订阅消费全局泳道，性能有点差，等待优化
 * 暂时不支持单个应用使用
 */
public class LaneService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LaneService.class);

    private static volatile LaneFilter LANE_FILTER = FemasPluginContext.getLaneFilter();

    public static void headerPreprocess() {
        if (LANE_FILTER != null) {
            LANE_FILTER.preProcessLaneId();
        }
    }

    public static List<ServiceInstance> filterInstancesWithLane(Service service,
            List<ServiceInstance> serviceInstances) {
        // RATE_LIMITER 不为空，且Tag规则命中，且限流不通过
        if (LANE_FILTER != null) {
            return LANE_FILTER.filterInstancesWithLane(service, serviceInstances);
        }

        return serviceInstances;
    }

    public static void refreshLaneFilter(LaneFilter laneFilter) {
        LANE_FILTER = laneFilter;
    }
}
