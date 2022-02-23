package com.tencent.tsf.femas.adaptor.paas.governance.router;

import com.tencent.tsf.femas.adaptor.paas.common.FemasConstant;
import com.tencent.tsf.femas.adaptor.paas.config.FemasPaasConfigManager;
import com.tencent.tsf.femas.common.context.FemasContext;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.serialize.JSONSerializer;
import com.tencent.tsf.femas.common.tag.Tag;
import com.tencent.tsf.femas.common.tag.TagRule;
import com.tencent.tsf.femas.common.tag.constant.TagConstant;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.config.Config;
import com.tencent.tsf.femas.config.ConfigChangeListener;
import com.tencent.tsf.femas.config.enums.PropertyChangeType;
import com.tencent.tsf.femas.config.model.ConfigChangeEvent;
import com.tencent.tsf.femas.governance.plugin.config.ConfigHandler;
import com.tencent.tsf.femas.governance.plugin.config.enums.ConfigHandlerTypeEnum;
import com.tencent.tsf.femas.governance.route.RouterRuleManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FemasRouterHandler extends ConfigHandler {

    private static final Logger logger = LoggerFactory.getLogger(FemasRouterHandler.class);

    private static Set<String> SUBSCRIBED_ROUTER_CONFIG = new HashSet<>();

    protected static void subscribeRouteConfig(String routeKey) {
        if (SUBSCRIBED_ROUTER_CONFIG.contains(routeKey)) {
            return;
        }

        Config<String> config = FemasPaasConfigManager.getConfig();

        config.subscribe(routeKey, new ConfigChangeListener<String>() {
            @Override
            public void onChange(List<ConfigChangeEvent<String>> configChangeEvents) {
                logger.info("[Femas ADAPTOR ROUTER] Starting process router rule change event. Changed event size : "
                        + configChangeEvents.size());
                if (CollectionUtil.isEmpty(configChangeEvents)) {
                    return;
                }

                for (ConfigChangeEvent<String> configChangeEvent : configChangeEvents) {
                    try {
                        // 删除规则
                        if (configChangeEvent.getChangeType() == PropertyChangeType.DELETED) {
                            RouteRuleGroup routeRuleGroup = parseRouteRuleGroup(configChangeEvent.getOldValue());
                            Service service = new Service(routeRuleGroup.getNamespaceId(),
                                    routeRuleGroup.getMicroserviceName());
                            RouterRuleManager.removeRouteRule(service);

                            logger.info("[Femas ADAPTOR ROUTER] Remove route rule group. Service " + service);
                        } else {
                            // 修改或者新增逻辑
                            RouteRuleGroup routeRuleGroup = parseRouteRuleGroup(configChangeEvent.getNewValue());
                            Service service = new Service(routeRuleGroup.getNamespaceId(),
                                    routeRuleGroup.getMicroserviceName());
                            com.tencent.tsf.femas.governance.route.entity.RouteRuleGroup femasRouteRuleGroup = transferFemasRouterRuleToFemas(
                                    routeRuleGroup);
                            RouterRuleManager.refreshRouteRule(service, femasRouteRuleGroup);

                            logger.info("[Femas ADAPTOR ROUTER] Update route rule group. Service = " + service
                                    + " RouteRuleGroup = " + femasRouteRuleGroup);
                        }
                    } catch (Throwable ex) {
                        logger.error("[Femas ADAPTOR ROUTER] tsf route rule load error.", ex);
                    }

                }
            }

            @Override
            public void onChange(ConfigChangeEvent<String> changeEvent) {
            }
        });

        SUBSCRIBED_ROUTER_CONFIG.add(routeKey);
    }

    private static RouteRuleGroup parseRouteRuleGroup(String routerRule) {
        try {
            if (!StringUtils.isEmpty(routerRule)) {
                return JSONSerializer.deserializeStr(RouteRuleGroup.class, routerRule);
            }
            throw new RuntimeException("Router rule is null.");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static com.tencent.tsf.femas.governance.route.entity.RouteRuleGroup transferFemasRouterRuleToFemas(
            RouteRuleGroup routeRuleGroup) {
        com.tencent.tsf.femas.governance.route.entity.RouteRuleGroup femasRouteRuleGroup = new com.tencent.tsf.femas.governance.route.entity.RouteRuleGroup();

        femasRouteRuleGroup.setServiceName(routeRuleGroup.getMicroserviceName());
        femasRouteRuleGroup.setNamespace(routeRuleGroup.getNamespaceId());
        femasRouteRuleGroup.setFallback(routeRuleGroup.getFallbackStatus());

        List<com.tencent.tsf.femas.governance.route.entity.RouteRule> femasRouteRules = new ArrayList<>();
        for (RouteRule routeRule : routeRuleGroup.getRuleList()) {
            com.tencent.tsf.femas.governance.route.entity.RouteRule femasRouteRule = new com.tencent.tsf.femas.governance.route.entity.RouteRule();
            femasRouteRule.setTagRule(transferFemasTagToFemasTag(routeRule.getTagList()));
            femasRouteRule.setDestList(transferFemasRouteDestToFemas(routeRule));
            femasRouteRules.add(femasRouteRule);
        }
        femasRouteRuleGroup.setRuleList(femasRouteRules);

        return femasRouteRuleGroup;
    }

    private static TagRule transferFemasTagToFemasTag(List<RouteTag> routeTags) {
        TagRule tagRule = new TagRule();
        List<Tag> tags = new ArrayList<>();
        tagRule.setTags(tags);

        if (CollectionUtil.isEmpty(routeTags)) {
            return tagRule;
        }

        for (RouteTag routeTag : routeTags) {
            Tag tag = new Tag();
            tag.setTagField(transferFemasSystemTagFieldToFemasSystemTagField(routeTag.getTagField()));
            tag.setTagOperator(routeTag.getTagOperator());
            tag.setTagType(routeTag.getTagType());
            tag.setTagValue(routeTag.getTagValue());

            tags.add(tag);
        }

        return tagRule;
    }

    private static List<com.tencent.tsf.femas.governance.route.entity.RouteDest> transferFemasRouteDestToFemas(
            RouteRule routeRule) {
        List<com.tencent.tsf.femas.governance.route.entity.RouteDest> femasRouteDestList = new ArrayList<>();

        if (!CollectionUtil.isEmpty(routeRule.getDestList())) {
            for (RouteDest routeDest : routeRule.getDestList()) {
                com.tencent.tsf.femas.governance.route.entity.RouteDest femasRouteDest = new com.tencent.tsf.femas.governance.route.entity.RouteDest();
                femasRouteDest.setDestWeight(routeDest.getDestWeight());

                TagRule femasTagRule = new TagRule();
                List<Tag> tagList = new ArrayList<>();
                for (RouteDestItem routeDestItem : routeDest.getDestItemList()) {
                    Tag tag = new Tag();

                    if (StringUtils.equals(routeDestItem.getDestItemValue(), ROUTE_DEST_SYSTEM_VALUE.ALL)) {
                        continue;
                    } else if (StringUtils.equals(routeDestItem.getDestItemValue(), ROUTE_DEST_SYSTEM_VALUE.ELSE)) {
                        // 路由目标到其他， 其他过滤条件设置
                        tag.setTagField(transferFemasDestTagFieldToFemas(routeDestItem.getDestItemField()));
                        tag.setTagValue(
                                findDestOtherValueList(routeDestItem.getDestItemField(), routeRule.getDestList()));
                        tag.setTagType(TagConstant.TYPE.SYSTEM);
                        tag.setTagOperator(TagConstant.OPERATOR.NOT_IN);
                    } else {
                        // 路由到具体过滤条件，具体过滤条件设置
                        tag.setTagValue(routeDestItem.getDestItemValue());
                        tag.setTagField(transferFemasDestTagFieldToFemas(routeDestItem.getDestItemField()));
                        tag.setTagType(TagConstant.TYPE.SYSTEM);
                        tag.setTagOperator(TagConstant.OPERATOR.EQUAL);
                    }

                    tagList.add(tag);
                }
                femasTagRule.setTags(tagList);

                femasRouteDest.setDestItemList(femasTagRule);
                femasRouteDestList.add(femasRouteDest);
            }
        }

        return femasRouteDestList;
    }

    /**
     * 服务路由规则中存在 路由 到 其他 的规则时，获取指定 destItemField 字段名称在当前
     * routeRuleItem的 routeRuleDestList中出现的 destItemValue值列表，然后通过 不包含 的逻辑判断，实现路由到 其他 的效果
     *
     * @param destItemField 字段名称
     * @param routeDestList
     * @return
     */
    private static String findDestOtherValueList(String destItemField, List<RouteDest> routeDestList) {
        StringBuilder destOtherValueList = new StringBuilder();
        if (!CollectionUtil.isEmpty(routeDestList)) {
            for (RouteDest routeDest : routeDestList) {
                if (!CollectionUtil.isEmpty(routeDest.getDestItemList())) {
                    for (RouteDestItem routeDestItem : routeDest.getDestItemList()) {
                        if (StringUtils.equals(routeDestItem.getDestItemField(), destItemField)) {
                            destOtherValueList.append(routeDestItem.getDestItemValue()).append(",");
                        }
                    }
                }
            }
        }

        return destOtherValueList.toString();
    }

    /**
     * Consul server里的metadata是Femas_PROG_VERSION这种类型的
     *
     * @param tagField
     * @return
     */
    public static String transferFemasDestTagFieldToFemas(String tagField) {
        switch (tagField) {
            case FemasConstant.FEMAS_META_APPLICATION_ID_KEY:
                return FemasConstant.FEMAS_META_APPLICATION_ID_KEY;
            case FemasConstant.FEMAS_META_APPLICATION_VERSION_KEY:
                return FemasConstant.FEMAS_META_APPLICATION_VERSION_KEY;
            case FemasConstant.FEMAS_META_GROUP_ID_KEY:
                return FemasConstant.FEMAS_META_GROUP_ID_KEY;
            default:
                return tagField;
        }
    }

    /**
     * 这里的上游是在配置route规则的下游provider视角
     * 实际上route规则作用在上游consumer
     * 所以这里要把上游的字段换成systag的字段
     *
     * @param tagField
     * @return
     */
    public static String transferFemasSystemTagFieldToFemasSystemTagField(String tagField) {
        switch (tagField) {
            case FemasConstant.SOURCE_APPLICATION_ID:
                return FemasConstant.FEMAS_APPLICATION_ID;
            case FemasConstant.SOURCE_GROUP_ID:
                return FemasConstant.FEMAS_GROUP_ID;
            case FemasConstant.SOURCE_CONNECTION_IP:
                return FemasConstant.FEMAS_LOCAL_IP;
            case FemasConstant.SOURCE_APPLICATION_VERSION:
                return FemasConstant.FEMAS_APPLICATION_VERSION;
            case FemasConstant.SOURCE_SERVICE_NAME:
                return FemasConstant.FEMAS_SERVICE_NAME;
            case FemasConstant.DESTINATION_INTERFACE:
                return FemasConstant.DESTINATION_INTERFACE;
            case FemasConstant.REQUEST_HTTP_METHOD:
                return FemasConstant.REQUEST_HTTP_METHOD;
            case FemasConstant.SOURCE_NAMESPACE_SERVICE_NAME:
                return FemasConstant.NAMESPACE_SERVICE_NAME;
            default:
                return tagField;
        }
    }

    /**
     * @see com.tencent.tsf.femas.common.spi.SpiExtensionClass#getType()
     */
    @Override
    public String getType() {
        return ConfigHandlerTypeEnum.ROUTER.getType();
    }

    /**
     * 指定某个service
     *
     * @param service
     */
    public synchronized void subscribeServiceConfig(Service service) {
        String routeKey = "route/" + service.getNamespace() + "/" + service.getName() + "/";
        subscribeRouteConfig(routeKey);
    }

    /**
     * 兼容老的Spring SDK
     */
    public synchronized void subscribeAllRouteConfig() {
        String routeKey = "route/" + FemasContext.getSystemTag(FemasConstant.FEMAS_NAMESPACE_ID) + "/";
        subscribeRouteConfig(routeKey);
    }

    /**
     * 兼容gateway
     */
    public synchronized void subscribeNamespaceConfig(String namespace) {
        String routeKey = "route/" + namespace + "/";
        subscribeRouteConfig(routeKey);
    }

    public static class ROUTE_DEST_SYSTEM_VALUE {

        /**
         * 匹配剩余其他
         */
        public static final String ELSE = "FEMAS_ROUTE_DEST_ELSE";

        /**
         * 匹配所有
         */
        public static final String ALL = "FEMAS_ROUTE_DEST_ALL";
    }
}
