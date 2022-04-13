package com.tencent.tsf.femas.entity.rule;

import static com.tencent.tsf.femas.constant.AdminConstants.FEMAS_META_APPLICATION_VERSION_KEY;

import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.entity.pass.route.RouteDest;
import com.tencent.tsf.femas.entity.pass.route.RouteDestItem;
import com.tencent.tsf.femas.entity.pass.route.RouteRule;
import com.tencent.tsf.femas.entity.pass.route.RouteRuleGroup;
import com.tencent.tsf.femas.entity.rule.route.RouteTag;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;

public class FemasRouteRule {

    @ApiModelProperty("规则id")
    private String ruleId;

    @ApiModelProperty("命名空间id")
    private String namespaceId;

    @ApiModelProperty("服务名")
    private String serviceName;

    @ApiModelProperty("规则名")
    private String ruleName;

    @ApiModelProperty("生效状态 1开启 0 关闭")
    private String status;

    @ApiModelProperty("路由标签")
    private List<RouteTag> routeTag;

    @ApiModelProperty("创建时间")
    private Long createTime;

    @ApiModelProperty("生效时间")
    private Long updateTime;

    @ApiModelProperty("描述")
    private String desc;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }


    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<RouteTag> getRouteTag() {
        return routeTag;
    }

    public void setRouteTag(List<RouteTag> routeTag) {
        this.routeTag = routeTag;
    }

    public RouteRuleGroup toPassRule() {
        RouteRuleGroup routeRuleGroup = new RouteRuleGroup();
        routeRuleGroup.setRouteId(this.ruleId);
        routeRuleGroup.setRouteDesc(this.desc);
        routeRuleGroup.setRouteName(this.ruleName);
        routeRuleGroup.setNamespaceId(this.getNamespaceId());
        routeRuleGroup.setMicroserviceName(this.serviceName);
        ArrayList<RouteRule> routeRules = new ArrayList<>();
        this.getRouteTag().stream().forEach(s -> {
            RouteRule routeRule = new RouteRule();
            routeRule.setRuleName(this.ruleName);
            routeRule.setRouteId(this.ruleId);
            routeRule.setServiceName(this.getServiceName());
            routeRule.setNamespaceId(this.getNamespaceId());
            ArrayList<com.tencent.tsf.femas.entity.pass.route.RouteTag> routeTags = new ArrayList<>();
            ArrayList<RouteDest> routeDests = new ArrayList<>();
            s.getDestTag().forEach(s1 -> {
                RouteDest routeDest = new RouteDest();
                routeDest.setRouteRuleId(this.ruleId);
                routeDest.setDestWeight(s1.getWeight());
                RouteDestItem routeDestItem = new RouteDestItem();
                routeDestItem.setDestItemField(FEMAS_META_APPLICATION_VERSION_KEY);// 只支持版本号
                routeDestItem.setDestItemValue(s1.getServiceVersion());
                ArrayList<RouteDestItem> routeDestItems = new ArrayList<>();
                routeDestItems.add(routeDestItem);
                routeDest.setDestItemList(routeDestItems);
                routeDests.add(routeDest);
            });
            s.getTags().forEach(s1 -> {
                com.tencent.tsf.femas.entity.pass.route.RouteTag routeTag = new com.tencent.tsf.femas.entity.pass.route.RouteTag();
                routeTag.setRouteRuleId(this.ruleId);
                routeTag.setTagField(s1.getTagField());
                routeTag.setTagOperator(s1.getTagOperator());
                routeTag.setTagValue(s1.getTagValue());
                routeTag.setTagType(s1.getTagType());
                routeTags.add(routeTag);
            });
            routeRule.setTagList(routeTags);
            routeRule.setDestList(routeDests);
            routeRules.add(routeRule);
        });
        routeRuleGroup.setRuleList(routeRules);
        return routeRuleGroup;
    }

    @Override
    public int hashCode() {
        return ruleId.hashCode();
    }

    @Override
    public String toString() {
        return "命名空间：" + namespaceId + "，服务：" + serviceName + "，规则：" + ruleName + "， 生效状态：" + ("1".equalsIgnoreCase(status) ? "开启" : "关闭");
    }
}
