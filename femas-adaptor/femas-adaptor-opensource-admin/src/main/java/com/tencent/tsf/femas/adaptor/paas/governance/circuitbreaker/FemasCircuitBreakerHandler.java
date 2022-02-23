package com.tencent.tsf.femas.adaptor.paas.governance.circuitbreaker;

import com.tencent.tsf.femas.adaptor.paas.config.FemasPaasConfigManager;
import com.tencent.tsf.femas.adaptor.paas.governance.circuitbreaker.event.CircuitBreakerEventCollector;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.serialize.JSONSerializer;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.config.Config;
import com.tencent.tsf.femas.config.ConfigChangeListener;
import com.tencent.tsf.femas.config.enums.PropertyChangeType;
import com.tencent.tsf.femas.config.model.ConfigChangeEvent;
import com.tencent.tsf.femas.governance.circuitbreaker.FemasCircuitBreakerIsolationLevelEnum;
import com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService;
import com.tencent.tsf.femas.governance.circuitbreaker.core.CircuitBreaker;
import com.tencent.tsf.femas.governance.circuitbreaker.core.CircuitBreakerFactory;
import com.tencent.tsf.femas.governance.circuitbreaker.core.StateTransitionCallback;
import com.tencent.tsf.femas.governance.circuitbreaker.core.internal.CircuitBreakerMetrics;
import com.tencent.tsf.femas.governance.config.FemasPluginContext;
import com.tencent.tsf.femas.governance.plugin.config.ConfigHandler;
import com.tencent.tsf.femas.governance.plugin.config.enums.ConfigHandlerTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class FemasCircuitBreakerHandler extends ConfigHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FemasCircuitBreakerHandler.class);

    private static ICircuitBreakerService circuitBreakerService = FemasPluginContext.getCircuitBreakers().get(0);

    /**
     * 熔断事件上报
     */
    static {
        CircuitBreakerFactory.registerCallback(new StateTransitionCallback() {
            @Override
            public void onTransition(ICircuitBreakerService.State from, ICircuitBreakerService.State to,
                    Object circuitBreakerObject, CircuitBreakerMetrics metrics, CircuitBreaker circuitBreaker) {
                com.tencent.tsf.femas.governance.circuitbreaker.rule.CircuitBreakerRule rule = circuitBreaker
                        .getCircuitBreakerConfig().getCircuitBreakerRule();

                String circuitBreakerObjectString = circuitBreaker.getName();
                if (rule.getIsolationLevel() == FemasCircuitBreakerIsolationLevelEnum.SERVICE) {
                    circuitBreakerObjectString = rule.getTargetService().getName();
                }

                if (circuitBreakerObject != null) {
                    circuitBreakerObjectString = circuitBreakerObject.toString();
                }

                float fail = metrics.getFailureRate();
                float slow = metrics.getSlowCallRate();
                CircuitBreakerEventCollector
                        .addCircuitBreakerEvent(System.currentTimeMillis(), from, to, rule.getTargetService().getName(),
                                rule.getTargetService().getNamespace(), circuitBreakerObjectString,
                                String.valueOf(formatRate(fail)), String.valueOf(formatRate(slow)));
            }
        });

        LOGGER.info("[Femas  ADAPTOR CIRCUIT BREAKER] Registered event processing callback.");
    }

    private static float formatRate(float rate) {
        if (rate < 0) {
            return 0;
        }

        return rate;
    }

    private static com.tencent.tsf.femas.governance.circuitbreaker.rule.CircuitBreakerRule convertRule(
            CircuitBreakerRule tsfRule) {
        com.tencent.tsf.femas.governance.circuitbreaker.rule.CircuitBreakerRule femasRule = new com.tencent.tsf.femas.governance.circuitbreaker.rule.CircuitBreakerRule();

        Service service = new Service(tsfRule.getTargetNamespaceId(), tsfRule.getTargetServiceName());
        femasRule.setTargetService(service);
        femasRule.setIsolationLevel(tsfRule.getIsolationLevel());
        femasRule.setStrategyList(convertStrategies(tsfRule.getStrategyList()));

        return femasRule;
    }

    private static List<com.tencent.tsf.femas.governance.circuitbreaker.rule.CircuitBreakerStrategy> convertStrategies(
            List<CircuitBreakerStrategy> strategies) {
        List<com.tencent.tsf.femas.governance.circuitbreaker.rule.CircuitBreakerStrategy> femasStrategies = new ArrayList<>();

        if (strategies != null) {
            for (CircuitBreakerStrategy tsfStrategy : strategies) {
                com.tencent.tsf.femas.governance.circuitbreaker.rule.CircuitBreakerStrategy femasStrategy = new com.tencent.tsf.femas.governance.circuitbreaker.rule.CircuitBreakerStrategy();

                femasStrategy.setFailureRateThreshold(tsfStrategy.getFailureRateThreshold());
                femasStrategy.setMinimumNumberOfCalls(tsfStrategy.getMinimumNumberOfCalls());
                femasStrategy.setSlidingWindowSize(tsfStrategy.getSlidingWindowSize());
                femasStrategy.setSlowCallDurationThreshold(tsfStrategy.getSlowCallDurationThreshold());
                femasStrategy.setWaitDurationInOpenState(tsfStrategy.getWaitDurationInOpenState());
                femasStrategy.setMaxEjectionPercent(tsfStrategy.getMaxEjectionPercent());
                femasStrategy.setSlowCallRateThreshold(tsfStrategy.getSlowCallRateThreshold());
                femasStrategy.setApiList(convertApis(tsfStrategy.getApiList()));

                femasStrategies.add(femasStrategy);
            }
        }

        return femasStrategies;
    }

    private static List<com.tencent.tsf.femas.governance.circuitbreaker.rule.CircuitBreakerApi> convertApis(
            List<CircuitBreakerApi> apis) {
        List<com.tencent.tsf.femas.governance.circuitbreaker.rule.CircuitBreakerApi> femasApis = new ArrayList<>();

        if (apis != null) {
            for (CircuitBreakerApi api : apis) {
                com.tencent.tsf.femas.governance.circuitbreaker.rule.CircuitBreakerApi femasApi = new com.tencent.tsf.femas.governance.circuitbreaker.rule.CircuitBreakerApi();

                femasApi.setMethod(api.getMethod() + "/" + api.getPath());

                femasApis.add(femasApi);
            }
        }

        return femasApis;
    }

    private static CircuitBreakerRule parseCircuitBreakerRule(String circuitBreakerRuleString) {
        try {
            if (!StringUtils.isEmpty(circuitBreakerRuleString)) {
                return JSONSerializer.deserializeStr(CircuitBreakerRule.class, circuitBreakerRuleString);
            }
            throw new RuntimeException("CircuitBreakerRuleString rule is null.");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see com.tencent.tsf.femas.common.spi.SpiExtensionClass#getType()
     */
    @Override
    public String getType() {
        return ConfigHandlerTypeEnum.CIRCUIT_BREAKER.getType();
    }

    /**
     * 指定某个service
     *
     * @param service
     */
    public synchronized void subscribeServiceConfig(Service service) {
        String cbKey = "circuitbreaker/" + service.getNamespace() + "/" + service.getName() + "/";
        Config config = FemasPaasConfigManager.getConfig();
        config.subscribe(cbKey, new ConfigChangeListener<String>() {
            @Override
            public void onChange(List<ConfigChangeEvent<String>> configChangeEvents) {
                LOGGER.info(
                        "[Femas  ADAPTOR CIRCUIT BREAKER] Starting process circuit-breaker change event. Changed event size : "
                                + configChangeEvents.size());
                if (CollectionUtil.isEmpty(configChangeEvents)) {
                    return;
                }
                for (ConfigChangeEvent<String> configChangeEvent : configChangeEvents) {
                    try {
                        CircuitBreakerRule circuitBreakerRule = null;
                        // 删除规则
                        if (configChangeEvent.getChangeType() == PropertyChangeType.DELETED) {
                            circuitBreakerRule = parseCircuitBreakerRule(configChangeEvent.getOldValue());
                            circuitBreakerService
                                    .disableCircuitBreaker(convertRule(circuitBreakerRule).getTargetService());
                        } else {
                            circuitBreakerRule = parseCircuitBreakerRule(configChangeEvent.getNewValue());
                            circuitBreakerService.updateCircuitBreakerRule(convertRule(circuitBreakerRule));
                        }

                        LOGGER.info(
                                "[Femas  ADAPTOR CIRCUIT BREAKER] Update circuit-breaker rule group. CircuitBreakerRule = "
                                        + circuitBreakerRule);
                    } catch (Exception ex) {
                        LOGGER.error("[Femas  ADAPTOR CIRCUIT BREAKER] circuit-breaker load error.", ex);
                    }

                }
            }

            @Override
            public void onChange(ConfigChangeEvent<String> changeEvent) {

            }
        });
    }
}
