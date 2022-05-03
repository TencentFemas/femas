package com.tencent.tsf.femas.service.rule;

import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.entity.PageService;
import com.tencent.tsf.femas.entity.rule.FemasLimitRule;
import com.tencent.tsf.femas.entity.rule.RuleModel;
import com.tencent.tsf.femas.entity.rule.limit.LimitModel;
import com.tencent.tsf.femas.service.ServiceExecutor;
import com.tencent.tsf.femas.storage.DataOperation;
import com.tencent.tsf.femas.util.ResultCheck;
import java.util.List;
import org.springframework.stereotype.Service;


/**
 * @Auther: yrz
 * @Date: 2021/05/08/13:22
 * @Descriptioin
 */
@Service
public class LimitService implements ServiceExecutor {

    private final DataOperation dataOperation;

    public LimitService(DataOperation dataOperation) {
        this.dataOperation = dataOperation;
    }

    public Result configureLimitRule(FemasLimitRule limitRule) {
        // 局部限流单条限制
        if (limitRule.getType() == FemasLimitRule.Type.PART && limitRule.judgeStatus()) {
            LimitModel limitModel = new LimitModel();
            limitModel.setNamespaceId(limitRule.getNamespaceId());
            limitModel.setServiceName(limitRule.getServiceName());
            List<FemasLimitRule> femasLimitRules = dataOperation.fetchLimitRule(limitModel);
            if (femasLimitRules != null) {
                for (FemasLimitRule oldLimitRule : femasLimitRules) {
                    // 允许编辑
                    if (!StringUtils.isEmpty(limitRule.getRuleId())
                            && limitRule.getRuleId().equals(oldLimitRule.getRuleId())) {
                        continue;
                    }
                    if (oldLimitRule.getType() == FemasLimitRule.Type.PART
                            && oldLimitRule.judgeStatus() && oldLimitRule.isEqualTags(limitRule)) {
                        return Result.errorMessage("规则已存在，请勿重复创建");
                    }
                }
            }
        }
        int res = dataOperation.configureLimitRule(limitRule);
        if (ResultCheck.checkCount(res)) {
            return Result.successMessage("规则编辑成功");
        }
        return Result.errorMessage("规则编辑失败");
    }

    public Result<PageService<FemasLimitRule>> fetchLimitRule(LimitModel serviceModel) {
        PageService<FemasLimitRule> limitRules = dataOperation.fetchLimitRulePages(serviceModel);
        return Result.successData(limitRules);
    }

    public Result deleteLimitRule(RuleModel ruleModel) {
        int res = dataOperation.deleteLimitRule(ruleModel);
        if (ResultCheck.checkCount(res)) {
            return Result.successMessage("规则删除成功");
        }
        return Result.errorMessage("规则删除失败");
    }


}
