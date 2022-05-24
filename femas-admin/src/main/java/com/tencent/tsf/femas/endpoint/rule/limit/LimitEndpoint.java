package com.tencent.tsf.femas.endpoint.rule.limit;

import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.endpoint.adaptor.AbstractBaseEndpoint;
import com.tencent.tsf.femas.entity.PageService;
import com.tencent.tsf.femas.entity.rule.FemasLimitRule;
import com.tencent.tsf.femas.entity.rule.RuleModel;
import com.tencent.tsf.femas.entity.rule.RuleSearch;
import com.tencent.tsf.femas.entity.rule.limit.LimitModel;
import com.tencent.tsf.femas.service.rule.LimitService;
import com.tencent.tsf.femas.storage.DataOperation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Auther: yrz
 * @Date: 2021/05/08/15:12
 * @Descriptioin
 */
@RestController
@RequestMapping("atom/v1/limit")
@Api(tags = "服务限流规则")
public class LimitEndpoint extends AbstractBaseEndpoint {

    private final DataOperation dataOperation;

    @Autowired
    LimitService limitService;

    public LimitEndpoint(DataOperation dataOperation) {
        this.dataOperation = dataOperation;
    }

    @PostMapping("fetchLimitRuleById")
    @ApiOperation("查询单条服务限流规则")
    public Result<FemasLimitRule> fetchLimitRuleById(@RequestBody RuleSearch ruleSearch) {
        return executor.process(() -> {
            FemasLimitRule limitRule = dataOperation.fetchLimitRuleById(ruleSearch);
            if (limitRule == null) {
                return Result.errorMessage("数据不存在");
            }
            return Result.successData(limitRule);
        });
    }

    @PostMapping("fetchLimitRule")
    @ApiOperation("查询服务限流规则")
    public Result<PageService<FemasLimitRule>> fetchLimitRule(@RequestBody LimitModel serviceModel) {
        return executor.process(()->limitService.fetchLimitRule(serviceModel));
    }

    @PostMapping("deleteLimitRule")
    @ApiOperation("删除服务限流规则")
    public Result deleteLimitRule(@RequestBody RuleModel ruleModel) {
        return executor.process(()->limitService.deleteLimitRule(ruleModel));
    }

    @PostMapping("configureLimitRule")
    @ApiOperation("新增，修改服务限流规则")
    public Result configureLimitRule(@RequestBody FemasLimitRule limitRule) {
        return executor.process(()->limitService.configureLimitRule(limitRule));
    }


}
