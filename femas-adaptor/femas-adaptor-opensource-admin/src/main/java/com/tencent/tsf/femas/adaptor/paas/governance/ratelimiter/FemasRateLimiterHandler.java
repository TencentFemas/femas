package com.tencent.tsf.femas.adaptor.paas.governance.ratelimiter;


import com.tencent.tsf.femas.adaptor.paas.config.FemasPaasConfigManager;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.serialize.JSONSerializer;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.config.ConfigChangeListener;
import com.tencent.tsf.femas.config.enums.PropertyChangeType;
import com.tencent.tsf.femas.config.impl.paas.PaasConfig;
import com.tencent.tsf.femas.config.model.ConfigChangeEvent;
import com.tencent.tsf.femas.plugin.config.ConfigHandler;
import com.tencent.tsf.femas.plugin.config.enums.ConfigHandlerTypeEnum;
import com.tencent.tsf.femas.plugin.impl.FemasPluginContext;
import com.tencent.tsf.femas.governance.ratelimit.RateLimiter;
import com.tencent.tsf.femas.governance.ratelimit.RateLimiterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class FemasRateLimiterHandler extends ConfigHandler {

    private static final Logger logger = LoggerFactory.getLogger(FemasRateLimiterHandler.class);

    /**
     * @see com.tencent.tsf.femas.common.spi.SpiExtensionClass#getType()
     */
    @Override
    public String getType() {
        return ConfigHandlerTypeEnum.RATE_LIMITER.getType();
    }

    /**
     * 指定某个service
     *
     * @param service
     */
    public void subscribeServiceConfig(final Service service) {
        RateLimiter rateLimiter = FemasPluginContext.getRateLimiter();
        // 由于rateLimit是单例的故放入map时需要复制
        if (RateLimiterManager.getRateLimiter(service) == null) {
            Class<? extends RateLimiter> aClass = rateLimiter.getClass();
            try {
                RateLimiter limiter = aClass.newInstance();
                limiter.buildCollector(service);
                RateLimiterManager.refreshRateLimiter(service, limiter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String rateLimitKey = "ratelimit/" + service.getNamespace() + "/" + service.getName() + "/";
        PaasConfig config = FemasPaasConfigManager.getConfig();

        config.subscribe(rateLimitKey, new ConfigChangeListener<String>() {
            @Override
            public void onChange(List<ConfigChangeEvent<String>> configChangeEvents) {
                logger.info(
                        "[Femas ADAPTOR RATE LIMITER] Starting process RATE LIMITER rule change event. Changed event size : "
                                + configChangeEvents.size());
                if (CollectionUtil.isEmpty(configChangeEvents)) {
                    return;
                }

                /**
                 * 目前一个进程只支持暴露一个Provider服务，所以这里监听一个service没数据就直接reset all了
                 */
                for (ConfigChangeEvent<String> configChangeEvent : configChangeEvents) {
                    try {
                        // 删除规则
                        if (configChangeEvent.getChangeType() == PropertyChangeType.DELETED) {
                            ((FemasRateLimiter) RateLimiterManager.getRateLimiter(service)).resetConfig();
                            logger.info("[Femas ADAPTOR RATE LIMITER] Reset rate limiter. Service " + service);
                        } else {
                            Map config = JSONSerializer.deserializeStr(Map.class, configChangeEvent.getNewValue());
                            ((FemasRateLimiter) RateLimiterManager.getRateLimiter(service)).reloadConfig(config);
                            logger.info("[Femas ADAPTOR RATE LIMITER] Refresh rate limiter. Service = " + service
                                    + ", new Map:" + config);
                        }

                    } catch (Exception ex) {
                        logger.error("[Femas ADAPTOR RATE LIMITER] tsf route rule load error.", ex);
                    }

                }
            }

            @Override
            public void onChange(ConfigChangeEvent<String> changeEvent) {
            }
        });
    }

}
