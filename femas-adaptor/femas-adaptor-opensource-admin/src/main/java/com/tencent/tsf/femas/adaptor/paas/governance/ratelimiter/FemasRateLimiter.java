package com.tencent.tsf.femas.adaptor.paas.governance.ratelimiter;

import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.governance.config.impl.RateLimitConfigImpl;
import com.tencent.tsf.femas.governance.event.LimitEventCollector;
import com.tencent.tsf.femas.governance.plugin.context.ConfigContext;
import com.tencent.tsf.femas.governance.ratelimit.RateLimiter;
import com.tencent.tsf.femas.governance.ratelimit.RateLimiterManager;
import com.tencent.tsf.femas.governance.ratelimit.entity.InitLimitRule;
import com.tencent.tsf.femas.governance.ratelimit.entity.RateLimitRuleConfig;
import com.tencent.tsf.femas.governance.ratelimit.entity.RateLimiterRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FemasRateLimiter implements RateLimiter<RateLimiterRule> {

    private static final  Logger LOGGER = LoggerFactory.getLogger(RateLimiter.class);

    RateLimitController rateLimitController = new RateLimitController();
    RateLimitClientCache ratelimitClientCache = new RateLimitClientCache(rateLimitController);
    RequestCollector requestCollector;
    private Service service;

    public void buildCollector(Service service) {
        this.service = service;
        this.requestCollector = new RequestCollector(service, ratelimitClientCache);
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public RequestCollector getRequestCollector() {
        return requestCollector;
    }

    public void setRequestCollector(RequestCollector requestCollector) {
        this.requestCollector = requestCollector;
    }

    @Override
    public boolean acquire() {
        List<String> passList = new ArrayList<>();
        RateLimitController.Result result = rateLimitController.tryConsume(passList);
        for (String id : passList) {
            if (result == RateLimitController.Result.PASS) {
                requestCollector.incrPassCount(id);
            } else {
                requestCollector.incrBlockCount(id);
            }
        }
        if (result == RateLimitController.Result.PASS) {
            return true;
        } else {
            LimitEventCollector.addLimitEvent(Context.getRpcInfo().getAll());
            return false;
        }
    }

    @Override
    public boolean acquire(int permits) {
        throw new UnsupportedOperationException("Femas Rate Limiter do not support this method.");
    }

    @Override
    public void update(RateLimiterRule rule) {
    }

    public void reloadConfig(Map config) {
        ratelimitClientCache.reloadConfig(config);
    }

    public void resetConfig() {
        ratelimitClientCache.resetConfig();
    }

    /**
     * 适配单进程多Service的场景
     */
    public void setInsId(String insId) {
        requestCollector.setInsId(insId);
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public void init(ConfigContext conf) throws FemasRuntimeException {
        RateLimitConfigImpl rateLimitRule = (RateLimitConfigImpl) conf.getConfig().getRateLimit();
        if (rateLimitRule == null || CollectionUtil.isEmpty(rateLimitRule.getLimitRule())) {
            return;
        }
        String namespaceId = System.getProperty("femas_namespace_id");
        try {
            for (RateLimitRuleConfig rateLimitRuleConfig : rateLimitRule.getLimitRule()) {
                Service service = new Service();
                service.setNamespace(namespaceId);
                service.setName(rateLimitRuleConfig.getServiceName());
                List<InitLimitRule> limitRules = rateLimitRuleConfig.getLimitRuleGroup();
                ArrayList<Map> ruleMap = new ArrayList<>();
                limitRules.stream().forEach(initLimitRule -> {
                    ruleMap.add(initLimitRule.toMapRule());
                });
                HashMap<String, Object> sdkParam = new HashMap<>();
                sdkParam.put("rules", ruleMap);
                FemasRateLimiter femasRateLimiter = new FemasRateLimiter();
                femasRateLimiter.reloadConfig(sdkParam);
                femasRateLimiter.buildCollector(service);
                RateLimiterManager.refreshRateLimiter(service, femasRateLimiter);
            }
        } catch (Exception e) {
            throw new FemasRuntimeException("rate limit init refresh error");
        }
        LOGGER.info("init rate limit rule: {}", rateLimitRule.getLimitRule());
    }

    @Override
    public String getName() {
        return "femasRateLimit";
    }

    @Override
    public void destroy() {

    }
}
