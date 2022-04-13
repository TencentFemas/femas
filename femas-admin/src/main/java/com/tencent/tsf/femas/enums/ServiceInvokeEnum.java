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
package com.tencent.tsf.femas.enums;

import com.tencent.tsf.femas.entity.ServiceModel;
import com.tencent.tsf.femas.entity.dcfg.ConfigRequest;
import com.tencent.tsf.femas.entity.namespace.Namespace;
import com.tencent.tsf.femas.entity.namespace.NamespacePageModel;
import com.tencent.tsf.femas.entity.registry.RegistryModel;
import com.tencent.tsf.femas.entity.registry.RegistrySearch;
import com.tencent.tsf.femas.entity.rule.*;
import com.tencent.tsf.femas.entity.rule.auth.AuthRuleModel;
import com.tencent.tsf.femas.entity.rule.auth.ServiceAuthRuleModel;
import com.tencent.tsf.femas.entity.rule.breaker.CircuitBreakerModel;
import com.tencent.tsf.femas.entity.rule.limit.LimitModel;
import com.tencent.tsf.femas.entity.rule.route.Tolerate;


/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/4/28 20:50
 */
public enum ServiceInvokeEnum {

    REGISTRY_MANAGER,
    NAMESPACE_MANGER,
    DCFG_CONFIG,
    DCFG_CONFIG_VERSION,
    SERVICE_AUTH,
    SERVICE_BREAK,
    SERVICE_LIMIT,
    SERVICE_ROUTE;

    public enum ApiInvokeEnum {

        /**
         * method可以不填，默认取endpoint 的stack trace的方法名，即保证endpoint的方法名和executor方法名一致
         */
        REGISTRY_MANAGER_CONFIG("配置注册中心", "", new Class[]{RegistryModel.class}, ServiceInvokeEnum.REGISTRY_MANAGER),
        CHECK_CERTIFICATE_CONF("校验注册中心", "", new Class[]{RegistryModel.class}, ServiceInvokeEnum.REGISTRY_MANAGER),
        REGISTRY_MANAGER_DESCRIBE("获取注册中心", "", new Class[]{RegistrySearch.class}, ServiceInvokeEnum.REGISTRY_MANAGER),
        FETCH_REGISTRY_CLUSTER("获取注册中心集群信息", "", new Class[]{String.class}, ServiceInvokeEnum.REGISTRY_MANAGER),
        REGISTRY_MANAGER_DELETE("删除注册中心", "", new Class[]{String.class}, ServiceInvokeEnum.REGISTRY_MANAGER),
        REGISTRY_MANAGER_DESCRIBE_SERVICES("获取注册中心服务列表", "", new Class[]{String.class,String.class, Integer.class, Integer.class, String.class}, ServiceInvokeEnum.REGISTRY_MANAGER),
        REGISTRY_MANAGER_DESCRIBE_INSTANCES("获取注册中心服务实例列表", "", new Class[]{String.class, String.class}, ServiceInvokeEnum.REGISTRY_MANAGER),

        // namespace
        NAMESPACE_MANGER_FETCH("查询命名空间","", new Class[]{NamespacePageModel.class}, ServiceInvokeEnum.NAMESPACE_MANGER),
        NAMESPACE_MANGER_DELETE("删除命名空间","", new Class[]{String.class}, ServiceInvokeEnum.NAMESPACE_MANGER),
        NAMESPACE_MANGER_CREATE("创建命名空间","", new Class[]{Namespace.class}, ServiceInvokeEnum.NAMESPACE_MANGER),
        NAMESPACE_MANGER_MODIFY("修改命名空间","", new Class[]{Namespace.class}, ServiceInvokeEnum.NAMESPACE_MANGER),
        NAMESPACE_MANGER_FETCH_BY_ID("通过命名空间id查询命名空间","", new Class[]{String.class}, ServiceInvokeEnum.NAMESPACE_MANGER),
        NAMESPACE_MANGER_INIT("服务自动创建命名空间","", new Class[]{String.class,String.class}, ServiceInvokeEnum.NAMESPACE_MANGER),


        // dcfg
        DCFG_CONFIG_CONFIGURE("创建或更新配置", "", new Class[]{ConfigRequest.class},ServiceInvokeEnum.DCFG_CONFIG),
        DCFG_CONFIG_FETCH("查看配置列表", "", new Class[]{ConfigRequest.class},ServiceInvokeEnum.DCFG_CONFIG),
        DCFG_CONFIG_FETCH_BY_ID("查看配置", "", new Class[]{ConfigRequest.class},ServiceInvokeEnum.DCFG_CONFIG),
        DCFG_CONFIG_DELETE("删除配置", "", new Class[]{ConfigRequest.class},ServiceInvokeEnum.DCFG_CONFIG),

        // dcfg version
        DCFG_CONFIG_VERSION_FETCH("查看配置版本", "", new Class[]{ConfigRequest.class},ServiceInvokeEnum.DCFG_CONFIG_VERSION),
        DCFG_CONFIG_VERSION_OPERATE("操作配置版本", "", new Class[]{ConfigRequest.class},ServiceInvokeEnum.DCFG_CONFIG_VERSION),
        DCFG_CONFIG_VERSION_DELETE("删除版本", "", new Class[]{ConfigRequest.class},ServiceInvokeEnum.DCFG_CONFIG_VERSION),



        // serviceBreak
        SERVICE_BREAK_FETCH("查询服务熔断规则","",new Class[]{CircuitBreakerModel.class},ServiceInvokeEnum.SERVICE_BREAK),
        SERVICE_BREAK_CONFIG("配置熔断规则","",new Class[]{
            FemasCircuitBreakerRule.class},ServiceInvokeEnum.SERVICE_BREAK),
        SERVICE_BREAK_DELETE("删除服务熔断规则","",new Class[]{
                RuleModel.class},ServiceInvokeEnum.SERVICE_BREAK),

        // serviceAuth
        SERVICE_AUTH_FETCH("查询服务鉴权规则","",new Class[]{AuthRuleModel.class},ServiceInvokeEnum.SERVICE_AUTH),
        SERVICE_AUTH_CONFIG("配置鉴权规则","",new Class[]{FemasAuthRule.class},ServiceInvokeEnum.SERVICE_AUTH),
        SERVICE_AUTH_DELETE("删除服务鉴权规则","",new Class[]{ServiceAuthRuleModel.class},ServiceInvokeEnum.SERVICE_AUTH),

        // serviceLimit
        SERVICE_LIMIT_FETCH("查询服务限流规则","",new Class[]{LimitModel.class},ServiceInvokeEnum.SERVICE_LIMIT),
        SERVICE_LIMIT_CONFIG("配置限流规则","",new Class[]{FemasLimitRule.class},ServiceInvokeEnum.SERVICE_LIMIT),
        SERVICE_LIMIT_DELETE("删除服务限流规则","",new Class[]{RuleModel.class},ServiceInvokeEnum.SERVICE_LIMIT),

        // serviceRoute
        SERVICE_ROUTE_FETCH("查询服务路由规则","",new Class[]{ServiceModel.class},ServiceInvokeEnum.SERVICE_ROUTE),
        SERVICE_ROUTE_CONFIG("配置服务路由规则","",new Class[]{FemasRouteRule.class},ServiceInvokeEnum.SERVICE_ROUTE),
        SERVICE_ROUTE_DELETE("删除服务路由规则","",new Class[]{RuleModel.class},ServiceInvokeEnum.SERVICE_ROUTE),
        SERVICE_ROUTE_TOLERANT("配置服务路由容错","",new Class[]{Tolerate.class},ServiceInvokeEnum.SERVICE_ROUTE)
        ;

        String name;
        String method;
        Class<?>[] paramClazz;
        ServiceInvokeEnum serviceInvokeEnum;

        ApiInvokeEnum(String name, String method, Class<?>[] paramClazz, ServiceInvokeEnum serviceInvokeEnum) {
            this.name = name;
            this.method = method;
            this.paramClazz = paramClazz;
            this.serviceInvokeEnum = serviceInvokeEnum;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Class<?>[] getParamClazz() {
            return paramClazz;
        }

        public void setParamClazz(Class<?>[] paramClazz) {
            this.paramClazz = paramClazz;
        }

        public ServiceInvokeEnum getServiceInvokeEnum() {
            return serviceInvokeEnum;
        }

        public void setServiceInvokeEnum(ServiceInvokeEnum serviceInvokeEnum) {
            this.serviceInvokeEnum = serviceInvokeEnum;
        }

        public static ApiInvokeEnum getTypeByName(String name) {
            for (ApiInvokeEnum type : ApiInvokeEnum.values()) {
                if (type.name.equalsIgnoreCase(name))
                    return type;
            }
            return null;
        }
    }

}
