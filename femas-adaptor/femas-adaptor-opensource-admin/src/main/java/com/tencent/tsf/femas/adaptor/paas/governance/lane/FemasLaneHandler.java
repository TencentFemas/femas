package com.tencent.tsf.femas.adaptor.paas.governance.lane;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tsf.femas.adaptor.paas.config.FemasPaasConfigManager;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.tag.Tag;
import com.tencent.tsf.femas.common.tag.TagExpression;
import com.tencent.tsf.femas.common.tag.TagRule;
import com.tencent.tsf.femas.common.tag.constant.TagConstant;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.config.Config;
import com.tencent.tsf.femas.config.ConfigChangeListener;
import com.tencent.tsf.femas.config.enums.PropertyChangeType;
import com.tencent.tsf.femas.config.model.ConfigChangeEvent;
import com.tencent.tsf.femas.governance.lane.entity.LaneRule;
import com.tencent.tsf.femas.governance.lane.LaneService;
import com.tencent.tsf.femas.governance.plugin.config.ConfigHandler;
import com.tencent.tsf.femas.governance.plugin.config.enums.ConfigHandlerTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class FemasLaneHandler extends ConfigHandler {
    private static final Logger logger = LoggerFactory.getLogger(FemasLaneHandler.class);

    private static AtomicBoolean SUBSCRIBED_LANE_CONFIG = new AtomicBoolean(false);
    private static AtomicBoolean SUBSCRIBED_LANE_RULE_CONFIG = new AtomicBoolean(false);

    /**
     * @see com.tencent.tsf.femas.common.spi.SpiExtensionClass#getType()
     */
    @Override
    public String getType() {
        return ConfigHandlerTypeEnum.LANE.getType();
    }

    @Override
    public synchronized void subscribeServiceConfig(Service service) {
        subscribeLaneConfig();
        subscribeLaneRuleConfig();
    }

    public void subscribeLaneConfig() {
        if (SUBSCRIBED_LANE_CONFIG.get()) {
            return;
        }
        FemasLaneFilter femasLaneFilter = new FemasLaneFilter();
        LaneService.refreshLaneFilter(femasLaneFilter);
        String laneKey = "lane/info/";

        Config config = FemasPaasConfigManager.getConfig();
        // 初始化时，同步获取 lane info
//        List<ConfigChangeEvent<String>> laneInfoEvents = config.getDirectory(laneKey);
//        if (CollectionUtil.isNotEmpty(laneInfoEvents)) {
//            for (ConfigChangeEvent<String> configChangeEvent : laneInfoEvents) {
//                //新增逻辑
//                LaneInfo laneInfo = parseLaneInfo(configChangeEvent.getNewValue());
//                FemasLaneFilter.addLaneInfo(laneInfo);
//            }
//        }
//        FemasLaneFilter.updateLaneShortCutInfo();
//        logger.info("init group lane id:{}", LaneIdHolder.getCurrentGroupLaneId());

        config.subscribe(laneKey, new ConfigChangeListener<String>() {
            @Override
            public void onChange(List<ConfigChangeEvent<String>> configChangeEvents) {
                logger.info("[FEMAS TSF ADAPTOR LANE INFO] Starting process lane info change event. Changed event size : " + configChangeEvents.size());
                if (CollectionUtil.isEmpty(configChangeEvents)) {
                    return;
                }

                for (ConfigChangeEvent<String> configChangeEvent : configChangeEvents) {
                    try {
                        // 删除规则
                        if (configChangeEvent.getChangeType() == PropertyChangeType.DELETED) {
                            LaneInfo laneInfo = parseLaneInfo(configChangeEvent.getOldValue());
                            FemasLaneFilter.removeLaneInfo(laneInfo);

                            logger.info("[FEMAS TSF ADAPTOR LANE INFO] Remove Lane info. Lane info : " + laneInfo);
                        } else if (configChangeEvent.getChangeType() == PropertyChangeType.MODIFIED) {
                            // 修改
                            LaneInfo oldLaneInfo = parseLaneInfo(configChangeEvent.getOldValue());
                            FemasLaneFilter.removeLaneInfo(oldLaneInfo);

                            LaneInfo laneInfo = parseLaneInfo(configChangeEvent.getNewValue());
                            FemasLaneFilter.addLaneInfo(laneInfo);

                            logger.info("[FEMAS TSF ADAPTOR LANE INFO] Update Lane info. Lane info : " + laneInfo);
                        } else {
                            //新增逻辑
                            LaneInfo laneInfo = parseLaneInfo(configChangeEvent.getNewValue());
                            // 因为初始化时会设置一次，为了避免重复设置有问题，先尝试清除一次
                            FemasLaneFilter.removeLaneInfo(laneInfo);
                            FemasLaneFilter.addLaneInfo(laneInfo);

                            logger.info("[FEMAS TSF ADAPTOR LANE INFO] ADD Lane info. Lane info : " + laneInfo);
                        }
                        // 任何变化时，都更新当前部署组所在泳道信息
                        FemasLaneFilter.updateLaneShortCutInfo();
                    } catch (Exception ex) {
                        logger.error("[FEMAS TSF ADAPTOR LANE INFO] tsf Lane info load error.", ex);
                    }

                }
            }

            @Override
            public void onChange(ConfigChangeEvent<String> changeEvent) {
            }
        });

        SUBSCRIBED_LANE_CONFIG.set(true);
    }

    public void subscribeLaneRuleConfig() {
        if (SUBSCRIBED_LANE_RULE_CONFIG.get()) {
            return;
        }

        String laneKey = "lane/rule/";

        Config config = FemasPaasConfigManager.getConfig();

        // 初始化时，同步获取 lane rule
//        List<ConfigChangeEvent<String>> laneRuleEvents = config.getDirectory(laneKey);
//        if (CollectionUtil.isNotEmpty(laneRuleEvents)) {
//            for (ConfigChangeEvent<String> configChangeEvent : laneRuleEvents) {
//                //新增逻辑
//                LaneRule laneRule = parseLaneRule(configChangeEvent.getNewValue());
//                FemasLaneFilter.removeLaneRule(laneRule);
//
//                FemasLaneFilter.addLaneRule(laneRule);
//            }
//        }

        config.subscribe(laneKey, new ConfigChangeListener<String>() {
            @Override
            public void onChange(List<ConfigChangeEvent<String>> configChangeEvents) {
                logger.info("[FEMAS TSF ADAPTOR LANE RULE] Starting process lane rule change event. Changed event size : " + configChangeEvents.size());
                if (CollectionUtil.isEmpty(configChangeEvents)) {
                    return;
                }

                for (ConfigChangeEvent<String> configChangeEvent : configChangeEvents) {
                    try {
                        // 删除规则
                        if (configChangeEvent.getChangeType() == PropertyChangeType.DELETED) {
                            LaneRule laneRule = parseLaneRule(configChangeEvent.getOldValue());
                            FemasLaneFilter.removeLaneRule(laneRule);

                            logger.info("[FEMAS TSF ADAPTOR LANE RULE] Remove Lane Rule. Lane rule : " + laneRule);
                        } else {
                            // 修改或者新增逻辑
                            LaneRule laneRule = parseLaneRule(configChangeEvent.getNewValue());
                            FemasLaneFilter.removeLaneRule(laneRule);

                            FemasLaneFilter.addLaneRule(laneRule);

                            logger.info("[FEMAS TSF ADAPTOR LANE RULE] Update Lane Rule. Lane rule : " + laneRule);
                        }
                    } catch (Exception ex) {
                        logger.error("[FEMAS TSF ADAPTOR LANE] tsf Lane info load error.", ex);
                    }

                }
            }

            @Override
            public void onChange(ConfigChangeEvent<String> changeEvent) {
            }
        });

        SUBSCRIBED_LANE_RULE_CONFIG.set(true);
    }

    public static void unsubscribeLaneRuleConfig() {
        String laneKey = "lane/rule/";

        Config config = FemasPaasConfigManager.getConfig();

        config.unsubscribe(laneKey);
    }


    private static LaneInfo parseLaneInfo(String laneInfoString) {
        try {
            if (!StringUtils.isEmpty(laneInfoString)) {
                Yaml yaml = new Yaml();
                ObjectMapper mapper = new ObjectMapper();
                // 配置 ObjectMapper在反序列化时，忽略目标对象没有的属性
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                String laneInfoJsonString = mapper.writeValueAsString(yaml.load(laneInfoString));
                LaneInfo laneInfo = mapper.readValue(laneInfoJsonString, new TypeReference<LaneInfo>() {
                });
                return laneInfo;
            }

            throw new RuntimeException("Lane rule is null.");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static LaneRule parseLaneRule(String laneRuleString) {
        try {
            if (!StringUtils.isEmpty(laneRuleString)) {
                Yaml yaml = new Yaml();
                ObjectMapper mapper = new ObjectMapper();
                // 配置 ObjectMapper在反序列化时，忽略目标对象没有的属性
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                String laneInfoJsonString = mapper.writeValueAsString(yaml.load(laneRuleString));
                com.tencent.tsf.femas.adaptor.paas.governance.lane.LaneRule laneRule = mapper.readValue(laneInfoJsonString, new TypeReference<com.tencent.tsf.femas.adaptor.paas.governance.lane.LaneRule>() {
                });

                LaneRule femasLaneRule = new LaneRule();
                femasLaneRule.setCreateTime(laneRule.getCreateTime());
                femasLaneRule.setLaneId(laneRule.getLaneId());
                femasLaneRule.setPriority(laneRule.getPriority());
                femasLaneRule.setRuleId(laneRule.getRuleId());

                TagRule tagRule = new TagRule();
                String expression = laneRule.getRuleTagRelationship() == RuleTagRelationship.RELEATION_AND ? TagExpression.RELATION_AND : TagExpression.RELATION_OR;
                tagRule.setExpression(expression);

                List<Tag> tags = new ArrayList<>();
                for (LaneRuleTag laneRuleTag : laneRule.getRuleTagList()) {
                    Tag tag = new Tag();
                    tag.setTagValue(laneRuleTag.getTagValue());
                    tag.setTagOperator(laneRuleTag.getTagOperator());
                    tag.setTagField(laneRuleTag.getTagName());
                    tag.setTagType(TagConstant.TYPE.CUSTOM);

                    tags.add(tag);

                }
                tagRule.setTags(tags);

                femasLaneRule.setTagRule(tagRule);

                return femasLaneRule;
            }

            throw new RuntimeException("Lane rule is null.");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
