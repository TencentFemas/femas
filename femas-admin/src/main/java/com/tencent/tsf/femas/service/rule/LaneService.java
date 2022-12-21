package com.tencent.tsf.femas.service.rule;

import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.entity.PageService;
import com.tencent.tsf.femas.entity.rule.lane.*;
import com.tencent.tsf.femas.event.ConfigUpdateEvent;
import com.tencent.tsf.femas.event.EventPublishCenter;
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

    private final EventPublishCenter eventPublishCenter;

    public LaneService(DataOperation dataOperation, EventPublishCenter eventPublishCenter) {
        this.dataOperation = dataOperation;
        this.eventPublishCenter = eventPublishCenter;
    }

    public Result configureLane(LaneInfo laneInfo) {
        Integer res = dataOperation.configureLane(laneInfo);
        if(res == 1){
            eventPublishCenter.publishEvent(new ConfigUpdateEvent("lane-info", "lane-info"));
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
        eventPublishCenter.publishEvent(new ConfigUpdateEvent("lane-info", "lane-info"));
        return Result.successMessage("泳道删除成功");
    }



    public Result configureLaneRule(LaneRule laneRule) {
        if(laneRule.getGrayType() == GrayTypeEnum.TAG && CollectionUtil.isEmpty(laneRule.getRuleTagList())){
            return Result.errorMessage("蓝绿灰度类型的tag至少需要一条");
        }
        if(laneRule.getGrayType() == GrayTypeEnum.CANARY){
            Integer totalRate = 0;
            for(Integer rate: laneRule.getRelativeLane().values()){
                totalRate += rate;
            }
            if(totalRate != 100){
                return Result.errorMessage("泳道百分比之和应为100");
            }
        }
        Integer res = dataOperation.configureLaneRule(laneRule);
        if(res == 1){
            eventPublishCenter.publishEvent(new ConfigUpdateEvent("lane-rule", "lane-rule"));
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
        eventPublishCenter.publishEvent(new ConfigUpdateEvent("lane-rule", "lane-rule"));
        return Result.successMessage("泳道规则删除成功");
    }


    public Result adjustLaneRulePriority(PriorityModel priorityModel) {
        Result<LaneRule> laneRuleResult = fetchLaneRuleById(priorityModel.getLaneId());
        Result<LaneRule> targetLaneRuleResult = fetchLaneRuleById(priorityModel.getTargetLaneId());
        if(!Result.SUCCESS.equals(laneRuleResult.getCode()) || !Result.SUCCESS.equals(targetLaneRuleResult.getCode()) ){
            return Result.errorMessage("泳道不存在");
        }
        LaneRule laneRule = laneRuleResult.getData();
        LaneRule targetLaneRule = targetLaneRuleResult.getData();
        Long laneRulePriority = laneRule.getPriority();
        laneRule.setPriority(targetLaneRule.getPriority());
        targetLaneRule.setPriority(laneRulePriority);
        if(!Result.SUCCESS.equals(configureLaneRule(laneRule).getCode()) || !Result.SUCCESS.equals(configureLaneRule(targetLaneRule).getCode()) ){
            return Result.errorMessage("优先级调整失败");
        }
        eventPublishCenter.publishEvent(new ConfigUpdateEvent("lane-rule", "lane-rule"));
        return Result.successData("优先级调整成功");
    }
}
