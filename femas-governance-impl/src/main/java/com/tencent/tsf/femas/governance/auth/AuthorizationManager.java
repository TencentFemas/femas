package com.tencent.tsf.femas.governance.auth;

import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.governance.auth.entity.AuthRuleGroup;
import com.tencent.tsf.femas.governance.config.FemasPluginContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Auth服务权限管理客户端
 */
public class AuthorizationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationManager.class);

    private static final IAuthentication<AuthRuleGroup> authentication = FemasPluginContext.getAuthentication();

    /**
     * 鉴权
     */
    public static Boolean authenticate(Service service) {
        return authentication.authenticate(service);
    }

    public static void refreshAuthRuleGroup(Service service, AuthRuleGroup authRuleGroup) {
        authentication.refreshAuthRuleGroup(service, authRuleGroup);
        LOGGER.info("Refresh auth rule group. Service : " + service + ", authRuleGroup : " + authRuleGroup);
    }

    public static void disableAuthRuleGroup(Service service) {
        authentication.disableAuthRuleGroup(service);
        LOGGER.info("Disable auth rule group. Service : " + service);
    }

}
