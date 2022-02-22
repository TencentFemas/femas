package com.tencent.tsf.femas.adaptor.paas.governance.ratelimiter;


import com.tencent.tsf.femas.adaptor.paas.governance.ratelimiter.entity.LimitRule;
import com.tencent.tsf.femas.common.tag.Tag;
import com.tencent.tsf.femas.common.tag.TagExpression;
import com.tencent.tsf.femas.common.tag.TagRule;
import com.tencent.tsf.femas.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class RateLimitClientCache {

    // WARNING: 这个代码实现假定了同一个 Rule id，它的 type 和 source 并不会发生改变，
    // 同时假定了 Consul 的数据是合法的，不会出现同样 source 的多条规则。
    private static final Logger LOG = LoggerFactory.getLogger(RateLimitClientCache.class);

    private static final String RATE_LIMIT_CONFIG_VERSION = "0.0.2";

    private volatile Map<String, LimitRule> ruleMap = new HashMap<>();
    private Map<String, Integer> ruleInstanceQuota = new HashMap<>();
    private RateLimitController rateLimitController;

    public RateLimitClientCache(RateLimitController rateLimitController) {
        this.rateLimitController = rateLimitController;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    synchronized void reloadConfig(Map config) {
        Set<String> ruleIds = new HashSet<>();

        try {
            String version = (String) config.get("version");
            if (version != null && !version.equals(RATE_LIMIT_CONFIG_VERSION)) {
                LOG.error("ratelimit config version {0} no compatiable, wanna {1}", version, RATE_LIMIT_CONFIG_VERSION);
                return;
            }
            List<Map<String, Object>> rulesConfig = (List<Map<String, Object>>) config.get("rules");
            for (Map<String, Object> ruleConfig : rulesConfig) {
                String id = (String) ruleConfig.get("id");
                int duration = (int) ruleConfig.get("duration");
                int totalQuota = (int) ruleConfig.get("quota");
                int instanceQuota = (int) ruleConfig.get("quota");
//                Integer instanceQuota = ruleInstanceQuota.get(id);

                LimitRule.Type type;
                List<Map<String, String>> tag = (List<Map<String, String>>) ruleConfig.get("conditions");
                TagRule tagRule = null;
                if (tag != null) {
                    tagRule = new TagRule();
                    type = LimitRule.Type.TAG_CONDITION;
                    List<Tag> list = new ArrayList<>();
                    for (Map<String, String> cond : tag) {
                        Tag tagCondition = new Tag();
                        String tagFiled = cond.get("tagField");

                        if (tagFiled.startsWith("destination.")) {
                            tagFiled = tagFiled.substring(12);
                        }
                        tagCondition.setTagField(tagFiled);
                        tagCondition.setTagType(cond.get("tagType"));
                        tagCondition.setTagOperator(cond.get("tagOperator"));
                        tagCondition.setTagValue(cond.get("tagValue"));
                        list.add(tagCondition);
                        LOG.debug("ratelimit add a tag condition {}", tagCondition);
                    }
                    tagRule.setTags(list);
                    String expression = (String) ruleConfig.get("conditionExpression");
                    expression = StringUtils.isEmpty(expression) ? TagExpression.RELATION_AND : expression;
                    tagRule.setExpression(expression);
                } else {
                    LOG.debug("ratelimit none tag condition");
                    type = LimitRule.Type.GLOBAL;
                }

                LimitRule newLimitRule = new LimitRule(id, duration, totalQuota, instanceQuota, type, tagRule);

                LimitRule oldLimitRule = ruleMap.get(newLimitRule.getRuleId());
                if (oldLimitRule != null) {
                    if (!oldLimitRule.equals(newLimitRule)) {
                        LOG.info("[FEMAS Ratelimit] Rule {} is changed, new config applied", newLimitRule.getRuleId());
                        ruleMap.put(newLimitRule.getRuleId(), newLimitRule);
                        rateLimitController.applyRule(oldLimitRule, newLimitRule);
                    } else {
                        LOG.debug("[FEMAS Ratelimit] Rule {} is not changed", newLimitRule.getRuleId());
                    }
                } else {
                    LOG.info("[FEMAS Ratelimit] Get new rule {}", newLimitRule);
                    ruleMap.put(newLimitRule.getRuleId(), newLimitRule);
                    rateLimitController.applyRule(null, newLimitRule);
                }

                ruleIds.add(newLimitRule.getRuleId());
            }

            // 干掉被删掉的规则
            for (Map.Entry<String, LimitRule> ruleEntry : ruleMap.entrySet()) {
                if (!ruleIds.contains(ruleEntry.getKey())) {
                    rateLimitController.removeRule(ruleEntry.getValue());
                }
            }
            ruleMap.keySet().retainAll(ruleIds);
            ruleInstanceQuota.keySet().retainAll(ruleIds);

            LOG.info("[FEMAS Ratelimit] Rule snapshot: {}", ruleMap.values());
        } catch (Exception e) {
            LOG.error("[FEMAS Ratelimit] Parse consul config failed: {}", e);
        }
    }

    synchronized void resetConfig() {
        ruleMap = new HashMap<>();
        ruleInstanceQuota = new HashMap<>();

        rateLimitController.clearRules();
        LOG.info("Reset all rules.");
    }

    /**
     * 根据中控的返回，动态调整规则
     *
     * @param ruleQuotaMap
     */
    synchronized void applyQuota(Map<String, Integer> ruleQuotaMap) {
        for (Map.Entry<String, LimitRule> entry : ruleMap.entrySet()) {
            String ruleId = entry.getKey();
            Integer quota = ruleQuotaMap.get(ruleId);
            if (quota != null && !quota.equals(entry.getValue().getInstanceQuota())) {
                LimitRule oldLimitRule = new LimitRule(entry.getValue());
                entry.getValue().setInstanceQuota(quota);

                rateLimitController.applyRule(oldLimitRule, entry.getValue());
            }
        }
        ruleInstanceQuota = new HashMap<>(ruleQuotaMap);
        LOG.debug("[FEMAS Ratelimit] Rule instance quota map snapshot: {}", ruleInstanceQuota);
    }

    Collection<LimitRule> getRules() {
        return Collections.unmodifiableCollection(ruleMap.values());
    }
}
