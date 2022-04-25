package com.tencent.tsf.femas.service.rule;

import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.entity.PageService;
import com.tencent.tsf.femas.entity.rule.FemasCircuitBreakerRule;
import com.tencent.tsf.femas.entity.rule.RuleModel;
import com.tencent.tsf.femas.entity.rule.breaker.CircuitBreakerModel;
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
public class BreakerService implements ServiceExecutor {


    private final DataOperation dataOperation;

    public BreakerService(DataOperation dataOperation) {
        this.dataOperation = dataOperation;
    }

    public Result configureBreakerRule(FemasCircuitBreakerRule circuitBreakerRule) {
        if (StringUtils.isEmpty(circuitBreakerRule.getIsEnable())) {
            circuitBreakerRule.setIsEnable("0");
        }
        if (circuitBreakerRule.judgeStatus()) {
            CircuitBreakerModel circuitBreakerModel = new CircuitBreakerModel();
            circuitBreakerModel.setNamespaceId(circuitBreakerRule.getNamespaceId());
            circuitBreakerModel.setServiceName(circuitBreakerRule.getServiceName());
            List<FemasCircuitBreakerRule> femasCircuitBreakerRules = dataOperation
                    .fetchBreakerRule(circuitBreakerModel);
            if (femasCircuitBreakerRules != null) {
                for (FemasCircuitBreakerRule oldBreakerRule : femasCircuitBreakerRules) {
                    // 修改操作
                    if (!StringUtils.isEmpty(circuitBreakerRule.getRuleId())
                            && circuitBreakerRule.getRuleId().equals(oldBreakerRule.getRuleId())) {
                        continue;
                    }
                    if (oldBreakerRule.judgeStatus()
                            && oldBreakerRule.getTargetServiceName()
                            .equals(circuitBreakerRule.getTargetServiceName())) {
                        return Result.errorMessage("规则已存在，请勿重复创建");
                    }
                }
            }
        }
        return dataOperation.configureBreakerRule(circuitBreakerRule);
    }

    public Result<PageService<FemasCircuitBreakerRule>> fetchBreakerRule(CircuitBreakerModel circuitBreakerModel) {
        PageService<FemasCircuitBreakerRule> breakerRules = dataOperation.fetchBreakerRulePages(circuitBreakerModel);
        return Result.successData(breakerRules);
    }

    public Result deleteBreakerRule(RuleModel ruleModel) {
        int res = dataOperation.deleteBreakerRule(ruleModel);
        if (ResultCheck.checkCount(res)) {
            return Result.successMessage("规则删除成功");
        }
        return Result.errorMessage("规则删除失败");
    }
}
