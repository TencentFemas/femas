package com.tencent.tsf.femas.adaptor.paas.governance.lane;

import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.common.util.StringUtils;

import java.util.List;

/**
 * 兼容原sdk接口，内部实现与 TsfLaneFilter 一致
 */
public class LaneIdHolder {

    public static String getLaneId() {
        String laneId = Context.getRpcInfo().get(ContextConstant.LANE_ID_TAG);
        if (StringUtils.isEmpty(laneId)) {
            return "";
        } else {
            return laneId;
        }
    }

    public static void setLaneId(String laneId) {
        Context.getRpcInfo().put(ContextConstant.LANE_ID_TAG, laneId);
    }

    public static String getUpstreamLaneId() {
        String upstreamLaneId = Context.getRpcInfo().get(FemasLaneFilter.SOURCE_LANE_ID_TAG);
        if (StringUtils.isEmpty(upstreamLaneId)) {
            return "";
        } else {
            return upstreamLaneId;
        }
    }

    public static void setUpstreamLaneId(String laneId) {
        Context.putSourceTag(ContextConstant.LANE_ID_TAG, laneId);
    }

    /**
     * 获取当前部署组所在泳道id，避免直接调用 TsfLaneFilter
     * @return
     */
    public static String getCurrentGroupLaneId() {
        List<String> laneIds = FemasLaneFilter.getCurrentGroupLaneIds();
        if (CollectionUtil.isNotEmpty(laneIds)) {
            return laneIds.get(0);
        } else {
            return "";
        }
    }

    public static List<String> getCurrentGroupLaneIds() {
        return FemasLaneFilter.getCurrentGroupLaneIds();
    }

}
