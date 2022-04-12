package com.tencent.tsf.femas.endpoint.rule.auth;

import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.endpoint.adaptor.AbstractBaseEndpoint;
import com.tencent.tsf.femas.entity.PageService;
import com.tencent.tsf.femas.entity.rule.FemasAuthRule;
import com.tencent.tsf.femas.entity.rule.RuleSearch;
import com.tencent.tsf.femas.entity.rule.auth.AuthRuleModel;
import com.tencent.tsf.femas.entity.rule.auth.ServiceAuthRuleModel;
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
 * @Date: 2021/05/08/15:57
 * @Descriptioin
 */
@RestController
@RequestMapping("atom/v1/auth")
@Api(tags = "服务鉴权模块")
public class AuthEndpoint extends AbstractBaseEndpoint {

    private final DataOperation dataOperation;

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
    public Result<PageService<List<FemasAuthRule>>> fetchAuthRule(@RequestBody AuthRuleModel serviceModel) {
        return executor.invoke(ServiceInvokeEnum.ApiInvokeEnum.SERVICE_AUTH_FETCH, serviceModel);
    }

    @PostMapping("deleteAuthRule")
    @ApiOperation("删除服务鉴权规则")
    public Result deleteAuthRule(@RequestBody ServiceAuthRuleModel serviceAuthRuleModel) {
        return executor.invoke(ServiceInvokeEnum.ApiInvokeEnum.SERVICE_AUTH_DELETE, serviceAuthRuleModel);
    }

    @PostMapping("configureAuthRule")
    @ApiOperation("新建、修改服务鉴权规则")
    public Result configureAuthRule(@RequestBody FemasAuthRule authRule) {
        return executor.invoke(ServiceInvokeEnum.ApiInvokeEnum.SERVICE_AUTH_CONFIG, authRule);
    }
}
