package com.tencent.tsf.femas.adaptor.paas.governance.auth;

import com.tencent.tsf.femas.adaptor.paas.config.FemasPaasConfigManager;
import com.tencent.tsf.femas.common.constant.FemasConstant;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.serialize.JSONSerializer;
import com.tencent.tsf.femas.common.tag.Tag;
import com.tencent.tsf.femas.common.tag.TagRule;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.config.Config;
import com.tencent.tsf.femas.config.ConfigChangeListener;
import com.tencent.tsf.femas.config.enums.PropertyChangeType;
import com.tencent.tsf.femas.config.model.ConfigChangeEvent;
import com.tencent.tsf.femas.governance.auth.AuthorizationManager;
import com.tencent.tsf.femas.plugin.config.ConfigHandler;
import com.tencent.tsf.femas.plugin.config.enums.ConfigHandlerTypeEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class FemasAuthHandler extends ConfigHandler {

    private static final Logger logger = LoggerFactory.getLogger(FemasAuthHandler.class);

    private static com.tencent.tsf.femas.plugin.impl.config.rule.auth.AuthRuleGroup convertAuthGroup(AuthRuleGroup
            authRuleGroup) throws Throwable {
        com.tencent.tsf.femas.plugin.impl.config.rule.auth.AuthRuleGroup femasGroup = new com.tencent.tsf.femas.plugin.impl.config.rule.auth.AuthRuleGroup();

        femasGroup.setType(authRuleGroup.getType());
        femasGroup.setRules(convertAuthRules(authRuleGroup.getRules()));

        return femasGroup;
    }

    private static List<TagRule> convertAuthRules(List<AuthRule> authRules) throws Throwable {
        List<TagRule> tagRules = new ArrayList<>();

        for (AuthRule authRule : authRules) {
            TagRule tagRule = new TagRule();

            if (CollectionUtils.isNotEmpty(authRule.getTags())) {
                for (Tag tag : authRule.getTags()) {
                    tag.setTagField(convertTagField(tag.getTagField()));
                }
            }

            tagRule.setTags(authRule.getTags());
            tagRules.add(tagRule);
        }

        return tagRules;
    }

    /**
     * source是针对上游来说的
     * destination就是本机信息
     *
     * @param authTagField
     * @return
     */
    private static String convertTagField(String authTagField) {
        switch (authTagField) {
            case FemasConstant.SOURCE_APPLICATION_ID:
                return FemasConstant.SOURCE_APPLICATION_ID;
            case FemasConstant.SOURCE_GROUP_ID:
                return FemasConstant.SOURCE_GROUP_ID;
            case FemasConstant.SOURCE_CONNECTION_IP:
                return FemasConstant.SOURCE_CONNECTION_IP;
            case FemasConstant.SOURCE_APPLICATION_VERSION:
                return FemasConstant.SOURCE_APPLICATION_VERSION;
            case FemasConstant.SOURCE_SERVICE_NAME:
                return FemasConstant.SOURCE_SERVICE_NAME;
            case FemasConstant.DESTINATION_APPLICATION_ID:
                return FemasConstant.FEMAS_APPLICATION_ID;
            case FemasConstant.DESTINATION_APPLICATION_VERSION:
                return FemasConstant.FEMAS_APPLICATION_VERSION;
            case FemasConstant.DESTINATION_GROUP_ID:
                return FemasConstant.FEMAS_GROUP_ID;
            case FemasConstant.DESTINATION_INTERFACE:
                return FemasConstant.FEMAS_INTERFACE;
            default:
                return authTagField;
        }
    }

    private static AuthRuleGroup parseAuthRuleGroup(String authRuleGroupString) {
        try {
            if (!StringUtils.isEmpty(authRuleGroupString)) {
                return JSONSerializer.deserializeStr(AuthRuleGroup.class, authRuleGroupString);
            }
            throw new RuntimeException("AuthRuleGroupJsonString rule is null.");
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
        return ConfigHandlerTypeEnum.AUTH.getType();
    }

    /**
     * 指定某个service
     *
     * @param service
     */
    public synchronized void subscribeServiceConfig(final Service service) {
        String authKey = "authority/" + service.getNamespace() + "/" + service.getName() + "/";

        Config<String> config = FemasPaasConfigManager.getConfig();
        config.subscribe(authKey, new ConfigChangeListener<String>() {
            @Override
            public void onChange(List<ConfigChangeEvent<String>> configChangeEvents) {
                if (CollectionUtil.isEmpty(configChangeEvents)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[Femas ADAPTOR AUTH] Auth change event collection is empty, No need update.");
                    }
                    return;
                }
                for (ConfigChangeEvent changeEvent : configChangeEvents) {
                    logger.info(
                            "[Femas ADAPTOR AUTH] Starting process auth change event. Changed event  : " + changeEvent
                                    .toString());
                    try {
                        AuthRuleGroup authRuleGroup = null;
                        // 删除规则
                        if (changeEvent.getChangeType() == PropertyChangeType.DELETED) {
                            AuthorizationManager.disableAuthRuleGroup(service);
                        } else {
                            authRuleGroup = parseAuthRuleGroup((String) changeEvent.getNewValue());
                            AuthorizationManager.refreshAuthRuleGroup(service, convertAuthGroup(authRuleGroup));
                        }
                        logger.info("[Femas ADAPTOR AUTH] Update auth group. AuthRuleGroup = " + authRuleGroup);
                    } catch (Throwable ex) {
                        logger.error("[Femas ADAPTOR AUTH] auth group load error. Service : " + service, ex);
                    }
                }
            }

            @Override
            public void onChange(ConfigChangeEvent<String> changeEvent) {

            }
        });
    }

}
