/**
 * Tencent is pleased to support the open source community by making Polaris available.
 * <p>
 * Copyright (C) 2019 THL A29 Limited, a Tencent company. All rights reserved.
 * <p>
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * <p>
 * https://opensource.org/licenses/BSD-3-Clause
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.tencent.tsf.femas.plugin.impl.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tencent.tsf.femas.plugin.config.PluginConfigImpl;
import com.tencent.tsf.femas.plugin.config.gov.ServiceRouterConfig;
import com.tencent.tsf.femas.plugin.impl.config.rule.router.RouteRuleGroup;
import com.tencent.tsf.femas.plugin.impl.config.rule.router.RouterType;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务路由相关配置项
 *
 * @author andrewshan
 * @author leoziltong
 * @date 2019/8/20
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceRouterConfigImpl extends PluginConfigImpl implements ServiceRouterConfig {

    @JsonProperty
    private List<String> chain;

    @JsonProperty
    private RouteRuleGroup routeRule;

    @Override
    public List<String> getChain() {
        return chain;
    }

    public void setChain(List<String> chain) {
        this.chain = chain;
    }

    @Override
    public void verify() throws IllegalArgumentException {

    }

    public RouteRuleGroup getRouteRule() {
        return routeRule;
    }

    public void setRouteRule(RouteRuleGroup routeRule) {
        this.routeRule = routeRule;
    }

    @Override
    public void setDefault() {
        if (CollectionUtils.isEmpty(chain)) {
            chain = new ArrayList<>();
            for (RouterType routerType : RouterType.sortedValues()) {
                chain.add(routerType.getName());
            }
        }
    }

    @Override
    public String toString() {
        return "ServiceRouterConfigImpl{" +
                "chain=" + chain +
                '}';
    }
}
