package com.tencent.tsf.femas.service.rule;

import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.entity.PageService;
import com.tencent.tsf.femas.entity.rule.lane.LaneInfo;
import com.tencent.tsf.femas.entity.rule.lane.LaneInfoModel;
import com.tencent.tsf.femas.entity.rule.lane.LaneRule;
import com.tencent.tsf.femas.entity.rule.lane.LaneRuleModel;
import com.tencent.tsf.femas.service.ServiceExecutor;
import com.tencent.tsf.femas.storage.DataOperation;
import org.springframework.stereotype.Service;

/**
 * @Author: cody
 * @Date: 2022/7/26
 * @Descriptioin
 */
@Service
public class LaneService implements ServiceExecutor {

    private final DataOperation dataOperation;

    public LaneService(DataOperation dataOperation) {
        this.dataOperation = dataOperation;
    }

    public Result configureLane(LaneInfo laneInfo) {
        Integer res = dataOperation.configureLane(laneInfo);
        if(res == 1){
            return Result.successMessage("泳道编辑成功");
        }
        return Result.errorMessage("泳道编辑失败");
    }

    public Result<LaneInfo> fetchLaneById(String laneId) {
        LaneInfo laneInfo = dataOperation.fetchLaneById(laneId);
        return Result.successData(laneInfo);
    }

    public Result<PageService<LaneInfo>> fetchLaneInfoPages(LaneInfoModel laneInfoModel) {
        PageService<LaneInfo> laneInfos = dataOperation.fetchLaneInfoPages(laneInfoModel);
        return Result.successData(laneInfos);
    }


    public Result deleteLane(String laneId) {
        Integer res = dataOperation.deleteLane(laneId);
        if(res == 0){
            return Result.errorMessage("泳道删除失败");
        }
        return Result.successMessage("泳道删除成功");
    }



    public Result configureLaneRule(LaneRule laneRule) {
        Integer res = dataOperation.configureLaneRule(laneRule);
        if(res == 1){
            return Result.successMessage("泳道规则编辑成功");
        }
        return Result.errorMessage("泳道规则编辑失败");
    }

    public Result<LaneRule> fetchLaneRuleById(String laneRuleId) {
        LaneRule laneRule = dataOperation.fetchLaneRuleById(laneRuleId);
        return Result.successData(laneRule);
    }

    public Result<PageService<LaneRule>> fetchLaneRulePages(LaneRuleModel laneRuleModel) {
        PageService<LaneRule> laneRules = dataOperation.fetchLaneRulePages(laneRuleModel);
        return Result.successData(laneRules);
    }

    public Result deleteLaneRule(String laneRuleId) {
        Integer res = dataOperation.deleteLaneRule(laneRuleId);
        if(res == 0){
            return Result.errorMessage("泳道规则删除失败");
        }
        return Result.successMessage("泳道规则删除成功");
    }
}
