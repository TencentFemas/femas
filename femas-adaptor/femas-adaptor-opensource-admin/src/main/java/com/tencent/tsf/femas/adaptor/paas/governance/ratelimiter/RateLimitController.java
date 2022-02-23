package com.tencent.tsf.femas.adaptor.paas.governance.ratelimiter;


import com.tencent.tsf.femas.adaptor.paas.governance.ratelimiter.entity.LimitRule;
import com.tencent.tsf.femas.common.tag.TagRule;
import com.tencent.tsf.femas.common.tag.engine.TagEngine;
import com.tencent.tsf.femas.common.tag.exception.TagEngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RateLimitController {

    // 本来想要这个类不去关心 Rule，但是不同 Rule 间带有关联（因为有一个 Rule 是全局的），导致我还是需要去关心 Rule 结构，
    // 并且 applyRule(oldRule, newRule) 这种代码无法实现，因为可能你 apply 的 newRule 是个全局的，把旧的全局覆盖掉等
    // bla bla 问题
    private static final Logger LOG = LoggerFactory.getLogger(RateLimitController.class);

    private volatile Map<String, FemasTokenBucket> serviceBucketMap = new ConcurrentHashMap<>();
    private volatile Map<String, TagRule> tagResolverMap = new ConcurrentHashMap<>();

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    {
        scheduledExecutorService.scheduleWithFixedDelay(this::report, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    // 新的 Rule 出现（oldRule 为空），或者 Rule 的 instanceCount / 周期变化时调用它
    //
    // 正常过程：
    // 1. 程序启动，拿到若干条 Rule，调用此函数，此时 instance quota 为 null
    // 2. 开始上报（RequestCollector.report()），拿到各规则的 instance quota，调用此函数
    // 3. 正常的变化流程
    //
    // 可能的异常：
    // 1. 上报一直失败，拿不到 instance quota，而此过程用户可能在控制台修改了规则周期，此时 instance quota 一直为 null
    // 2. 上报一开始成功，后面有失败，不会走到这里来
    public synchronized void applyRule(LimitRule oldLimitRule, LimitRule newLimitRule) {
        if (newLimitRule.getInstanceQuota() == null) {
            // 还没拿 instance quota，先不起令牌桶
            // instanceQuota 一旦不为 null 就不会变回 null
            return;
        }

        if (oldLimitRule == null) {
            // 相信调用方是正确调用的，这里不去处理 serviceBucketMap 中已经有这个 rule id 的情况
            try {
                tagResolverMap.put(newLimitRule.getRuleId(), newLimitRule.getTagRule());
            } catch (TagEngineException e) {
                LOG.warn("new tag condition invalid: {}", e.getMessage());
                return;
            }

            FemasTokenBucket newBucket = new FemasTokenBucket(newLimitRule.getInstanceQuota(),
                    newLimitRule.getDuration(),
                    TimeUnit.SECONDS, newLimitRule.getRuleId());
            serviceBucketMap.put(newLimitRule.getRuleId(), newBucket);
        } else {
            FemasTokenBucket bucket = serviceBucketMap.get(newLimitRule.getRuleId());
            if (!oldLimitRule.isSameDuration(newLimitRule) || bucket == null || !oldLimitRule
                    .isSameTagRule(newLimitRule)) {
                try {
                    tagResolverMap.put(newLimitRule.getRuleId(), newLimitRule.getTagRule());
                } catch (TagEngineException ex) {
                    LOG.warn("new tag condition invalid: {}", ex.getMessage());
                    return;
                }

                // 1. 周期不同，当作新的 bucket 起
                // 2. bucket 未初始化
                FemasTokenBucket newBucket = new FemasTokenBucket(newLimitRule.getInstanceQuota(),
                        newLimitRule.getDuration(),
                        TimeUnit.SECONDS, newLimitRule.getRuleId());
                serviceBucketMap.put(newLimitRule.getRuleId(), newBucket);
            } else {
                // 周期相同，做升额降额操作
                bucket.setNewCapacity(newLimitRule.getInstanceQuota());
            }
        }
        LOG.debug("[FEMAS Ratelimit] Service bucket snapshot: {}", serviceBucketMap);
    }

    // Rule 被去除调用
    public synchronized void removeRule(LimitRule limitRule) {
        serviceBucketMap.remove(limitRule.getRuleId());
        tagResolverMap.remove(limitRule.getRuleId());
    }

    public synchronized void clearRules() {
        serviceBucketMap.clear();
        tagResolverMap.clear();
    }

    /**
     * @param passRule result value, store pass or block rule id list
     * @return passed or not
     */
    public synchronized Result tryConsume(List<String> passRule) {
        Result res = Result.PASS;
        for (Map.Entry<String, TagRule> entry : tagResolverMap.entrySet()) {
            if (TagEngine.checkRuleHitByUpstreamTags(entry.getValue())) {
                LOG.debug("match ratelimit rule {}", entry.getKey());
                FemasTokenBucket serviceBucket = serviceBucketMap.get(entry.getKey());
                if (serviceBucket != null) {
                    if (!serviceBucket.consumeToken()) { // leak token
                        LOG.debug("block by ratelimit rule {}", entry.getKey());
                        for (String id : passRule) { // put block rule id in passRule
                            FemasTokenBucket t = serviceBucketMap.get(id);
                            if (t != null) {
                                t.returnToken();
                            }
                        }
                        passRule.clear();
                        passRule.add(entry.getKey());
                        res = Result.BLOCKED;
                        break;
                    } else {
                        LOG.trace("ratelimit rule {} passing", entry.getKey());
                        passRule.add(entry.getKey());
                    }
                } else {
                    LOG.error("ratelimit rule {} has not token bucket", entry.getKey());
                }
            } else {
                LOG.trace("ratelimit rule {} ignore", entry.getKey());
            }
        }
        return res;
    }

    public void report() {
        for (Map.Entry<String, FemasTokenBucket> entry : serviceBucketMap.entrySet()) {
            FemasTokenBucket bucket = entry.getValue();
            bucket.refillAndSyncPeriod();
        }
    }

    public enum Result {
        PASS, BLOCKED
    }
}
