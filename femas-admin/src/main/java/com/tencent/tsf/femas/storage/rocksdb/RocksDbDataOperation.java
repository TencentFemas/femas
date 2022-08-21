package com.tencent.tsf.femas.storage.rocksdb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tsf.femas.common.serialize.JSONSerializer;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.constant.AdminConstants;
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
import com.tencent.tsf.femas.entity.rule.lane.LaneInfo;
import com.tencent.tsf.femas.entity.rule.lane.LaneInfoModel;
import com.tencent.tsf.femas.entity.rule.lane.LaneRule;
import com.tencent.tsf.femas.entity.rule.lane.LaneRuleModel;
import com.tencent.tsf.femas.entity.rule.limit.LimitModel;
import com.tencent.tsf.femas.entity.rule.route.Tolerate;
import com.tencent.tsf.femas.entity.rule.route.TolerateModel;
import com.tencent.tsf.femas.entity.service.ServiceEventModel;
import com.tencent.tsf.femas.enums.LogModuleEnum;
import com.tencent.tsf.femas.service.IIDGeneratorService;
import com.tencent.tsf.femas.service.registry.OpenApiFactory;
import com.tencent.tsf.femas.service.registry.RegistryOpenApiInterface;
import com.tencent.tsf.femas.storage.DataOperation;
import com.tencent.tsf.femas.storage.StorageResult;
import com.tencent.tsf.femas.storage.config.RocksDbConditional;
import com.tencent.tsf.femas.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static com.tencent.tsf.femas.constant.AdminConstants.REGISTRY_ID_PREFIX;
import static com.tencent.tsf.femas.constant.AdminConstants.StorageKeyPrefix.REGISTRY_CONFIG_PREFIX;
import static com.tencent.tsf.femas.service.namespace.NamespaceMangerService.DEFAULT_DESC;
import static com.tencent.tsf.femas.service.namespace.NamespaceMangerService.DEFAULT_NAME;

/**
 * @author Cody
 * @date 2021 2021/7/28 2:24 下午
 */
@Component
@Conditional(RocksDbConditional.class)
public class RocksDbDataOperation implements DataOperation {

    @Autowired
    private StringRawKVStoreManager kvStoreManager;

    @Autowired
    private IIDGeneratorService iidGeneratorService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OpenApiFactory factory;

    /**
     * 配置注册中心
     *
     * @param registryConfig
     * @return
     */
    @Override
    public int configureRegistry(RegistryConfig registryConfig) {
        if (StringUtils.isEmpty(registryConfig.getRegistryId())) {
            registryConfig.setRegistryId(REGISTRY_ID_PREFIX.concat(iidGeneratorService.nextHashId()));
        }
        kvStoreManager.put(REGISTRY_CONFIG_PREFIX.concat(registryConfig.getRegistryId()), JSONSerializer.serializeStr(registryConfig));
        return 1;
    }

    /**
     * 查询注册中心
     *
     * @param registrySearch
     * @return
     */
    @Override
    public List<RegistryConfig> fetchRegistryConfigs(RegistrySearch registrySearch) {
        StorageResult<List<String>> result = kvStoreManager.scanPrefix(REGISTRY_CONFIG_PREFIX);
        ArrayList<RegistryConfig> res = new ArrayList<>();
        if (result.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)) {
            List<String> strings = result.getData();
            strings.stream().forEach(s -> {
                StorageResult<String> storageResult = kvStoreManager.get(s);
                if (storageResult.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)) {
                    RegistryConfig registryConfig = JSONSerializer.deserializeStr(RegistryConfig.class, storageResult.getData());
                    if(StringUtils.isEmpty(registrySearch.getRegistryType()) ||
                            registrySearch.getRegistryType().equalsIgnoreCase(registryConfig.getRegistryType())){
                        res.add(registryConfig);
                    }
                }
            });
        }
        return res;
    }

    /**
     * 删除注册中心
     *
     * @param registryId
     * @return
     */
    @Override
    public int deleteRegistry(String registryId) {
        kvStoreManager.delete(REGISTRY_CONFIG_PREFIX.concat(registryId));
        // 解绑命名空间关联
        StorageResult<String> res = kvStoreManager.get(AdminConstants.REGISTRY_NAMESPACE_PREFIX.concat(registryId));
        if(res.getData() != null){
            // 获取关联该注册中心的命名空间id
            List<String> ids = JSONSerializer.deserializeStr2List(String.class, res.getData());
            if(!CollectionUtil.isEmpty(ids)){
                for(String nsId : ids){
                    Namespace namespace = fetchNamespaceById(nsId);
                    if(namespace == null){
                        continue;
                    }
                    ArrayList<String> newRegistryIds = new ArrayList<>();
                    for (String id : namespace.getRegistryId()) {
                        if(!id.equals(registryId)){
                            newRegistryIds.add(id);
                        }
                    }
                    namespace.setRegistryId(newRegistryIds);
                    modifyNamespace(namespace);
                }
            }
        }
        kvStoreManager.delete(AdminConstants.REGISTRY_NAMESPACE_PREFIX.concat(registryId));
        return 1;
    }

    /**
     * 查询注册中心
     *
     * @param registryId
     * @return
     */
    @Override
    public RegistryConfig fetchRegistryById(String registryId) {
        StorageResult<String> result = kvStoreManager.get(REGISTRY_CONFIG_PREFIX.concat(registryId));
        if (result.getStatus().equals(StorageResult.SUCCESS) && !StringUtils.isEmpty(result.getData())) {
            return JSONSerializer.deserializeStr(RegistryConfig.class, result.getData());
        }
        return null;
    }

    /**
     * 获取命名空间
     * @param namespaceId
     * @return
     */
    @Override
    public Namespace fetchNamespaceById(String namespaceId) {
        StorageResult<String> result = kvStoreManager.get(AdminConstants.NAMESPACE_PATH_PREFIX.concat(namespaceId));
        if(result.getData() == null){
            return null;
        }
        return JSONSerializer.deserializeStr(Namespace.class, result.getData());
    }

    /**
     * 修改命名空间
     * @param namespace
     * @return
     */
    @Override
    public int modifyNamespace(Namespace namespace) {
        if (StringUtils.isEmpty(namespace.getNamespaceId())) {
            return 0;
        } else {
            Namespace exitsNamespace = fetchNamespaceById(namespace.getNamespaceId());
            if (exitsNamespace == null) {
                return 0;
            }
        }
        changeRelation(namespace);
        StorageResult res = kvStoreManager.put(AdminConstants.NAMESPACE_PATH_PREFIX.concat(namespace.getNamespaceId()), JSONSerializer.serializeStr(namespace));
        if(!CollectionUtil.isEmpty(namespace.getRegistryId())){
            RegistryConfig config = fetchRegistryById(namespace.getRegistryId().get(0));
            if(config == null){
                return 0;
            }
            RegistryOpenApiInterface registryOpenApiInterface = factory.select(config.getRegistryType());
            registryOpenApiInterface.modifyNamespace(config, namespace);
        }
        if(!res.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)){
            return 0;
        }
        return 1;
    }

    /**
     * 创建命名空间
     * @param namespace
     * @return
     */
    @Override
    public int createNamespace(Namespace namespace) {
        if (StringUtils.isEmpty(namespace.getNamespaceId())) {
            namespace.setNamespaceId(AdminConstants.NAMESPACE_ID_PREFIX.concat(iidGeneratorService.nextHashId()));
        } else {
            // 判断namespaceId是否存在
            Namespace exitsNamespace = fetchNamespaceById(namespace.getNamespaceId());
            if (exitsNamespace != null) {
                return 0;
            }
        }
        changeRelation(namespace);
        StorageResult res = kvStoreManager.put(AdminConstants.NAMESPACE_PATH_PREFIX.concat(namespace.getNamespaceId()), JSONSerializer.serializeStr(namespace));
        if(!CollectionUtil.isEmpty(namespace.getRegistryId())){
            RegistryConfig config = fetchRegistryById(namespace.getRegistryId().get(0));
            if(config == null){
                return 0;
            }
            RegistryOpenApiInterface registryOpenApiInterface = factory.select(config.getRegistryType());
            registryOpenApiInterface.createNamespace(config, namespace);
        }
        if(!res.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)){
            return 0;
        }
        return 1;
    }

    /**
     * 修改命名空间关联关系
     * @param namespace
     */
    public void changeRelation(Namespace namespace){
        Namespace oldNamespace = fetchNamespaceById(namespace.getNamespaceId());
        if(oldNamespace != null && !CollectionUtil.isEmpty(oldNamespace.getRegistryId())){
            releaseRelation(namespace);
        }
        if(!CollectionUtil.isEmpty(namespace.getRegistryId())) {
            for (String registryId : namespace.getRegistryId()) {
                // 修改关联关系
                List<String> namespaceIds = new ArrayList<>();
                namespaceIds.add(namespace.getNamespaceId());
                StorageResult<String> storageResult = kvStoreManager.putIfAbsent(AdminConstants.REGISTRY_NAMESPACE_PREFIX.concat(registryId), JSONSerializer.serializeStr(namespaceIds));
                // 已存在关联对象 在关联对象中添加命名空间
                if (storageResult.getData() != null) {
                    namespaceIds = JSONSerializer.deserializeStr2List(String.class, storageResult.getData());
                    boolean flag = true;
                    for (String nsId : namespaceIds) {
                        if (nsId.equals(namespace.getNamespaceId())) {
                            flag = false;
                        }
                    }
                    if (flag) {
                        namespaceIds.add(namespace.getNamespaceId());
                    }
                    kvStoreManager.put(AdminConstants.REGISTRY_NAMESPACE_PREFIX.concat(registryId), JSONSerializer.serializeStr(namespaceIds));
                }
            }
        }
    }

    public void releaseRelation(Namespace namespace){
        if(!CollectionUtil.isEmpty(namespace.getRegistryId())){
            for(String registryId : namespace.getRegistryId()){
                StorageResult<String> storageResult1 = kvStoreManager.get(AdminConstants.REGISTRY_NAMESPACE_PREFIX.concat(registryId));
                List<String> ids = JSONSerializer.deserializeStr2List(String.class, storageResult1.getData());
                ids.remove(AdminConstants.NAMESPACE_PATH_PREFIX.concat(namespace.getNamespaceId()));
                kvStoreManager.put(AdminConstants.REGISTRY_NAMESPACE_PREFIX.concat(registryId),JSONSerializer.serializeStr(ids));
            }
        }
    }

    /**
     * 删除命名空间
     *
     * @param namespaceId
     * @return
     */
    @Override
    public int deleteNamespaceById(String namespaceId) {
        StorageResult<String> storageResult = kvStoreManager.get(AdminConstants.NAMESPACE_PATH_PREFIX.concat(namespaceId));
        String res = storageResult.getData();
        if(StringUtils.isEmpty(res)){
            return 0;
        }
        Namespace namespace = JSONSerializer.deserializeStr(Namespace.class, res);
        // 解绑关联关系
        releaseRelation(namespace);
        kvStoreManager.delete(AdminConstants.NAMESPACE_PATH_PREFIX.concat(namespaceId));
        // TODO: 一个命名空间如果支持绑定多个注册中心，这里需要额外判断
        // TODO: 删除对应注册中心的ns，位置可能需要调整
        if (!CollectionUtil.isEmpty(namespace.getRegistryId())) {
            RegistryConfig config = fetchRegistryById(namespace.getRegistryId().get(0));
            if(config == null){
                return 0;
            }
            RegistryOpenApiInterface registryOpenApiInterface = factory.select(config.getRegistryType());
            registryOpenApiInterface.deleteNamespace(config, namespace);
        }
        return 1;
    }

    /**
     * 服务启动创建自动创建命名空间
     *
     * @param registryAddress
     * @param namespaceId
     * @return
     */
    @Override
    public void initNamespace(String registryAddress, String namespaceId) {
        if(StringUtils.isEmpty(registryAddress)){
            return;
        }
        StorageResult<List<String>> result = kvStoreManager.scanPrefixValue(REGISTRY_CONFIG_PREFIX);
        if(CollectionUtil.isEmpty(result.getData())){
            return;
        }
        if(fetchNamespaceById(namespaceId) != null){
            return;
        }

        String[] addresses = registryAddress.split(",");
        for(String registry : result.getData()){
            RegistryConfig config = JSONSerializer.deserializeStr(RegistryConfig.class, registry);
            //获取注册中心信息
            RegistryOpenApiInterface registryOpenApiInterface = factory.select(config.getRegistryType());
            List<Namespace> namespaces = registryOpenApiInterface.allNamespaces(config);
            Namespace remoteNamespace = namespaces.stream().filter(namespace -> namespace.getNamespaceId().equals(namespaceId)).findFirst().orElse(null);

            for(String address : addresses){
                // 对 localhost 进行转换
                String registryCluster = config.getRegistryCluster();
                if (StringUtils.isBlank(registryCluster)) {
                    continue;
                }
                if (registryCluster.contains("localhost")) {
                    registryCluster = registryCluster.replace("localhost", "127.0.0.1");
                }
                if (address.contains("localhost")) {
                    address = address.replace("localhost", "127.0.0.1");
                }
                if(registryCluster.contains(address)){
                    Namespace namespace = new Namespace();
                    namespace.setNamespaceId(namespaceId);
                    String namespaceName =DEFAULT_NAME;
                    if(remoteNamespace!=null){
                        namespaceName =StringUtils.isBlank(remoteNamespace.getName())?namespaceId: remoteNamespace.getName();
                    }
                    namespace.setName(namespaceName);
                    ArrayList<String> registryId = new ArrayList<>();
                    registryId.add(config.getRegistryId());
                    namespace.setRegistryId(registryId);
                    namespace.setDesc(DEFAULT_DESC);
                    createNamespace(namespace);
                    return;
                }
            }
        }
    }

    /**
     * 条件查询命名空间
     *
     * @param namespaceModel
     * @return
     */
    @Override
    public PageService<Namespace> fetchNamespaces(NamespacePageModel namespaceModel) {
        StorageResult<List<String>> result = kvStoreManager.scanPrefixValue(AdminConstants.NAMESPACE_PATH_PREFIX);
        ArrayList<Namespace> namespaces = new ArrayList<>();
        if (result.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)) {
            if(!CollectionUtil.isEmpty(result.getData())){
                for(String nsStr : result.getData()){
                    Namespace namespace = JSONSerializer.deserializeStr(Namespace.class, nsStr);
                    if(!StringUtils.isEmpty(namespaceModel.getName())){
                        if(!namespace.getName().contains(namespaceModel.getName()) && !namespace.getNamespaceId().contains(namespaceModel.getName())){
                            continue;
                        }
                    }
                    if(!StringUtils.isEmpty(namespaceModel.getRegistryId()) && !CollectionUtil.hasString(namespace.getRegistryId(), namespaceModel.getRegistryId())){
                        continue;
                    }
                    namespaces.add(namespace);
                }
            }
        }
        List<Namespace> data = PageUtil.pageList(namespaces, namespaceModel.getPageNo(), namespaceModel.getPageSize());
        return new PageService<Namespace>(data,namespaces.size());
    }

    /**
     * 获取注册中心下的命名空间数量
     *
     * @param registryId
     * @return
     */
    @Override
    public int getNamespacesCountByRegistry(String registryId) {
        StorageResult<String> storageResult = kvStoreManager.get(AdminConstants.REGISTRY_NAMESPACE_PREFIX.concat(registryId));
        Integer namespaceCount = 0;
        if (storageResult.getData() != null) {
            List<String> namespacesId = JSONSerializer.deserializeStr2List(String.class, storageResult.getData());
            namespaceCount = namespacesId.size();
        }
        return namespaceCount;
    }

    /**
     * 上报api
     *
     * @param namespaceId
     * @param serviceName
     * @param apiId
     * @param data
     */
    @Override
    public void reportServiceApi(String namespaceId, String serviceName, String groupId, String apiId, String data) {
        String key = "api-" + namespaceId + "-" + serviceName + "-" + groupId;
        kvStoreManager.put(key,data);
    }

    @Override
    public void deleteOfflinceServiceApi(String namespaceId, String serviceName, HashSet<String> onlineVersions) {
        HashSet<String> onlineKeys = new HashSet<>();
        for (String version: onlineVersions) {
            onlineKeys.add("api-" + namespaceId + "-" + serviceName + "-" + version);
        }
        StorageResult<List<String>> apiKeys = kvStoreManager.scanPrefix("api-" + namespaceId + "-" + serviceName);
        if (CollectionUtil.isNotEmpty(apiKeys.getData())) {
            for (String existKey: apiKeys.getData()) {
                if (!onlineKeys.contains(existKey)) {
                    kvStoreManager.delete(existKey);
                }
            }
        }
    }

    /**
     * 上报服务事件
     *
     * @param namespaceId
     * @param serviceName
     * @param eventId
     * @param data
     */
    @Override
    public void reportServiceEvent(String namespaceId, String serviceName, String eventId, String data) {
        String key = "event-" + namespaceId + "-" + serviceName + "-" + eventId;
        kvStoreManager.put(key,data);
    }

    /**
     * 获取服务事件数据
     *
     * @param serviceEventModel
     * @return
     */
    @Override
    public PageService<FemasEventData> fetchEventData(ServiceEventModel serviceEventModel) {
        StorageResult<List<String>> result = kvStoreManager.scanPrefixValue("event-" + serviceEventModel.getNamespaceId() + "-" + serviceEventModel.getServiceName() + "-");
        ArrayList<FemasEventData> eventDataList = new ArrayList<>();
        if (!CollectionUtil.isEmpty(result.getData())) {
            result.getData().forEach(s -> {
                List<FemasEventData> femasEvents = null;
                try {
                    femasEvents = objectMapper.readValue(s, new TypeReference<List<FemasEventData>>() {});
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                for (FemasEventData eventData : femasEvents) {
                    if (eventData == null || eventData.getOccurTime() < serviceEventModel.getStartTime() || eventData.getOccurTime() > serviceEventModel.getEndTime()) {
                        continue;
                    }
                    if(!StringUtils.isEmpty(serviceEventModel.getEventType())
                            && !serviceEventModel.getEventType().equalsIgnoreCase(eventData.getEventType().name())){
                        continue;
                    }
                    eventDataList.add(eventData);
                }
            });
        }
        List<FemasEventData> femasEventData = PageUtil.pageList(eventDataList, serviceEventModel.getPageNo(), serviceEventModel.getPageSize(), new Comparator<FemasEventData>() {
            @Override
            public int compare(FemasEventData o1, FemasEventData o2) {
                return (int) (o2.getOccurTime() - o1.getOccurTime());
            }
        });
        return new PageService<>(femasEventData, eventDataList.size());
    }

    /**
     * 获取服务api
     *
     * @param apiModel
     * @return
     */
    @Override
    public PageService<ServiceApi> fetchServiceApiData(ApiModel apiModel) {
        // TODO: get specific version if needed
        StorageResult<List<String>> result = kvStoreManager.scanPrefixValue("api-" + apiModel.getNamespaceId() + "-" + apiModel.getServiceName());
        List<ServiceApi> serviceApis = new ArrayList<>();
        if (!CollectionUtil.isEmpty(result.getData())) {
            for (String apiData: result.getData()) {
                if (StringUtils.isNotEmpty(apiData)) {
                    try {
                        serviceApis.addAll(objectMapper.readValue(apiData, new TypeReference<List<ServiceApi>>() {
                        }));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
            }
            // 过滤
            if (!StringUtils.isEmpty(apiModel.getStatus()) || !StringUtils.isEmpty(apiModel.getServiceVersion())
                    || !StringUtils.isEmpty(apiModel.getKeyword())) {
                ArrayList tmp = new ArrayList<ServiceApi>();
                for (ServiceApi serverApi : serviceApis) {
                    if (!StringUtils.isEmpty(apiModel.getStatus()) && !apiModel.getStatus().equalsIgnoreCase(serverApi.getStatus())) {
                        continue;
                    }
                    if (!StringUtils.isEmpty(apiModel.getServiceVersion()) && !apiModel.getServiceVersion().equalsIgnoreCase(serverApi.getServiceVersion())) {
                        continue;
                    }
                    if(!StringUtils.isEmpty(apiModel.getKeyword()) && !StringUtils.isEmpty(serverApi.getPath())
                        && !serverApi.getPath().contains(apiModel.getKeyword())){
                        continue;
                    }
                    tmp.add(serverApi);
                }
                serviceApis = tmp;
            }
        }
        List<ServiceApi> res = PageUtil.pageList(serviceApis, apiModel.getPageNo(), apiModel.getPageSize());
        PageService<ServiceApi> pageService = new PageService<>();
        pageService.setData(res);
        pageService.setCount(serviceApis.size());
        return pageService;
    }

    /**
     * 配置鉴权规则
     *
     * @param authRule
     * @return
     */
    @Override
    public int configureAuthRule(FemasAuthRule authRule) {
        if (StringUtils.isEmpty(authRule.getRuleId())) {
            authRule.setRuleId("auth-" + iidGeneratorService.nextHashId());
            authRule.setCreateTime(new Date().getTime());
        }
        // 静默处理
        if("1".equalsIgnoreCase(authRule.getIsEnabled())){
            StorageResult<List<String>> result = kvStoreManager.scanPrefix("authority/" + authRule.getNamespaceId() + "/" + authRule.getServiceName() + "/");
            if(result.getData() != null){
                result.getData().stream().forEach(s->{
                    StorageResult<String> stringStorageResult = kvStoreManager.get(s);
                    FemasAuthRule femasAuthRule = JSONSerializer.deserializeStr(FemasAuthRule.class, stringStorageResult.getData());
                    if("1".equalsIgnoreCase(femasAuthRule.getIsEnabled())){
                        femasAuthRule.setIsEnabled("0");
                        kvStoreManager.put(s,JSONSerializer.serializeStr(femasAuthRule));
                    }
                });
            }
        }
        String authKey = "authority/" + authRule.getNamespaceId() + "/" + authRule.getServiceName() + "/" + authRule.getRuleId();
        authRule.setAvailableTime(new Date().getTime());
        StorageResult res = kvStoreManager.put(authKey, JSONSerializer.serializeStr(authRule));
        if (res.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)) {
            return 1;
        }
        return 0;
    }

    /**
     * 查询鉴权规则
     *
     * @param serviceModel
     * @return
     */
    @Override
    public List<FemasAuthRule> fetchAuthRule(ServiceModel serviceModel) {
        String authKey = "authority/" + serviceModel.getNamespaceId() + "/" + serviceModel.getServiceName() + "/";
        StorageResult<List<String>> result = kvStoreManager.scanPrefix(authKey);
        ArrayList<FemasAuthRule> authRules = new ArrayList<>();
        if (result.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)) {
            List<String> strings = result.getData();
            strings.stream().forEach(s -> {
                if(!authKey.equals(s)){
                    StorageResult<String> storageResult = kvStoreManager.get(s);
                    if (storageResult.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)) {
                        authRules.add(JSONSerializer.deserializeStr(FemasAuthRule.class, storageResult.getData()));
                    }
                }
            });
        }
        return authRules;
    }

    /**
     * 删除鉴权规则
     *
     * @param serviceAuthRuleModel
     * @return
     */
    @Override
    public int deleteAuthRule(ServiceAuthRuleModel serviceAuthRuleModel) {
        String cbKey = "authority/" + serviceAuthRuleModel.getNamespaceId() + "/" + serviceAuthRuleModel.getServiceName() + "/" + serviceAuthRuleModel.getRuleId();
        StorageResult result = kvStoreManager.delete(cbKey);
        if(result.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)){
            return 1;
        }
        return 0;
    }

    /**
     * 配置熔断规则
     *
     * @param circuitBreakerRule
     * @return
     */
    @Override
    public Result configureBreakerRule(FemasCircuitBreakerRule circuitBreakerRule) {
        // 限制一个下游服务只能创建一条规则
        if (StringUtils.isEmpty(circuitBreakerRule.getRuleId())) {
            circuitBreakerRule.setRuleId("bk-" + iidGeneratorService.nextHashId());
        }
        String cbKey = "circuitbreaker/" + circuitBreakerRule.getNamespaceId() + "/" + circuitBreakerRule.getServiceName() + "/" + circuitBreakerRule.getRuleId();
        circuitBreakerRule.setUpdateTime(new Date().getTime());
        StorageResult res = kvStoreManager.put(cbKey, JSONSerializer.serializeStr(circuitBreakerRule));
        if(!res.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)){
            return Result.errorMessage("服务熔断规则编辑失败");
        }
        return Result.successMessage("服务熔断规则编辑成功");
    }

    /**
     * 查询熔断规则
     *
     * @param circuitBreakerModel
     * @return
     */
    @Override
    public List<FemasCircuitBreakerRule> fetchBreakerRule(CircuitBreakerModel circuitBreakerModel) {
        String cbKey = "circuitbreaker/" + circuitBreakerModel.getNamespaceId() + "/" + circuitBreakerModel.getServiceName() + "/";
        StorageResult<List<String>> result = kvStoreManager.scanPrefix(cbKey);
        ArrayList<FemasCircuitBreakerRule> breakerRules = new ArrayList<>();
        if (!CollectionUtil.isEmpty(result.getData())) {
            List<String> ids = result.getData();
            for(String id : ids){
                StorageResult<String> storageResult = kvStoreManager.get(id);
                if (storageResult.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)) {
                    FemasCircuitBreakerRule femasCircuitBreakerRule = JSONSerializer.deserializeStr(FemasCircuitBreakerRule.class, storageResult.getData());
                    // 隔离级别过滤
                    if(!StringUtils.isEmpty(circuitBreakerModel.getIsolationLevel()) && !circuitBreakerModel.getIsolationLevel().equalsIgnoreCase(femasCircuitBreakerRule.getIsolationLevel())){
                        continue;
                    }
                    if(!StringUtils.isEmpty(circuitBreakerModel.getSearchWord())
                            && !femasCircuitBreakerRule.getTargetServiceName().contains(circuitBreakerModel.getSearchWord())){
                        continue;
                    }
                    breakerRules.add(femasCircuitBreakerRule);
                }
            }
        }
        return breakerRules;
    }

    /**
     * 删除熔断规则
     *
     * @param ruleModel
     * @return
     */
    @Override
    public int deleteBreakerRule(RuleModel ruleModel) {
        String cbKey = "circuitbreaker/" + ruleModel.getNamespaceId() + "/" + ruleModel.getServiceName() + "/" + ruleModel.getRuleId();
        StorageResult result = kvStoreManager.delete(cbKey);
        if(result.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)){
            return 1;
        }
        return 0;
    }

    /**
     * 配置限流规则
     *
     * @param limitRule
     * @return
     */
    @Override
    public int configureLimitRule(FemasLimitRule limitRule) {
        if (StringUtils.isEmpty(limitRule.getRuleId())) {
            limitRule.setRuleId("lt-" + iidGeneratorService.nextHashId());
        }
        limitRule.setUpdateTime(new Date().getTime());
        String rateLimitKey = "ratelimit/" + limitRule.getNamespaceId() + "/" + limitRule.getServiceName() + "/" + limitRule.getRuleId();
        StorageResult res = kvStoreManager.put(rateLimitKey, JSONSerializer.serializeStr(limitRule));
        if(res.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)){
            return 1;
        }
        return 0;
    }

    /**
     * 查询限流规则
     *
     * @param serviceModel
     * @return
     */
    @Override
    public List<FemasLimitRule> fetchLimitRule(LimitModel serviceModel) {
        String rateLimitKey = "ratelimit/" + serviceModel.getNamespaceId() + "/" + serviceModel.getServiceName() + "/";
        StorageResult<List<String>> storageResult = kvStoreManager.scanPrefix(rateLimitKey);
        ArrayList<FemasLimitRule> limitRules = new ArrayList<>();
        if (!CollectionUtil.isEmpty(storageResult.getData())) {
            List<String> ids = storageResult.getData();
            for(String id : ids){
                StorageResult<String> result = kvStoreManager.get(id);
                if (storageResult.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)) {
                    FemasLimitRule femasLimitRule = JSONSerializer.deserializeStr(FemasLimitRule.class, result.getData());
                    // 限流类型过滤
                    if(!StringUtils.isEmpty(serviceModel.getType()) && !serviceModel.getType().equalsIgnoreCase(femasLimitRule.getType().name())){
                        continue;
                    }
                    if(!StringUtils.isEmpty(serviceModel.getKeyword()) && !femasLimitRule.getRuleName().contains(serviceModel.getKeyword())){
                        continue;
                    }
                    limitRules.add(femasLimitRule);
                }
            }
        }
        return limitRules;
    }

    /**
     * 删除限流规则
     *
     * @param ruleModel
     * @return
     */
    @Override
    public int deleteLimitRule(RuleModel ruleModel) {
        String rateLimitKey = "ratelimit/" + ruleModel.getNamespaceId() + "/" + ruleModel.getServiceName() + "/" + ruleModel.getRuleId();
        StorageResult res = kvStoreManager.delete(rateLimitKey);
        if(res.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)){
            return 1;
        }
        return 0;
    }

    /**
     * 查询路由规则
     *
     * @param serviceModel
     * @return
     */
    @Override
    public List<FemasRouteRule> fetchRouteRule(ServiceModel serviceModel) {
        String routeKey = "route/" + serviceModel.getNamespaceId() + "/" + serviceModel.getServiceName() + "/";
        return getRouteRulesByKey(routeKey);
    }

    /**
     * 删除路由规则
     *
     * @param ruleModel
     * @return
     */
    @Override
    public int deleteRouteRule(RuleModel ruleModel) {
        String routeKey = "route/" + ruleModel.getNamespaceId() + "/" + ruleModel.getServiceName() + "/" + ruleModel.getRuleId();
        StorageResult res = kvStoreManager.delete(routeKey);
        if(res.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)){
            return 1;
        }
        return 0;
    }

    /**
     * 配置路由规则
     *
     * @param routeRule
     * @return
     */
    @Override
    public int configureRouteRule(FemasRouteRule routeRule) {
        if (StringUtils.isEmpty(routeRule.getRuleId())) {
            routeRule.setRuleId("rt-" + iidGeneratorService.nextHashId());
            routeRule.setCreateTime(new Date().getTime());
        }
        // 静默处理
        if(!StringUtils.isEmpty(routeRule.getStatus()) && "1".equalsIgnoreCase(routeRule.getStatus())){
            String routeKey = "route/" + routeRule.getNamespaceId() + "/" + routeRule.getServiceName() + "/";
            StorageResult<List<String>> storageResult = kvStoreManager.scanPrefix(routeKey);
            List<String> strings = storageResult.getData();
            if(!CollectionUtil.isEmpty(strings)){
                for(String s : strings){
                    StorageResult<String> result = kvStoreManager.get(s);
                    FemasRouteRule femasRouteRule = JSONSerializer.deserializeStr(FemasRouteRule.class, result.getData());
                    if(!StringUtils.isEmpty(femasRouteRule.getStatus()) && "1".equalsIgnoreCase(femasRouteRule.getStatus())){ // 已有路由规则开启
                        femasRouteRule.setStatus("0");
                        kvStoreManager.put(s,JSONSerializer.serializeStr(femasRouteRule));
                    }
                }
            }
        }
        routeRule.setUpdateTime(new Date().getTime());
        String routeKey = "route/" + routeRule.getNamespaceId() + "/" + routeRule.getServiceName() + "/" + routeRule.getRuleId();
        StorageResult res = kvStoreManager.put(routeKey, JSONSerializer.serializeStr(routeRule));
        if(res.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)){
            return 1;
        }
        return 0;
    }

    /**
     * 新增操作日志
     *
     * @param record
     * @return
     */
    @Override
    public int configureRecord(Record record) {
        StorageResult res = kvStoreManager.put(AdminConstants.RECORD_LOG.concat(new Date().getTime() + ""), JSONSerializer.serializeStr(record));
        if(res.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)){
            return 1;
        }
        return 0;
    }

    /**
     * 编辑路由容错开关
     *
     * @param tolerate
     * @return
     */
    @Override
    public int configureTolerant(Tolerate tolerate) {
        String key = "tolerant-" + tolerate.getNamespaceId() + "-" + tolerate.getServiceName();
        StorageResult res = kvStoreManager.put(key, JSONSerializer.serializeStr(tolerate));
        if(res.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)){
            return 1;
        }
        return 0;
    }

    /**
     * 查询路由容错开关
     *
     * @param tolerateModel
     */
    @Override
    public boolean fetchTolerant(TolerateModel tolerateModel) {
        String key = "tolerant-" + tolerateModel.getNamespaceId() + "-" + tolerateModel.getServiceName();
        StorageResult<String> stringStorageResult = kvStoreManager.get(key);
        if(stringStorageResult.getData() == null){
            return false;
        }
        String res = JSONSerializer.deserializeStr(Tolerate.class,stringStorageResult.getData()).getIsTolerant();
        return "1".equals(res) ? true : false;
    }

    /**
     * 查询单条鉴权规则
     *
     * @param ruleSearch
     * @return
     */
    @Override
    public FemasAuthRule fetchAuthRuleById(RuleSearch ruleSearch) {
        StorageResult<String> stringStorageResult = kvStoreManager.get("authority/" + ruleSearch.getNamespaceId() + "/" + ruleSearch.getServiceName() + "/" + ruleSearch.getRuleId());
        if(stringStorageResult.getData() == null)
            return null;
        FemasAuthRule res = JSONSerializer.deserializeStr(FemasAuthRule.class, stringStorageResult.getData());
        return res;
    }

    /**
     * 查询单条路由规则
     *
     * @param ruleSearch
     */
    @Override
    public FemasRouteRule fetchRouteRuleById(RuleSearch ruleSearch) {
        StorageResult<String> stringStorageResult = kvStoreManager.get("route/" + ruleSearch.getNamespaceId() + "/" + ruleSearch.getServiceName() + "/" + ruleSearch.getRuleId());
        if(stringStorageResult.getData() == null)
            return null;
        FemasRouteRule res = JSONSerializer.deserializeStr(FemasRouteRule.class, stringStorageResult.getData());
        return res;
    }

    /**
     * 查询单条熔断规则
     *
     * @param ruleSearch
     * @return
     */
    @Override
    public FemasCircuitBreakerRule fetchBreakerRuleById(RuleSearch ruleSearch) {
        StorageResult<String> stringStorageResult = kvStoreManager.get("circuitbreaker/" + ruleSearch.getNamespaceId() + "/" + ruleSearch.getServiceName() + "/" + ruleSearch.getRuleId());
        if(stringStorageResult.getData() == null)
            return null;
        FemasCircuitBreakerRule res = JSONSerializer.deserializeStr(FemasCircuitBreakerRule.class, stringStorageResult.getData());
        return res;
    }

    /**
     * 查询单条限流规则
     *
     * @param ruleSearch
     * @return
     */
    @Override
    public FemasLimitRule fetchLimitRuleById(RuleSearch ruleSearch) {
        StorageResult<String> stringStorageResult = kvStoreManager.get("ratelimit/" + ruleSearch.getNamespaceId() + "/" + ruleSearch.getServiceName() + "/" + ruleSearch.getRuleId());
        if(stringStorageResult.getData() == null)
            return null;
        FemasLimitRule res = JSONSerializer.deserializeStr(FemasLimitRule.class, stringStorageResult.getData());
        return res;
    }

    /**
     * 查询操作日志
     *
     * @param logModel
     * @return
     */
    @Override
    public PageService<Record> fetchLogs(LogModel logModel) {
        StorageResult<List<String>> listStorageResult = kvStoreManager.scanPrefix(AdminConstants.RECORD_LOG);
        List<String> data = listStorageResult.getData();
        ArrayList<Record> res = new ArrayList<>();
        data.stream().forEach(s -> {
            Record record = JSONSerializer.deserializeStr(Record.class, kvStoreManager.get(s).getData());
            if(record.getTime() <= logModel.getEndTime() && record.getTime() >= logModel.getStartTime()){
                if(StringUtils.isEmpty(logModel.getModule()) || LogModuleEnum.valueOf(logModel.getModule()).getName().equals(record.getModule())){
                    res.add(record);
                }
            }
        });
        PageService<Record> pageService = new PageService<>();
        pageService.setCount(res.size());
        pageService.setData(PageUtil.pageList(res, logModel.getPageNo(), logModel.getPageSize(), new Comparator<Record>() {
            @Override
            public int compare(Record o1, Record o2) {
                return (int)(o2.getTime() - o1.getTime());
            }
        }));
        return pageService;
    }

    /**
     * 查询命名空间下的路由规则
     *
     * @param namespaceId
     * @return
     */
    @Override
    public List<FemasRouteRule> fetchRouteRuleByNamespaceId(String namespaceId) {
        String key = "route/" + namespaceId + "/";
        return getRouteRulesByKey(key);
    }

    private List<FemasRouteRule> getRouteRulesByKey(String key) {
        StorageResult<List<String>> storageResult = kvStoreManager.scanPrefix(key);
        ArrayList<FemasRouteRule> routeRules = new ArrayList<>();
        if (storageResult.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)) {
            List<String> strings = storageResult.getData();
            strings.stream().forEach(s -> {
                StorageResult<String> result = kvStoreManager.get(s);
                if (storageResult.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)) {
                    routeRules.add(JSONSerializer.deserializeStr(FemasRouteRule.class, result.getData()));
                }
            });
        }
        return routeRules;
    }

    /**
     * 拉取配置表的值
     *
     * @param key
     * @return
     */
    @Override
    public String fetchConfig(String key) {
        StorageResult<String> res = kvStoreManager.get("config-" + key);
        return StringUtils.isEmpty(res.getData()) ? null : res.getData();
    }

    /**
     * 修改配置表
     *
     * @param key
     * @param value
     * @return
     */
    @Override
    public int configureConfig(String key, String value) {
        StorageResult res = kvStoreManager.put("config-" + key, value);
        if(res.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)){
            return 1;
        }
        return 0;
    }

    /**  dcfg start **/
    @Override
    public int configureDcfg(Config config) {
        if (StringUtils.isEmpty(config.getConfigId())) {
            config.setConfigId(AdminConstants.DCFG_ID_PREFIX.concat(iidGeneratorService.nextHashId()));
        }
        String key = "dcfg/" + config.getNamespaceId() + "/" + config.getConfigId();
        StorageResult res = kvStoreManager.put(key, JSONSerializer.serializeStr(config));
        if (res.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)) {
            return 1;
        }
        return 0;
    }

    @Override
    public PageService<Config> fetchDcfgs(ConfigRequest request) {
        String key;
        if (StringUtils.isEmpty(request.getConfigId())) {
            key = "dcfg/" + request.getNamespaceId() + "/";
        } else {
            key = "dcfg/" + request.getNamespaceId() + "/" + request.getConfigId();
        }
        StorageResult<List<String>> result = kvStoreManager.scanPrefix(key);
        ArrayList<Config> list = new ArrayList<>();
        if (result.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)) {
            List<String> strings = result.getData();
            strings.stream().forEach(s -> {
                StorageResult<String> storageResult = kvStoreManager.get(s);
                if (storageResult.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)) {
                    Config config = JSONSerializer.deserializeStr(Config.class, storageResult.getData());
                    if (StringUtils.isNotEmpty(request.getSearchWord())) {
                        if (!config.getConfigId().contains(request.getSearchWord()) && !config.getConfigName().contains(request.getSearchWord())) {
                            return;
                        }
                    }
                    list.add(config);
                }
            });
        }
        List<Config> data = PageUtil.pageList(list, request.getPageNo(), request.getPageSize(), (o1, o2) -> (int) (o2.getCreateTime() - o1.getCreateTime()));
        return new PageService(data, list.size());
    }

    @Override
    public int deleteDcfg(String configId, String namespaceId) {
        String key = "dcfg/" + namespaceId + "/" + configId;
        StorageResult result = kvStoreManager.delete(key);
        if(result.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)){
            return 1;
        }
        return 0;
    }

    @Override
    public int deleteDcfgVersion(String configVersionId, String configId) {
        String key = "dcfg/version/" + configId + "/" + configVersionId;
        StorageResult result = kvStoreManager.delete(key);
        if(result.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)){
            return 1;
        }
        return 0;
    }

    @Override
    public List<Config> fetchDcfgOther(List<String> configIdList) {
        List<Config> list = new ArrayList<>();
        for (String configId : configIdList) {
            Config config = new Config();
            config.setConfigId(configId);
            ConfigRequest request = new ConfigRequest();
            request.setConfigId(configId);
            PageService<ConfigVersion> page = fetchDcfgVersions(request);
            config.setVersionCount(page.getCount());
            list.add(config);
        }
        return list;
    }

    @Override
    public int configureDcfgVersion(ConfigVersion configVersion) {
        if (StringUtils.isEmpty(configVersion.getConfigVersionId())) {
            configVersion.setConfigVersionId(AdminConstants.DCFGV_ID_PREFIX.concat(iidGeneratorService.nextHashId()));
        }
        String key = "dcfg/version/" + configVersion.getConfigId() + "/" + configVersion.getConfigVersionId();
        StorageResult res = kvStoreManager.put(key, JSONSerializer.serializeStr(configVersion));
        if (res.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)) {
            return 1;
        }
        return 0;
    }

    @Override
    public PageService<ConfigVersion> fetchDcfgVersions(ConfigRequest request) {
        String key;
        if (StringUtils.isEmpty(request.getConfigVersionId())) {
            key = "dcfg/version/" + request.getConfigId() + "/";
        } else {
            key = "dcfg/version/" + request.getConfigId() + "/" + request.getConfigVersionId();
        }
        StorageResult<List<String>> result = kvStoreManager.scanPrefix(key);
        ArrayList<ConfigVersion> list = new ArrayList<>();
        if (result.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)) {
            List<String> strings = result.getData();
            strings.stream().forEach(s -> {
                StorageResult<String> storageResult = kvStoreManager.get(s);
                if (storageResult.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)) {
                    ConfigVersion configVersion = JSONSerializer.deserializeStr(ConfigVersion.class, storageResult.getData());
                    if (StringUtils.isNotEmpty(request.getSearchWord())) {
                        if (!configVersion.getConfigVersionId().contains(request.getSearchWord())
                                && !(""+configVersion.getConfigVersion()).contains(request.getSearchWord())
                                && !configVersion.getConfigValue().contains(request.getSearchWord())) {
                            return;
                        }
                    }
                    if (StringUtils.isNotEmpty(request.getReleaseStatus())) {
                        if (!request.getReleaseStatus().equals(configVersion.getReleaseStatus())){
                            return;
                        }
                    }
                    list.add(configVersion);
                }
            });
        }
        List<ConfigVersion> data = PageUtil.pageList(list, request.getPageNo(), request.getPageSize(), (o1, o2) -> {
            int score = 0;
            if ("configVersion".equalsIgnoreCase(request.getOrderBy())) {
                score = o2.getConfigVersion() - o1.getConfigVersion();
            } else if ("releaseTime".equalsIgnoreCase(request.getOrderBy())) {
                score = (int) (o2.getReleaseTime() - o1.getReleaseTime());
            } else {
                score = (int) (o2.getCreateTime() - o1.getCreateTime());
            }
            return request.getOrderType() == 0 ? score : -score;
        });
        return new PageService(data,list.size());
    }

    @Override
    public int configureDcfgReleaseLog(ConfigReleaseLog configReleaseLog) {
        configReleaseLog.setId(System.currentTimeMillis());
        String key = "dcfg/releaseLog/" + configReleaseLog.getConfigId() + "/" + configReleaseLog.getId();
        StorageResult res = kvStoreManager.put(key, JSONSerializer.serializeStr(configReleaseLog));
        if (res.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)) {
            return 1;
        }
        return 0;
    }

    /** dcfg end **/


    /**
     * 查询鉴权规则
     *
     * @param authRuleModel
     * @return
     */
    @Override
    public PageService<FemasAuthRule> fetchAuthRulePages(AuthRuleModel authRuleModel) {
        List<FemasAuthRule> authRule = fetchAuthRule(authRuleModel);
        // 过滤
        if(!CollectionUtil.isEmpty(authRule) && !StringUtils.isEmpty(authRuleModel.getKeyword())){
            ArrayList<FemasAuthRule> tempAuthRule = new ArrayList<>();
            for (FemasAuthRule rule : authRule){
                if(rule.getRuleName().contains(authRuleModel.getKeyword())){
                    tempAuthRule.add(rule);
                }
            }
            authRule = tempAuthRule;
        }
        Comparator<FemasAuthRule> comparator = new Comparator<FemasAuthRule>() {
            @Override
            public int compare(FemasAuthRule o1, FemasAuthRule o2) {
                return (int)(o2.getAvailableTime() - o1.getAvailableTime());
            }
        };
        List<FemasAuthRule> data = PageUtil.pageList(authRule, authRuleModel.getPageNo(), authRuleModel.getPageSize(),comparator);
        return new PageService<FemasAuthRule>(data,authRule.size());
    }

    /**
     * 查询熔断规则
     *
     * @param circuitBreakerModel
     * @return
     */
    @Override
    public PageService<FemasCircuitBreakerRule> fetchBreakerRulePages(CircuitBreakerModel circuitBreakerModel) {
        List<FemasCircuitBreakerRule> circuitBreakerRules = fetchBreakerRule(circuitBreakerModel);
        Comparator<FemasCircuitBreakerRule> comparator = new Comparator<FemasCircuitBreakerRule>() {
            @Override
            public int compare(FemasCircuitBreakerRule o1, FemasCircuitBreakerRule o2) {
                return (int) (o2.getUpdateTime() - o1.getUpdateTime());
            }
        };
        List<FemasCircuitBreakerRule> data = PageUtil.pageList(circuitBreakerRules, circuitBreakerModel.getPageNo(), circuitBreakerModel.getPageSize(), comparator);
        return new PageService<FemasCircuitBreakerRule>(data,circuitBreakerRules.size());
    }

    /**
     * 查询限流规则
     *
     * @param serviceModel
     * @return
     */
    @Override
    public PageService<FemasLimitRule> fetchLimitRulePages(LimitModel serviceModel) {
        List<FemasLimitRule> limitRules = fetchLimitRule(serviceModel);
        Comparator<FemasLimitRule> comparator = new Comparator<FemasLimitRule>() {
            @Override
            public int compare(FemasLimitRule o1, FemasLimitRule o2) {
                return (int) (o2.getUpdateTime() - o1.getUpdateTime());
            }
        };
        List<FemasLimitRule> data = PageUtil.pageList(limitRules, serviceModel.getPageNo(), serviceModel.getPageSize(), comparator);
        return new PageService<FemasLimitRule>(data,limitRules.size());
    }

    /**
     * 查询路由规则
     *
     * @param serviceModel
     * @return
     */
    @Override
    public PageService<FemasRouteRule> fetchRouteRulePages(ServiceModel serviceModel) {
        List<FemasRouteRule> routeRules = fetchRouteRule(serviceModel);
        Comparator<FemasRouteRule> comparator = new Comparator<FemasRouteRule>() {
            @Override
            public int compare(FemasRouteRule o1, FemasRouteRule o2) {
                return (int) (o2.getUpdateTime() - o1.getUpdateTime());
            }
        };
        List<FemasRouteRule> data = PageUtil.pageList(routeRules, serviceModel.getPageNo(), serviceModel.getPageSize(), comparator);
        return new PageService<FemasRouteRule>(data,routeRules.size());
    }

    @Override
    public Integer configureLane(LaneInfo laneInfo) {
        if (StringUtils.isEmpty(laneInfo.getLaneId())) {
            laneInfo.setLaneId("lane-" + iidGeneratorService.nextHashId());
            laneInfo.setCreateTime(new Date().getTime());
        }
        laneInfo.setUpdateTime(new Date().getTime());
        String routeKey = "lane-info/" + laneInfo.getLaneId();
        StorageResult res = kvStoreManager.put(routeKey, JSONSerializer.serializeStr(laneInfo));
        if(res.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)){
            return 1;
        }
        return 0;
    }

    @Override
    public LaneInfo fetchLaneById(String laneId) {
        StorageResult<String> stringStorageResult = kvStoreManager.get("lane-info/" + laneId);
        if(stringStorageResult.getData() == null)
            return null;
        LaneInfo res = JSONSerializer.deserializeStr(LaneInfo.class, stringStorageResult.getData());
        return res;
    }

    @Override
    public PageService<LaneInfo> fetchLaneInfoPages(LaneInfoModel laneInfoModel) {
        List<LaneInfo> laneInfoTemp = fetchLaneInfo();
        List<LaneInfo> laneInfos = new ArrayList<>();
        // 条件过滤
        for (LaneInfo laneInfo : laneInfoTemp){
            if(!StringUtils.isEmpty(laneInfoModel.getLaneId()) && !laneInfo.getLaneId().contains(laneInfoModel.getLaneId())){
                continue;
            }
            if(!StringUtils.isEmpty(laneInfoModel.getLaneName()) && !laneInfo.getLaneName().contains(laneInfoModel.getLaneName())){
                continue;
            }
            if(!StringUtils.isEmpty(laneInfoModel.getRemark()) && !laneInfo.getRemark().contains(laneInfoModel.getRemark())){
                continue;
            }
            laneInfos.add(laneInfo);
        }
        Comparator<LaneInfo> comparator = new Comparator<LaneInfo>() {
            @Override
            public int compare(LaneInfo o1, LaneInfo o2) {
                return (int) (o2.getUpdateTime() - o1.getUpdateTime());
            }
        };
        List<LaneInfo> data = PageUtil.pageList(laneInfos, laneInfoModel.getPageNo(), laneInfoModel.getPageSize(), comparator);
        return new PageService<LaneInfo>(data,laneInfos.size());
    }

    @Override
    public Integer deleteLane(String laneId) {
        String routeKey = "lane-info/" + laneId;
        StorageResult res = kvStoreManager.delete(routeKey);
        if(res.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)){
            return 1;
        }
        return 0;
    }

    @Override
    public Integer configureLaneRule(LaneRule laneRule) {
        if (StringUtils.isEmpty(laneRule.getRuleId())) {
            laneRule.setRuleId("lane-rule-" + iidGeneratorService.nextHashId());
            laneRule.setCreateTime(new Date().getTime());
        }
        laneRule.setUpdateTime(new Date().getTime());
        String routeKey = "lane-rule/" + laneRule.getRuleId();
        StorageResult res = kvStoreManager.put(routeKey, JSONSerializer.serializeStr(laneRule));
        if(res.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)){
            return 1;
        }
        return 0;
    }

    @Override
    public LaneRule fetchLaneRuleById(String laneRuleId) {
        StorageResult<String> stringStorageResult = kvStoreManager.get("lane-rule/" + laneRuleId);
        if(stringStorageResult.getData() == null)
            return null;
        LaneRule res = JSONSerializer.deserializeStr(LaneRule.class, stringStorageResult.getData());
        return res;
    }

    @Override
    public PageService<LaneRule> fetchLaneRulePages(LaneRuleModel laneRuleModel) {
        List<LaneRule> laneRulesTemp = fetchLaneRule();
        List<LaneRule> laneRules = new ArrayList<>();
        // 条件过滤
        for (LaneRule laneRule : laneRulesTemp){
            if(!StringUtils.isEmpty(laneRuleModel.getRuleId()) && !laneRule.getRuleId().contains(laneRuleModel.getRuleId())){
                continue;
            }
            if(!StringUtils.isEmpty(laneRuleModel.getRuleName()) && !laneRule.getRuleName().contains(laneRuleModel.getRuleName())){
                continue;
            }
            if(!StringUtils.isEmpty(laneRuleModel.getRemark()) && !laneRule.getRemark().contains(laneRuleModel.getRemark())){
                continue;
            }
            laneRules.add(laneRule);
        }
        Comparator<LaneRule> comparator = new Comparator<LaneRule>() {
            @Override
            public int compare(LaneRule o1, LaneRule o2) {
                return (int) (o2.getUpdateTime() - o1.getUpdateTime());
            }
        };
        List<LaneRule> data = PageUtil.pageList(laneRules, laneRuleModel.getPageNo(), laneRuleModel.getPageSize(), comparator);
        return new PageService<LaneRule>(data,laneRules.size());
    }

    @Override
    public Integer deleteLaneRule(String laneRuleId) {
        String routeKey = "lane-rule/" + laneRuleId;
        StorageResult res = kvStoreManager.delete(routeKey);
        if(res.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)){
            return 1;
        }
        return 0;
    }

    @Override
    public List<LaneInfo> fetchLaneInfo() {
        String laneKey = "lane-info/";
        StorageResult<List<String>> storageResult = kvStoreManager.scanPrefix(laneKey);
        ArrayList<LaneInfo> laneInfos = new ArrayList<>();
        if (!CollectionUtil.isEmpty(storageResult.getData())) {
            List<String> ids = storageResult.getData();
            for(String id : ids){
                StorageResult<String> result = kvStoreManager.get(id);
                LaneInfo laneInfo = JSONSerializer.deserializeStr(LaneInfo.class, result.getData());
                laneInfos.add(laneInfo);
            }
        }
        return laneInfos;
    }

    @Override
    public List<LaneRule> fetchLaneRule() {
        String laneRuleKey = "lane-rule/";
        StorageResult<List<String>> storageResult = kvStoreManager.scanPrefix(laneRuleKey);
        ArrayList<LaneRule> laneRules = new ArrayList<>();
        if (!CollectionUtil.isEmpty(storageResult.getData())) {
            List<String> ids = storageResult.getData();
            for(String id : ids){
                StorageResult<String> result = kvStoreManager.get(id);
                LaneRule laneRule = JSONSerializer.deserializeStr(LaneRule.class, result.getData());
                laneRules.add(laneRule);
            }
        }
        return laneRules;
    }

    public static enum OptionType{
        ADD,
        UPDATE,
        DELETE,
        SELECT,
    }
}
