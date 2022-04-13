package com.tencent.tsf.femas.storage;


import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.entity.PageService;
import com.tencent.tsf.femas.entity.Record;
import com.tencent.tsf.femas.entity.ServiceModel;
import com.tencent.tsf.femas.entity.dcfg.Config;
import com.tencent.tsf.femas.entity.dcfg.ConfigReleaseLog;
import com.tencent.tsf.femas.entity.dcfg.ConfigRequest;
import com.tencent.tsf.femas.entity.dcfg.ConfigVersion;
import com.tencent.tsf.femas.entity.log.LogModel;
import com.tencent.tsf.femas.entity.namespace.Namespace;
import com.tencent.tsf.femas.entity.namespace.NamespacePageModel;
import com.tencent.tsf.femas.entity.registry.ApiModel;
import com.tencent.tsf.femas.entity.registry.RegistryConfig;
import com.tencent.tsf.femas.entity.registry.RegistrySearch;
import com.tencent.tsf.femas.entity.registry.ServiceApi;
import com.tencent.tsf.femas.entity.rule.*;
import com.tencent.tsf.femas.entity.rule.auth.AuthRuleModel;
import com.tencent.tsf.femas.entity.rule.auth.ServiceAuthRuleModel;
import com.tencent.tsf.femas.entity.rule.breaker.CircuitBreakerModel;
import com.tencent.tsf.femas.entity.rule.limit.LimitModel;
import com.tencent.tsf.femas.entity.rule.route.Tolerate;
import com.tencent.tsf.femas.entity.rule.route.TolerateModel;
import com.tencent.tsf.femas.entity.service.ServiceEventModel;

import java.util.HashSet;
import java.util.List;

/**
 * @author Cody
 * @date 2021 2021/7/28 2:22 下午
 */
public interface DataOperation {

    /**
     * 配置注册中心
     * @param registryConfig
     * @return
     */
    int configureRegistry(RegistryConfig registryConfig);

    /**
     * 查询注册中心
     * @param registrySearch
     * @return
     */
    List<RegistryConfig> fetchRegistryConfigs(RegistrySearch registrySearch);

    /**
     * 删除注册中心
     * @param registryId
     * @return
     */
    int deleteRegistry(String registryId);

    /**
     * 查询注册中心
     * @param registryId
     * @return
     */
    RegistryConfig fetchRegistryById(String registryId);

    /**
     * 获取命名空间
     * @param namespaceId
     * @return
     */
    Namespace fetchNamespaceById(String namespaceId);

    /**
     * 修改命名空间
     * @param namespace
     * @return
     */
    int modifyNamespace(Namespace namespace);

    /**
     * 创建命名空间
     * @param namespace
     * @return
     */
    int createNamespace(Namespace namespace);

    /**
     * 删除命名空间
     * @param namespaceId
     * @return
     */
    int deleteNamespaceById(String namespaceId);

    /**
     * 服务启动创建自动创建命名空间
     * @param registryAddress
     * @param namespaceId
     * @return
     */
    void initNamespace(String registryAddress, String namespaceId);

    /**
     * 条件查询命名空间
     * @param namespaceModel
     * @return
     */
    PageService<Namespace> fetchNamespaces(NamespacePageModel namespaceModel);

    /**
     * 获取注册中心下的命名空间数量
     * @param registryId
     * @return
     */
    int getNamespacesCountByRegistry(String registryId);

    /**
     * 上报api
     * @param namespaceId
     * @param serviceName
     * @param apiId
     * @param data
     */
    void reportServiceApi(String namespaceId, String serviceName, String groupId, String apiId, String data);

    /**
     * 删除离线服务的api
     * @param namespaceId
     * @param serviceName
     * @param onlineVersions
     */
    void deleteOfflinceServiceApi(String namespaceId, String serviceName, HashSet<String> onlineVersions);

    /**
     * 上报服务事件
     * @param namespaceId
     * @param serviceName
     * @param eventId
     * @param data
     */
    void reportServiceEvent(String namespaceId, String serviceName, String eventId, String data);

    /**
     * 获取服务事件数据
     * @param serviceEventModel
     * @return
     */
    PageService<FemasEventData> fetchEventData(ServiceEventModel serviceEventModel);

    /**
     * 获取服务api
     * @param apiModel
     * @return
     */
    PageService<ServiceApi> fetchServiceApiData(ApiModel apiModel);

    /**
     * 配置鉴权规则
     * @param authRule
     * @return
     */
    int configureAuthRule(FemasAuthRule authRule);

    /**
     * 查询鉴权规则
     * @param serviceModel
     * @return
     */
    List<FemasAuthRule> fetchAuthRule(ServiceModel serviceModel);

    /**
     * 删除鉴权规则
     * @param serviceAuthRuleModel
     * @return
     */
    int deleteAuthRule(ServiceAuthRuleModel serviceAuthRuleModel);

    /**
     * 配置熔断规则
     * @param circuitBreakerRule
     * @return
     */
    Result configureBreakerRule(FemasCircuitBreakerRule circuitBreakerRule);

    /**
     * 查询熔断规则
     * @param circuitBreakerModel
     * @return
     */
    List<FemasCircuitBreakerRule> fetchBreakerRule(CircuitBreakerModel circuitBreakerModel);

    /**
     * 删除熔断规则
     * @param ruleModel
     * @return
     */
    int deleteBreakerRule(RuleModel ruleModel);

    /**
     * 配置限流规则
     * @param limitRule
     * @return
     */
    int configureLimitRule(FemasLimitRule limitRule);

    /**
     * 查询限流规则
     * @param serviceModel
     * @return
     */
    List<FemasLimitRule> fetchLimitRule(LimitModel serviceModel);

    /**
     * 删除限流规则
     * @param ruleModel
     * @return
     */
    int deleteLimitRule(RuleModel ruleModel);

    /**
     * 查询路由规则
     * @param serviceModel
     * @return
     */
    List<FemasRouteRule> fetchRouteRule(ServiceModel serviceModel);

    /**
     * 删除路由规则
     * @param ruleModel
     * @return
     */
    int deleteRouteRule(RuleModel ruleModel);

    /**
     * 配置路由规则
     * @param routeRule
     * @return
     */
    int configureRouteRule(FemasRouteRule routeRule);

    /**
     * 新增操作日志
     * @param record
     * @return
     */
    int configureRecord(Record record);

    /**
     * 编辑路由容错开关
     * @param tolerate
     * @return
     */
    int configureTolerant(Tolerate tolerate);

    /**
     * 查询路由容错开关
     * @param tolerateModel
     */
     boolean fetchTolerant(TolerateModel tolerateModel);

    /**
     * 查询单条鉴权规则
     * @param ruleSearch
     * @return
     */
    FemasAuthRule fetchAuthRuleById(RuleSearch ruleSearch);

    /**
     * 查询单条路由规则
     * @param ruleSearch
     */
    FemasRouteRule fetchRouteRuleById(RuleSearch ruleSearch);

    /**
     * 查询单条熔断规则
     * @param ruleSearch
     * @return
     */
    FemasCircuitBreakerRule fetchBreakerRuleById(RuleSearch ruleSearch);

    /**
     * 查询单条限流规则
     * @param ruleSearch
     * @return
     */
    FemasLimitRule fetchLimitRuleById(RuleSearch ruleSearch);

    /**
     * 查询操作日志
     * @param logModel
     * @return
     */
    PageService<Record> fetchLogs(LogModel logModel);

    /**
     * 查询命名空间下的路由规则
     * @param namespaceId
     * @return
     */
    List<FemasRouteRule> fetchRouteRuleByNamespaceId(String namespaceId);

    /**
     * 拉取配置表的值
     * @param key
     * @return
     */
    String fetchConfig(String key);

    /**
     * 修改配置表
     * @param key
     * @return
     */
    int configureConfig(String key, String value);


    int configureDcfg(Config config);

    PageService<Config> fetchDcfgs(ConfigRequest request);

    int deleteDcfg(String configId, String namespaceId);

    List<Config> fetchDcfgOther(List<String> configIdList);

    int configureDcfgVersion(ConfigVersion configVersion);

    PageService<ConfigVersion> fetchDcfgVersions(ConfigRequest request);

    int deleteDcfgVersion(String configVersionId, String configId);

    int configureDcfgReleaseLog(ConfigReleaseLog configReleaseLog);

    /**
     * 查询鉴权规则
     * @param authRuleModel
     * @return
     */
    PageService<FemasAuthRule> fetchAuthRulePages(AuthRuleModel authRuleModel);

    /**
     * 查询熔断规则
     * @param circuitBreakerModel
     * @return
     */
    PageService<FemasCircuitBreakerRule> fetchBreakerRulePages(CircuitBreakerModel circuitBreakerModel);

    /**
     * 查询限流规则
     * @param serviceModel
     * @return
     */
    PageService<FemasLimitRule> fetchLimitRulePages(LimitModel serviceModel);

    /**
     * 查询路由规则
     * @param serviceModel
     * @return
     */
    PageService<FemasRouteRule> fetchRouteRulePages(ServiceModel serviceModel);

}
