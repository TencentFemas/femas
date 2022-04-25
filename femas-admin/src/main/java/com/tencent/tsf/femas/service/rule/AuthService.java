package com.tencent.tsf.femas.service.rule;

import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.entity.PageService;
import com.tencent.tsf.femas.entity.rule.FemasAuthRule;
import com.tencent.tsf.femas.entity.rule.auth.AuthRuleModel;
import com.tencent.tsf.femas.entity.rule.auth.ServiceAuthRuleModel;
import com.tencent.tsf.femas.service.ServiceExecutor;
import com.tencent.tsf.femas.storage.DataOperation;
import com.tencent.tsf.femas.util.ResultCheck;
import org.springframework.stereotype.Service;


/**
 * @Auther: cody
 * @Date: 2021/05/08/13:22
 * @Descriptioin
 */
@Service
public class AuthService implements ServiceExecutor {

    private final DataOperation dataOperation;

    public AuthService(DataOperation dataOperation) {
        this.dataOperation = dataOperation;
    }


    public Result configureAuthRule(FemasAuthRule authRule) {
        int res = dataOperation.configureAuthRule(authRule);
        if (ResultCheck.checkCount(res)) {
            return Result.successMessage("服务鉴权规则配置成功");
        }
        return Result.errorMessage("服务鉴权规则配置失败");
    }

    public Result<PageService<FemasAuthRule>> fetchAuthRule(AuthRuleModel authRuleModel) {
        PageService<FemasAuthRule> authRules = dataOperation.fetchAuthRulePages(authRuleModel);
        return Result.successData(authRules);
    }

    public Result deleteAuthRule(ServiceAuthRuleModel serviceAuthRuleModel) {
        int res = dataOperation.deleteAuthRule(serviceAuthRuleModel);
        if (ResultCheck.checkCount(res)) {
            return Result.successMessage("删除成功");
        }
        return Result.errorMessage("删除失败");
    }
}
