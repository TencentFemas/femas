/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.tsf.femas.governance.auth;

import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.common.tag.TagRule;
import com.tencent.tsf.femas.common.tag.engine.TagEngine;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.governance.auth.constant.AuthConstant;
import com.tencent.tsf.femas.governance.auth.entity.AuthRuleConfig;
import com.tencent.tsf.femas.governance.auth.entity.AuthRuleGroup;
import com.tencent.tsf.femas.governance.config.impl.AuthenticateConfigImpl;
import com.tencent.tsf.femas.governance.event.AuthEventCollector;
import com.tencent.tsf.femas.governance.plugin.context.ConfigContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author leoziltong
 * @Date: 2021/6/2 20:42
 */
public class Authentication implements IAuthentication<AuthRuleGroup> {

    private static final  Logger LOGGER = LoggerFactory.getLogger(Authentication.class);

    private static Map<Service, AuthRuleGroup> authRuleGroupMap = new ConcurrentHashMap<>();

    private volatile Context commonContext = ContextFactory.getContextInstance();

    /**
     * 鉴权
     */
    @Override
    public Boolean authenticate(Service service) {
        LOGGER.debug("[FEMAS Auth] Start checking request...");

        if (service == null) {
            return true;
        }

        AuthRuleGroup authRuleGroup = authRuleGroupMap.get(service);
        Boolean authResult = checkAuthRuleGroup(authRuleGroup);

        if (!authResult) {
            LOGGER.debug("[FEMAS Auth] Auth false, authRuleGroup : {}", authRuleGroup);
        }

        return authResult;
    }

    @Override
    public void refreshAuthRuleGroup(Service service, AuthRuleGroup authRuleGroup) {
        authRuleGroupMap.put(service, authRuleGroup);
        LOGGER.info("Refresh auth rule group. Service : " + service + ", authRuleGroup : " + authRuleGroup);
    }

    @Override
    public void disableAuthRuleGroup(Service service) {
        if (service == null) {
            return;
        }

        authRuleGroupMap.remove(service);
        LOGGER.info("Disable auth rule group. Service : " + service);
    }

    public Boolean checkAuthRuleGroup(AuthRuleGroup authRuleGroup) {
        if (authRuleGroup != null && !CollectionUtil.isEmpty(authRuleGroup.getRules())) {
            Boolean authRuleGroupHit = false;
            for (TagRule authRule : authRuleGroup.getRules()) {
                if (TagEngine.checkRuleHitByUpstreamTags(authRule)) {
                    authRuleGroupHit = true;
                    break;
                }
            }

            if (StringUtils.equals(authRuleGroup.getType(), AuthConstant.WHITE_LIST)) {
                authRuleGroupHit = authRuleGroupHit;
            } else if (StringUtils.equals(authRuleGroup.getType(), AuthConstant.BLACK_LIST)) {
                authRuleGroupHit = !authRuleGroupHit;
            } else {
                return true;
            }
            if (!authRuleGroupHit) {
                AuthEventCollector.addAuthEvent(authRuleGroup, Context.getRpcInfo().getAll());
            }
            return authRuleGroupHit;
        }
        return true;
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public String getName() {
        return "femasAuthenticate";
    }

    @Override
    public void init(ConfigContext conf) throws FemasRuntimeException {
        AuthenticateConfigImpl authenticate = (AuthenticateConfigImpl) conf.getConfig().getAuthenticate();
        if (authenticate == null || CollectionUtil.isEmpty(authenticate.getAuthRule())) {
            return;
        }
        String namespaceId = System.getProperty("femas_namespace_id");
        try {
            for (AuthRuleConfig authRuleConfig : authenticate.getAuthRule()) {
                Service service = new Service();
                service.setNamespace(namespaceId);
                service.setName(authRuleConfig.getServiceName());
                refreshAuthRuleGroup(service, authRuleConfig.getAuthRuleGroup());
            }
        } catch (Exception e) {
            throw new FemasRuntimeException("auth rule refresh error");
        }
        LOGGER.info("init auth rule: {}", authenticate.getAuthRule().toString());
    }

    @Override
    public void destroy() {

    }
}
