package com.tencent.tsf.femas.endpoint.rule.breaker;

import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.endpoint.adaptor.AbstractBaseEndpoint;
import com.tencent.tsf.femas.entity.PageService;
import com.tencent.tsf.femas.entity.rule.FemasCircuitBreakerRule;
import com.tencent.tsf.femas.entity.rule.RuleModel;
import com.tencent.tsf.femas.entity.rule.RuleSearch;
import com.tencent.tsf.femas.entity.rule.breaker.CircuitBreakerModel;
import com.tencent.tsf.femas.enums.ServiceInvokeEnum;
import com.tencent.tsf.femas.storage.DataOperation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Auther: yrz
 * @Date: 2021/05/08/13:12
 * @Descriptioin
 */
@RestController
@RequestMapping("atom/v1/breaker")
@Api(tags = "服务熔断规则配置模块")
public class BreakerEndpoint extends AbstractBaseEndpoint {

    private final DataOperation dataOperation;

    public BreakerEndpoint(DataOperation dataOperation) {
        this.dataOperation = dataOperation;
    }

    @PostMapping("fetchBreakerRuleById")
    @ApiOperation("查询单条服务熔断规则")
    public Result<FemasCircuitBreakerRule> fetchBreakerRuleById(@RequestBody RuleSearch ruleSearch) {
        return executor.process(() -> {
            FemasCircuitBreakerRule circuitBreakerRule = dataOperation.fetchBreakerRuleById(ruleSearch);
            if (circuitBreakerRule == null) {
                return Result.errorMessage("数据不存在");
            }
            return Result.successData(circuitBreakerRule);
        });
    }


    @PostMapping("fetchBreakerRule")
    @ApiOperation("查询服务熔断规则")
    public Result<PageService<List<FemasCircuitBreakerRule>>> fetchBreakerRule(
            @RequestBody CircuitBreakerModel serviceModel) {
        return executor.invoke(ServiceInvokeEnum.ApiInvokeEnum.SERVICE_BREAK_FETCH, serviceModel);
    }

    @PostMapping("deleteBreakerRule")
    @ApiOperation("删除服务熔断规则")
    public Result deleteBreakerRule(@RequestBody RuleModel ruleModel) {
        return executor.invoke(ServiceInvokeEnum.ApiInvokeEnum.SERVICE_BREAK_DELETE, ruleModel);
    }

    @PostMapping("configureBreakerRule")
    @ApiOperation("修改、新建熔断规则")
    public Result configureBreakerRule(@RequestBody FemasCircuitBreakerRule breakerRule) {
        return executor.invoke(ServiceInvokeEnum.ApiInvokeEnum.SERVICE_BREAK_CONFIG, breakerRule);
    }

}
