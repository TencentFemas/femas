package com.tencent.tsf.femas.service.rule;

import com.tencent.tsf.femas.common.serialize.JSONSerializer;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.entity.ServiceModel;
import com.tencent.tsf.femas.entity.pass.GetValue;
import com.tencent.tsf.femas.entity.pass.auth.AuthRule;
import com.tencent.tsf.femas.entity.pass.auth.AuthRuleGroup;
import com.tencent.tsf.femas.entity.pass.route.RouteRuleGroup;
import com.tencent.tsf.femas.entity.rule.FemasAuthRule;
import com.tencent.tsf.femas.entity.rule.FemasCircuitBreakerRule;
import com.tencent.tsf.femas.entity.rule.FemasLimitRule;
import com.tencent.tsf.femas.entity.rule.FemasRouteRule;
import com.tencent.tsf.femas.entity.rule.breaker.CircuitBreakerModel;
import com.tencent.tsf.femas.entity.rule.lane.LaneInfo;
import com.tencent.tsf.femas.entity.rule.lane.LaneRule;
import com.tencent.tsf.femas.entity.rule.limit.LimitModel;
import com.tencent.tsf.femas.entity.rule.route.TolerateModel;
import com.tencent.tsf.femas.storage.DataOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 适配sdk规则数据结构
 */
@Component
public class ConvertService {

    private static final Logger log = LoggerFactory.getLogger(ConvertService.class);

    private final DataOperation dataOperation;

    public ConvertService(DataOperation dataOperation) {
        this.dataOperation = dataOperation;
    }

    public String getKeyType(String key) {
        if (key.startsWith("authority")) {
            return "authority";
        } else if (key.startsWith("circuitbreaker")) {
            return "circuitbreaker";
        } else if (key.startsWith("route")) {
            return "route";
        } else if (key.startsWith("ratelimit")) {
            return "ratelimit";
        } else if (key.startsWith("lane-info")) {
            return "lane-info";
        } else if (key.startsWith("lane-rule")) {
            return "lane-rule";
        } else if (key.startsWith("affinity")) {
            return "affinity";
        } else {
            return "";
        }
    }

    public String convert(String key) {
        if (StringUtils.isEmpty(key)) {
            return "key is null!";
        }
        try {
            String[] split = key.split("/");
            String namespaceId = null;
            if (split.length > 1) {
                namespaceId = split[1];
            }
            String serviceName = null;
            if (key.startsWith("authority")) {
                serviceName = split[2];
                return convertAuthRule(key, namespaceId, serviceName);
            } else if (key.startsWith("circuitbreaker")) {
                serviceName = split[2];
                return convertCircuitBreakerRule(key, namespaceId, serviceName);
            } else if (key.startsWith("route")) {
                return convertRouteRule(key, namespaceId);
            } else if (key.startsWith("ratelimit")) {
                serviceName = split[2];
                return convertLimit(key, namespaceId, serviceName);
            } else if (key.startsWith("lane-info")) {
                return convertLaneInfo(key);
            } else if (key.startsWith("lane-rule")) {
                return convertLaneRule(key);
            }
        } catch (Exception e) {
            log.error("convert rule failed. convert key is {}", key);
        }
        return null;
    }


    public String convertAuthRule(String key, String namespaceId, String serviceName) {
        ArrayList<GetValue> res = new ArrayList<>();
        List<FemasAuthRule> authRules = dataOperation.fetchAuthRule(new ServiceModel(namespaceId, serviceName));
        if (authRules == null || authRules.size() == 0) {
            return "";
        }
        AuthRuleGroup authRuleGroup = new AuthRuleGroup();
        authRuleGroup.setRules(new ArrayList<AuthRule>());
        String type = "";
        for (FemasAuthRule authRule : authRules) {
            if ("0".equalsIgnoreCase(authRule.getIsEnabled())) {
                continue;
            }
            // 规则转换
            switch (authRule.getRuleType()) {
                case BLACK:
                    type = "B";
                    break;
                case WHITE:
                    type = "W";
                    break;
                default: {
                    type = "D";
                }
            }
            authRuleGroup.getRules().add(authRule.toPassRule());
        }
        if (CollectionUtil.isEmpty(authRuleGroup.getRules())) {
            return "";
        }
        GetValue resValue = new GetValue();
        resValue.setKey(key);
        authRuleGroup.setType(type);
        resValue.setValue(JSONSerializer.serializeStr(authRuleGroup));
        res.add(resValue);
        return JSONSerializer.serializeStr(res);
    }

    public String convertCircuitBreakerRule(String key, String namespaceId, String serviceName) {
        ArrayList<GetValue> res = new ArrayList<>();
        List<FemasCircuitBreakerRule> circuitBreakerRules = dataOperation
                .fetchBreakerRule(new CircuitBreakerModel(namespaceId, serviceName));
        if (CollectionUtil.isEmpty(circuitBreakerRules)) {
            return "";
        }
        circuitBreakerRules.stream().forEach(curRule -> {
            if ("1".equalsIgnoreCase(curRule.getIsEnable())) {
                GetValue resValue = new GetValue();
                resValue.setKey(curRule.getTargetServiceName());
                resValue.setValue(JSONSerializer.serializeStr(curRule.toPassRule()));
                res.add(resValue);
            }
        });
        if (CollectionUtil.isEmpty(res)) {
            return "";
        }
        return JSONSerializer.serializeStr(res);
    }

    public String convertRouteRule(String key, String namespaceId) {
        ArrayList<GetValue> res = new ArrayList<>();
        List<FemasRouteRule> routeRules = dataOperation.fetchRouteRuleByNamespaceId(namespaceId);
        if (routeRules == null || routeRules.size() == 0) {
            return "";
        }
        routeRules.stream().forEach(routeRule -> {
            if ("1".equalsIgnoreCase(routeRule.getStatus())) {
                GetValue resValue = new GetValue();
                resValue.setKey(routeRule.getServiceName());
                RouteRuleGroup routeRuleGroup = routeRule.toPassRule();
                Boolean tolerate = dataOperation
                        .fetchTolerant(new TolerateModel(routeRule.getNamespaceId(), routeRule.getServiceName()));
                routeRuleGroup.setFallbackStatus(tolerate);
                resValue.setValue(JSONSerializer.serializeStr(routeRuleGroup));
                res.add(resValue);
            }
        });
        if (CollectionUtil.isEmpty(res)) {
            return "";
        }
        return JSONSerializer.serializeStr(res);
    }

    public String convertLimit(String key, String namespaceId, String serviceName) {
        ArrayList<GetValue> res = new ArrayList<>();
        List<FemasLimitRule> limitRules = dataOperation.fetchLimitRule(new LimitModel(namespaceId, serviceName));
        if (limitRules == null || limitRules.size() == 0) {
            return "";
        }
        ArrayList<HashMap> rules = new ArrayList<>();
        for (FemasLimitRule limitRule : limitRules) {
            if (limitRule.getStatus() == 1) {
                HashMap<String, Object> rule = new HashMap<>();
                rule.put("conditionExpression", null);
                ArrayList<HashMap> conditions = new ArrayList<>();
                limitRule.getTags().stream().forEach(s1 -> {
                    HashMap<String, String> condition = new HashMap<>();
                    condition.put("tagField", s1.getTagField());
                    condition.put("tagOperator", s1.getTagOperator());
                    condition.put("tagType", s1.getTagType());
                    condition.put("tagValue", s1.getTagValue());
                    conditions.add(condition);
                });
                rule.put("conditions", conditions);
                rule.put("duration", limitRule.getDuration());
                rule.put("id", limitRule.getRuleId());
                rule.put("name", limitRule.getRuleName());
                rule.put("quota", limitRule.getTotalQuota());
                rules.add(rule);
            }
        }
        HashMap<String, Object> map = new HashMap<>();
        if (CollectionUtil.isEmpty(rules)) {
            return "";
        }
        map.put("rules", rules);
        GetValue getValue = new GetValue();
        getValue.setKey(key);
        getValue.setValue(JSONSerializer.serializeStr(map));
        res.add(getValue);
        return JSONSerializer.serializeStr(res);
    }

    private String convertLaneInfo(String key) {
        List<LaneInfo> laneInfos = dataOperation.fetchLaneInfo();
        if (CollectionUtil.isEmpty(laneInfos)) {
            return "";
        }
        List<GetValue> res = new ArrayList<>();
        laneInfos.stream().forEach(e -> {
            GetValue getValue = new GetValue();
            getValue.setKey(e.getLaneId());
            getValue.setValue(JSONSerializer.serializeStr(e));
            res.add(getValue);
        });
        return JSONSerializer.serializeStr(res);
    }

    private String convertLaneRule(String key) {
        List<LaneRule> laneRules = dataOperation.fetchLaneRule();
        if (CollectionUtil.isEmpty(laneRules)) {
            return "";
        }
        List<GetValue> res = new ArrayList<>();
        laneRules.stream().forEach(e -> {
            if (e.getEnable() == 1) {
                GetValue getValue = new GetValue();
                getValue.setKey(e.getRuleId());
                getValue.setValue(JSONSerializer.serializeStr(e));
                res.add(getValue);
            }
        });
        return JSONSerializer.serializeStr(res);
    }
}
