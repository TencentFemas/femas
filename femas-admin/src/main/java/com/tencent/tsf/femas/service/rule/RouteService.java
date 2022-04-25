package com.tencent.tsf.femas.service.rule;

import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.entity.PageService;
import com.tencent.tsf.femas.entity.ServiceModel;
import com.tencent.tsf.femas.entity.rule.FemasRouteRule;
import com.tencent.tsf.femas.entity.rule.RuleModel;
import com.tencent.tsf.femas.entity.rule.route.Tolerate;
import com.tencent.tsf.femas.service.ServiceExecutor;
import com.tencent.tsf.femas.storage.DataOperation;
import com.tencent.tsf.femas.util.ResultCheck;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * @Auther: yrz
 * @Date: 2021/05/08/13:22
 * @Descriptioin
 */
@Service
public class RouteService implements ServiceExecutor {

    private final DataOperation dataOperation;

    public RouteService(DataOperation dataOperation) {
        this.dataOperation = dataOperation;
    }

    public Result<PageService<FemasRouteRule>> fetchRouteRule(ServiceModel serviceModel) {
        PageService<FemasRouteRule> routeRules = dataOperation.fetchRouteRulePages(serviceModel);
        return Result.successData(routeRules);
    }

    public Result deleteRouteRule(RuleModel ruleModel) {
        int res = dataOperation.deleteRouteRule(ruleModel);
        if (ResultCheck.checkCount(res)) {
            return Result.successMessage("路由规则删除成功");
        }
        return Result.errorMessage("路由规则删除失败");
    }

    public Result configureRouteRule(@RequestBody FemasRouteRule routeRule) {
        int res = dataOperation.configureRouteRule(routeRule);
        if (ResultCheck.checkCount(res)) {
            return Result.successMessage("规则编辑成功");
        }
        return Result.errorMessage("规则编辑失败");
    }

    public Result configureTolerant(@RequestBody Tolerate tolerate) {
        int res = dataOperation.configureTolerant(tolerate);
        if (ResultCheck.checkCount(res)) {
            return Result.successData("修改成功");
        }
        return Result.errorMessage("修改失败");
    }
}
