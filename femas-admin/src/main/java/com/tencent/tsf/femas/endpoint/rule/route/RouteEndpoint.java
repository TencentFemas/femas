package com.tencent.tsf.femas.endpoint.rule.route;

import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.endpoint.adaptor.AbstractBaseEndpoint;
import com.tencent.tsf.femas.entity.PageService;
import com.tencent.tsf.femas.entity.ServiceModel;
import com.tencent.tsf.femas.entity.rule.FemasRouteRule;
import com.tencent.tsf.femas.entity.rule.RuleModel;
import com.tencent.tsf.femas.entity.rule.RuleSearch;
import com.tencent.tsf.femas.entity.rule.route.Tolerate;
import com.tencent.tsf.femas.entity.rule.route.TolerateModel;
import com.tencent.tsf.femas.service.rule.RouteService;
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
@RequestMapping("atom/v1/route")
@Api(tags = "服务路由模块")
public class RouteEndpoint extends AbstractBaseEndpoint {

    private final DataOperation dataOperation;

    @Autowired
    RouteService routeService;

    public RouteEndpoint(DataOperation dataOperation) {
        this.dataOperation = dataOperation;
    }

    @PostMapping("fetchRouteRuleById")
    @ApiOperation("查询单条服务路由规则")
    public Result<FemasRouteRule> fetchRouteRuleById(@RequestBody RuleSearch ruleSearch) {
        return executor.process(() -> {
            FemasRouteRule routeRule = dataOperation.fetchRouteRuleById(ruleSearch);
            if (routeRule == null) {
                return Result.errorMessage("数据不存在");
            }
            return Result.successData(routeRule);
        });
    }

    @PostMapping("fetchRouteRule")
    @ApiOperation("查询服务路由规则")
    public Result<PageService<FemasRouteRule>> fetchRouteRule(@RequestBody ServiceModel serviceModel) {
        return executor.process(()-> routeService.fetchRouteRule(serviceModel));
    }

    @PostMapping("deleteRouteRule")
    @ApiOperation("删除服务路由规则")
    public Result deleteRouteRule(@RequestBody RuleModel ruleModel) {
        return executor.process(()-> routeService.deleteRouteRule(ruleModel));
    }

    @PostMapping("configureRouteRule")
    @ApiOperation("新建、修改服务路由规则")
    public Result configureRouteRule(@RequestBody FemasRouteRule routeRule) {
        return executor.process(()-> routeService.configureRouteRule(routeRule));
    }

    @PostMapping("fetchTolerant")
    @ApiOperation("查询是否开启容错")
    public Result fetchTolerant(@RequestBody TolerateModel tolerateModel) {
        return executor.process(() -> {
            boolean res = dataOperation.fetchTolerant(tolerateModel);
            return Result
                    .successData(new Tolerate(tolerateModel.getNamespaceId(), tolerateModel.getServiceName(), res));
        });
    }

    @PostMapping("configureTolerant")
    @ApiOperation("修改服务路由容错")
    public Result configureTolerant(@RequestBody Tolerate tolerate) {
        return executor.process(()-> routeService.configureTolerant(tolerate));
    }
}
