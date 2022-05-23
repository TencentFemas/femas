package com.tencent.tsf.femas.endpoint.rule.auth;

import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.endpoint.adaptor.AbstractBaseEndpoint;
import com.tencent.tsf.femas.entity.PageService;
import com.tencent.tsf.femas.entity.rule.FemasAuthRule;
import com.tencent.tsf.femas.entity.rule.RuleSearch;
import com.tencent.tsf.femas.entity.rule.auth.AuthRuleModel;
import com.tencent.tsf.femas.entity.rule.auth.ServiceAuthRuleModel;
import com.tencent.tsf.femas.service.rule.AuthService;
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
 * @Date: 2021/05/08/15:57
 * @Descriptioin
 */
@RestController
@RequestMapping("atom/v1/auth")
@Api(tags = "服务鉴权模块")
public class AuthEndpoint extends AbstractBaseEndpoint {

    private final DataOperation dataOperation;

    @Autowired
    AuthService authService;

    public AuthEndpoint(DataOperation dataOperation) {
        this.dataOperation = dataOperation;
    }

    @PostMapping("fetchAuthRuleById")
    @ApiOperation("查询单条服务鉴权规则")
    public Result<FemasAuthRule> fetchAuthRuleById(@RequestBody RuleSearch ruleSearch) {
        return executor.process(() -> {
            FemasAuthRule authRule = dataOperation.fetchAuthRuleById(ruleSearch);
            if (authRule == null) {
                return Result.errorMessage("数据不存在");
            }
            return Result.successData(authRule);
        });
    }

    @PostMapping("fetchAuthRule")
    @ApiOperation("查询服务鉴权规则")
    public Result<PageService<FemasAuthRule>> fetchAuthRule(@RequestBody AuthRuleModel serviceModel) {
        return executor.process(()->authService.fetchAuthRule(serviceModel));
    }

    @PostMapping("deleteAuthRule")
    @ApiOperation("删除服务鉴权规则")
    public Result deleteAuthRule(@RequestBody ServiceAuthRuleModel serviceAuthRuleModel) {
        return executor.process(()->authService.deleteAuthRule(serviceAuthRuleModel));
    }

    @PostMapping("configureAuthRule")
    @ApiOperation("新建、修改服务鉴权规则")
    public Result configureAuthRule(@RequestBody FemasAuthRule authRule) {
        return executor.process(()->authService.configureAuthRule(authRule));
    }
}
