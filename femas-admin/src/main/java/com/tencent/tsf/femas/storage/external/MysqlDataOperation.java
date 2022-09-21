package com.tencent.tsf.femas.storage.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.common.base.CaseFormat;
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
import com.tencent.tsf.femas.entity.namespace.NamespaceIdModel;
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
import com.tencent.tsf.femas.entity.rule.route.RouteTag;
import com.tencent.tsf.femas.entity.rule.route.Tolerate;
import com.tencent.tsf.femas.entity.rule.route.TolerateModel;
import com.tencent.tsf.femas.entity.service.ServiceEventModel;
import com.tencent.tsf.femas.enums.LogModuleEnum;
import com.tencent.tsf.femas.service.IIDGeneratorService;
import com.tencent.tsf.femas.service.registry.OpenApiFactory;
import com.tencent.tsf.femas.service.registry.RegistryOpenApiInterface;
import com.tencent.tsf.femas.storage.DataOperation;
import com.tencent.tsf.femas.storage.config.MysqlDbConditional;
import com.tencent.tsf.femas.util.ResultCheck;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.tencent.tsf.femas.constant.AdminConstants.*;
import static com.tencent.tsf.femas.service.namespace.NamespaceMangerService.DEFAULT_DESC;
import static com.tencent.tsf.femas.service.namespace.NamespaceMangerService.DEFAULT_NAME;
import static com.tencent.tsf.femas.storage.external.MysqlDbManager.OrderedType.ASC;
import static com.tencent.tsf.femas.storage.external.MysqlDbManager.OrderedType.DESC;
import static com.tencent.tsf.femas.storage.external.RowMapperFactory.MapperType.*;


/**
 * @author Cody
 * @date 2021 2021/7/28 2:23 下午
 */
@Component
@Conditional(MysqlDbConditional.class)
public class MysqlDataOperation implements DataOperation {

    private static final Logger log = LoggerFactory.getLogger(MysqlDataOperation.class);

    @Autowired
    private MysqlDbManager manager;

    @Autowired
    private  IIDGeneratorService iidGeneratorService;

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
        int res = 0;
        if(StringUtils.isEmpty(registryConfig.getRegistryId())){
            registryConfig.setRegistryId(REGISTRY_ID_PREFIX.concat(iidGeneratorService.nextHashId()));
            res = manager.update("insert into registry_config(registry_id,registry_cluster,registry_name,registry_type,user_name,password) values(?,?,?,?,?,?)",
                        registryConfig.getRegistryId(),
                        registryConfig.getRegistryCluster(),
                        registryConfig.getRegistryName(),
                        registryConfig.getRegistryType(),
                        registryConfig.getUsername(),
                        registryConfig.getPassword()
            );
        }else{
            res = manager.update("update registry_config set registry_cluster=?,registry_name=? where registry_id=?",
                        registryConfig.getRegistryCluster(),
                        registryConfig.getRegistryName(),
                        registryConfig.getRegistryId());
        }
        return res;
    }

    /**
     * 查询注册中心
     *
     * @param registrySearch
     * @return
     */
    @Override
    public List<RegistryConfig> fetchRegistryConfigs(RegistrySearch registrySearch) {
        if(registrySearch != null && !StringUtils.isEmpty(registrySearch.getRegistryType())){
            return manager.selectListPojo("select * from registry_config where registry_type=?",
                    RegistryConfig.class, registrySearch.getRegistryType());
        }else{
            return manager.selectListPojo("select * from registry_config", RegistryConfig.class);
        }
    }

    /**
     * 删除注册中心
     *
     * @param registryId
     * @return
     */
    @Override
    public int deleteRegistry(String registryId) {
        int res = manager.deleteById("registry_config", "registry_id", registryId);
        NamespacePageModel namespacePageModel = new NamespacePageModel();
        namespacePageModel.setRegistryId(registryId);
        PageService<Namespace> namespacePageService = fetchNamespaces(namespacePageModel);
        if(namespacePageService != null && !CollectionUtil.isEmpty(namespacePageService.getData())){
            for (Namespace ns : namespacePageService.getData()) {
                ArrayList<String> registryIds = new ArrayList<>();
                for (String id : ns.getRegistryId()) {
                    if(!id.equals(registryId)){
                        registryIds.add(id);
                    }
                }
                ns.setRegistryId(registryIds);
                modifyNamespace(ns);
            }
        }
        return res;
    }

    /**
     * 查询注册中心
     *
     * @param registryId
     * @return
     */
    @Override
    public RegistryConfig fetchRegistryById(String registryId) {
        return manager.selectPojo("select * from registry_config where registry_id = ?",RegistryConfig.class, registryId);
    }

    /**
     * 获取命名空间
     * @param namespaceId
     * @return
     */
    public Namespace fetchNamespaceById(String namespaceId){
        Namespace namespace = (Namespace) manager.selectPojoByMapper(RowMapperFactory.getMapper(NAMESPACE),"select * from namespace where namespace_id = ?", namespaceId);
        return namespace;
    }

    /**
     * 修改命名空间
     * @param namespace
     * @return
     */
    public int modifyNamespace(Namespace namespace){
        int res = 0;
        if(StringUtils.isEmpty(namespace.getNamespaceId())){
            return 0;
        }else{
            Namespace exitsNamespace = fetchNamespaceById(namespace.getNamespaceId());
            if (exitsNamespace == null) {
                return 0;
            }
        }
        res = manager.update("update namespace set namespace.name=?,namespace.desc=?,registry_id=? where namespace_id=?",
                namespace.getName(), namespace.getDesc(), JSONSerializer.serializeStr(namespace.getRegistryId()), namespace.getNamespaceId());

        if(!CollectionUtil.isEmpty(namespace.getRegistryId())){
            RegistryConfig config = fetchRegistryById(namespace.getRegistryId().get(0));
            RegistryOpenApiInterface registryOpenApiInterface = factory.select(config.getRegistryType());
            registryOpenApiInterface.modifyNamespace(config, namespace);
        }
        return res;
    }

    /**
     * 创建命名空间
     * @param namespace
     * @return
     */
    public int createNamespace(Namespace namespace){
        int res = 0;
        if(StringUtils.isEmpty(namespace.getNamespaceId())){
            namespace.setNamespaceId(AdminConstants.NAMESPACE_ID_PREFIX.concat(iidGeneratorService.nextHashId()));
        }else{
            Namespace exitsNamespace = fetchNamespaceById(namespace.getNamespaceId());
            if (exitsNamespace != null) {
                return 0;
            }
        }
        res = manager.update("insert into namespace(namespace_id,registry_id,namespace.name,namespace.desc) values(?,?,?,?)",
                namespace.getNamespaceId(), JSONSerializer.serializeStr(namespace.getRegistryId()), namespace.getName(), namespace.getDesc());

        if(!CollectionUtil.isEmpty(namespace.getRegistryId())){
            RegistryConfig config = fetchRegistryById(namespace.getRegistryId().get(0));
            RegistryOpenApiInterface registryOpenApiInterface = factory.select(config.getRegistryType());
            registryOpenApiInterface.createNamespace(config, namespace);
        }
        return res;
    }

    /**
     * 删除命名空间
     *
     * @param namespaceId
     * @return
     */
    @Override
    public int deleteNamespaceById(String namespaceId) {
        Namespace namespace = fetchNamespaceById(namespaceId);
        if (namespace != null && !CollectionUtil.isEmpty(namespace.getRegistryId())) {
            RegistryConfig config = fetchRegistryById(namespace.getRegistryId().get(0));
            RegistryOpenApiInterface registryOpenApiInterface = factory.select(config.getRegistryType());
            registryOpenApiInterface.deleteNamespace(config, namespace);
        }
        return manager.deleteById("namespace", "namespace_id", namespaceId);
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
        List<RegistryConfig> registryConfigs = fetchRegistryConfigs(null);
        if(CollectionUtil.isEmpty(registryConfigs)){
            return;
        }
        if(fetchNamespaceById(namespaceId) != null){
            return;
        }
        String[] addresses = registryAddress.split(",");
        for(RegistryConfig config : registryConfigs){
            String registryCluster = config.getRegistryCluster();
            if (StringUtils.isBlank(registryCluster)) {
                continue;
            }
            // 对 localhost 进行转换
            if (registryCluster.contains(AdminConstants.LOCALHOST_STRING)) {
                registryCluster = registryCluster.replace(AdminConstants.LOCALHOST_STRING, AdminConstants.LOCALHOST_IP);
            }

            //获取注册中心信息
            RegistryOpenApiInterface registryOpenApiInterface = factory.select(config.getRegistryType());
            List<Namespace> namespaces = registryOpenApiInterface.allNamespaces(config);
            Namespace remoteNamespace = namespaces.stream().filter(namespace -> namespace.getNamespaceId().equals(namespaceId)).findFirst().orElse(null);
            for(String address : addresses){
                // 对 localhost 进行转换
                if (address.contains(AdminConstants.LOCALHOST_STRING)) {
                    address = address.replace(AdminConstants.LOCALHOST_STRING, AdminConstants.LOCALHOST_IP);
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
        String sql = "select * from namespace ";
        boolean flag = false;
        if(!StringUtils.isEmpty(namespaceModel.getRegistryId())){
            flag = true;
            // registry_id使用list存储，故用模糊匹配
            sql += "where registry_id like  '%" +namespaceModel.getRegistryId() + "%'";
        }
        if(!StringUtils.isEmpty(namespaceModel.getName())){
            if(flag){
                sql += " and (namespace.name like '%" + namespaceModel.getName() + "%' or namespace_id like '%"
                        + namespaceModel.getName() + "%')";
            }else{
                sql += " where namespace.name like '%" + namespaceModel.getName() + "%' or namespace_id like '%"
                        + namespaceModel.getName() + "%'";
            }
        }
        return manager.selectByPages(RowMapperFactory.getMapper(NAMESPACE), sql, namespaceModel.getPageNo(), namespaceModel.getPageSize());
    }

    /**
     * 获取注册中心下的命名空间数量
     *
     * @param registryId
     * @return
     */
    @Override
    public int getNamespacesCountByRegistry(String registryId) {
        return manager.selectInteger("select count(1) from namespace where registry_id like '%"+registryId+"%'");
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
        List<ServiceApi> serviceApis = null;
        try {
            serviceApis = objectMapper.readValue(data, new TypeReference<List<ServiceApi>>() {
            });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        int existsCount = manager.selectInteger("select count(1) from service_api where namespace_id=? and service_name=? and service_version=?",
                namespaceId, serviceName, groupId);
        if( existsCount != 0){
            manager.update("delete from service_api where namespace_id=? and service_name=? and service_version=?",
                    namespaceId, serviceName, groupId);
        }
        List<Object[]> params = new ArrayList<>();
        for (ServiceApi serviceApi : serviceApis){
            Object[] param = new String[6];
            param[0] = namespaceId;
            param[1] = serviceName;
            param[2] = serviceApi.getPath();
            param[3] = serviceApi.getStatus();
            param[4] = serviceApi.getServiceVersion();
            param[5] = serviceApi.getMethod();
            params.add(param);
        }
        manager.batchInsert("insert into service_api(namespace_id,service_name,path,status,service_version,method) values(?,?,?,?,?,?)", params);
    }

    @Override
    public void deleteOfflinceServiceApi(String namespaceId, String serviceName, HashSet<String> onlineVersions) {
        List<String> existVersions = manager.selectListPojoByMapper(
                new RowMapper<String>() {
                    @Override
                    public String mapRow(ResultSet rs, int i) throws SQLException {
                        return rs.getString("service_version");
                    }
                },
            "select service_version from service_api where namespace_id=? and service_name=? group by service_version", namespaceId, serviceName);

        for (String existVersion: existVersions) {
            // 如果不存在了，则删除
            if (!onlineVersions.contains(existVersion)) {
                log.info("delete api, ns:{}, service:{}, version:{}", namespaceId, serviceName, existVersion);
                if (existVersion == null) {
                    manager.update("delete from service_api where namespace_id=? and service_name=? and service_version is null",
                            namespaceId, serviceName);
                } else {
                    manager.update("delete from service_api where namespace_id=? and service_name=? and service_version=?",
                            namespaceId, serviceName, existVersion);
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
        List<FemasEventData> femasEvents = null;
        try{
            femasEvents = objectMapper.readValue(data, new TypeReference<List<FemasEventData>>(){});
        } catch (JsonProcessingException e){
            e.printStackTrace();
        }
        if(CollectionUtil.isEmpty(femasEvents)){
            return;
        }
        ArrayList<Object[]> params = new ArrayList<>();
        for(FemasEventData eventData : femasEvents){
            Object[] param = new String[]{namespaceId, serviceName, eventData.getEventType().name(), eventData.getOccurTime()+"",
                    eventData.getUpstream(), eventData.getDownstream(), eventData.getInstanceId(), JSONSerializer.serializeStr(eventData.getAdditionalMsg())};
            params.add(param);
        }

        manager.batchInsert("insert into service_event(namespace_id,service_name,event_type,occur_time," +
                        "upstream,downstream,instance_id,additional_msg) values(?,?,?,?,?,?,?,?)",
                params);
    }

    /**
     * 获取服务事件数据
     *
     * @param serviceEventModel
     * @return
     */
    @Override
    public PageService<FemasEventData> fetchEventData(ServiceEventModel serviceEventModel) {
        String baseSql = "select * from service_event where namespace_id=? and service_name=? and occur_time<=? and occur_time>=? ";
        ArrayList<Object> params = new ArrayList<>();
        params.add(serviceEventModel.getNamespaceId());
        params.add(serviceEventModel.getServiceName());
        params.add(serviceEventModel.getEndTime());
        params.add(serviceEventModel.getStartTime());
        if(!StringUtils.isEmpty(serviceEventModel.getEventType())){
            baseSql += "and event_type=?";
            params.add(serviceEventModel.getEventType());
        }
        return manager.selectByPagesOrdered(RowMapperFactory.getMapper(SERVICE_EVENT), baseSql,
                serviceEventModel.getPageNo(), serviceEventModel.getPageSize(),
                DESC, "occur_time", params.toArray());
    }

    /**
     * 获取服务api
     *
     * @param apiModel
     * @return
     */
    @Override
    public PageService<ServiceApi> fetchServiceApiData(ApiModel apiModel) {
        String baseSql = "select * from service_api where namespace_id=? and service_name=? ";
        ArrayList<String> params = new ArrayList<>();
        params.add(apiModel.getNamespaceId());
        params.add(apiModel.getServiceName());
        if(!StringUtils.isEmpty(apiModel.getServiceVersion())){
            baseSql += "and service_version=? ";
            params.add(apiModel.getServiceVersion());
        }
        if(!StringUtils.isEmpty(apiModel.getStatus())){
            baseSql += "and status=? ";
            params.add(apiModel.getStatus());
        }
        if(!StringUtils.isEmpty(apiModel.getKeyword())){
            baseSql += "and path like '%" + apiModel.getKeyword() + "%' ";
        }
        return manager.selectByPages(new BeanPropertyRowMapper<>(ServiceApi.class), baseSql,
                apiModel.getPageNo(), apiModel.getPageSize(),
                params.toArray());
    }

    /**
     * 配置鉴权规则
     *
     * @param authRule
     * @return
     */
    @Override
    public int configureAuthRule(FemasAuthRule authRule) {
        // 静默处理
        if("1".equalsIgnoreCase(authRule.getIsEnabled())){
            manager.update("update auth_rule set is_enable='0'");
        }
        int res = 0;
        if (StringUtils.isEmpty(authRule.getRuleId())) {
            authRule.setRuleId("auth-" + iidGeneratorService.nextHashId());
            authRule.setCreateTime(new Date().getTime());
            authRule.setAvailableTime(new Date().getTime());
            res = manager.update("insert into auth_rule(rule_id,rule_name,is_enable,rule_type,create_time," +
                    "available_time,service_name,namespace_id,tags,tag_program,target,auth_rule.desc) values(?,?,?,?,?,?,?,?,?,?,?,?)",
                    authRule.getRuleId(), authRule.getRuleName(),
                    authRule.getIsEnabled(), authRule.getRuleType().name(),
                    authRule.getCreateTime(), authRule.getAvailableTime(),
                    authRule.getServiceName(), authRule.getNamespaceId(),
                    JSONSerializer.serializeStr(authRule.getTags()), authRule.getTagProgram(),
                    authRule.getTarget(), authRule.getDesc());
        }else{
            res = manager.update("update auth_rule set rule_name=?, is_enable=?, rule_type=?, " +
                    "available_time=?, tags=?, tag_program=?, target=?, auth_rule.desc=? " +
                    "where rule_id=?",
                    authRule.getRuleName(), authRule.getIsEnabled(),
                    authRule.getRuleType().name(), new Date().getTime(),
                    JSONSerializer.serializeStr(authRule.getTags()), authRule.getTagProgram(),
                    authRule.getTarget(), authRule.getDesc(),
                    authRule.getRuleId());
        }
        return res;
    }

    /**
     * 查询鉴权规则
     *
     * @param serviceModel
     * @return
     */
    @Override
    public List<FemasAuthRule> fetchAuthRule(ServiceModel serviceModel) {
        RowMapper rowMapper = RowMapperFactory.getMapper(AUTH);
        List<FemasAuthRule> femasAuthRules = manager.selectListPojoByMapper(rowMapper,"select * from auth_rule where namespace_id=? and service_name=?",
                serviceModel.getNamespaceId(), serviceModel.getServiceName());
        return femasAuthRules;
    }

    /**
     * 删除鉴权规则
     *
     * @param serviceAuthRuleModel
     * @return
     */
    @Override
    public int deleteAuthRule(ServiceAuthRuleModel serviceAuthRuleModel) {
        return manager.deleteById("auth_rule","rule_id", serviceAuthRuleModel.getRuleId());
    }

    /**
     * 配置熔断规则
     *
     * @param circuitBreakerRule
     * @return
     */
    @Override
    public Result configureBreakerRule(FemasCircuitBreakerRule circuitBreakerRule) {
        int res = 0;
        if(StringUtils.isEmpty(circuitBreakerRule.getRuleId())){
            circuitBreakerRule.setRuleId("bk-" + iidGeneratorService.nextHashId());
            res = manager.update("insert into circuit_breaker_rule(rule_id,namespace_id,service_name,target_namespace_id,target_service_name, " +
                    "rule_name,isolation_level,strategy,is_enable,update_time,circuit_breaker_rule.desc) values(?,?,?,?,?,?,?,?,?,?,?)",
                    circuitBreakerRule.getRuleId(), circuitBreakerRule.getNamespaceId(), circuitBreakerRule.getServiceName(),
                    circuitBreakerRule.getTargetNamespaceId(), circuitBreakerRule.getTargetServiceName(),
                    circuitBreakerRule.getRuleName(), circuitBreakerRule.getIsolationLevel(),
                    JSONSerializer.serializeStr(circuitBreakerRule.getStrategy()), circuitBreakerRule.getIsEnable(),
                    new Date().getTime(), circuitBreakerRule.getDesc());
        }else{
            res = manager.update("update circuit_breaker_rule set target_namespace_id=?,target_service_name=?, " +
                    "rule_name=?, isolation_level=?, strategy=?, is_enable=?, update_time=?, circuit_breaker_rule.desc=? where rule_id=?",
                    circuitBreakerRule.getTargetNamespaceId(), circuitBreakerRule.getTargetServiceName(),
                    circuitBreakerRule.getRuleName(),circuitBreakerRule.getIsolationLevel(),
                    JSONSerializer.serializeStr(circuitBreakerRule.getStrategy()), circuitBreakerRule.getIsEnable(),
                    new Date().getTime(), circuitBreakerRule.getDesc(), circuitBreakerRule.getRuleId());
        }
        if(ResultCheck.checkCount(res)){
            return Result.successMessage("服务熔断规则编辑成功");
        }
        return Result.errorMessage("服务熔断规则编辑失败");
    }

    /**
     * 查询熔断规则
     *
     * @param circuitBreakerModel
     * @return
     */
    @Override
    public List<FemasCircuitBreakerRule> fetchBreakerRule(CircuitBreakerModel circuitBreakerModel) {
        RowMapper<FemasCircuitBreakerRule> rowMapper = RowMapperFactory.getMapper(CIRCUIT_BREAKER);
        if(!StringUtils.isEmpty(circuitBreakerModel.getIsolationLevel())){
            return manager.selectListPojoByMapper(rowMapper,"select * from circuit_breaker_rule where namespace_id=? and service_name=? and isolation_level=?",
                    circuitBreakerModel.getNamespaceId(), circuitBreakerModel.getServiceName(),
                    circuitBreakerModel.getIsolationLevel());
        }else {
            return manager.selectListPojoByMapper(rowMapper,"select * from circuit_breaker_rule where namespace_id=? and service_name=?",
                    circuitBreakerModel.getNamespaceId(), circuitBreakerModel.getServiceName());
        }
    }

    /**
     * 删除熔断规则
     *
     * @param ruleModel
     * @return
     */
    @Override
    public int deleteBreakerRule(RuleModel ruleModel) {
        return manager.deleteById("circuit_breaker_rule","rule_id",ruleModel.getRuleId());
    }

    /**
     * 配置限流规则
     *
     * @param limitRule
     * @return
     */
    @Override
    public int configureLimitRule(FemasLimitRule limitRule) {
        limitRule.setUpdateTime(new Date().getTime());
        if (StringUtils.isEmpty(limitRule.getRuleId())) {
            limitRule.setRuleId("lt-" + iidGeneratorService.nextHashId());
            return manager.update("insert into rate_limit_rule(rule_id,namespace_id,service_name,rule_name,type,tags,duration,total_quota,status,update_time,rate_limit_rule.desc) " +
                            "values(?,?,?,?,?,?,?,?,?,?,?)",
                    limitRule.getRuleId(), limitRule.getNamespaceId(), limitRule.getServiceName(),
                    limitRule.getRuleName(), limitRule.getType().name(),
                    JSONSerializer.serializeStr(limitRule.getTags()), limitRule.getDuration(),
                    limitRule.getTotalQuota(), limitRule.getStatus(), limitRule.getUpdateTime(),
                    limitRule.getDesc());
        }else {
            return manager.update("update rate_limit_rule set rule_name=?, type=?, tags=?, duration=?, total_quota=?, status=?, update_time=?, rate_limit_rule.desc=? where rule_id=?",
                    limitRule.getRuleName(), limitRule.getType().name(),
                    JSONSerializer.serializeStr(limitRule.getTags()), limitRule.getDuration(),
                    limitRule.getTotalQuota(), limitRule.getStatus(),
                    limitRule.getUpdateTime(), limitRule.getDesc(),
                    limitRule.getRuleId());
        }
    }

    /**
     * 查询限流规则
     *
     * @param serviceModel
     * @return
     */
    @Override
    public List<FemasLimitRule> fetchLimitRule(LimitModel serviceModel) {
        RowMapper rowMapper = RowMapperFactory.getMapper(RATE_LIMIT);
        if(serviceModel.getType() != null){
            return manager.selectListPojoByMapper(rowMapper,"select * from rate_limit_rule where namespace_id=? and service_name=? and type=?",
                    serviceModel.getNamespaceId(), serviceModel.getServiceName(), serviceModel.getType());
        }else {
            return manager.selectListPojoByMapper(rowMapper,"select * from rate_limit_rule where namespace_id=? and service_name=?",
                    serviceModel.getNamespaceId(), serviceModel.getServiceName());
        }
    }

    /**
     * 删除限流规则
     *
     * @param ruleModel
     * @return
     */
    @Override
    public int deleteLimitRule(RuleModel ruleModel) {
        return manager.deleteById("rate_limit_rule","rule_id", ruleModel.getRuleId());
    }

    /**
     * 查询路由规则
     *
     * @param serviceModel
     * @return
     */
    @Override
    public List<FemasRouteRule> fetchRouteRule(ServiceModel serviceModel) {
        RowMapper rowMapper = RowMapperFactory.getMapper(ROUTE);
        return manager.selectListPojoByMapper(rowMapper, "select * from route_rule where namespace_id=? and service_name=?",
                serviceModel.getNamespaceId(), serviceModel.getServiceName());
    }

    /**
     * 删除路由规则
     *
     * @param ruleModel
     * @return
     */
    @Override
    public int deleteRouteRule(RuleModel ruleModel) {
        return manager.deleteById("route_rule", "rule_id", ruleModel.getRuleId());
    }

    /**
     * 配置路由规则
     *
     * @param routeRule
     * @return
     */
    @Override
    public int configureRouteRule(FemasRouteRule routeRule) {
        // 静默处理
        if(!StringUtils.isEmpty(routeRule.getStatus()) && "1".equalsIgnoreCase(routeRule.getStatus())){
            manager.update("update route_rule set route_rule.status='0' where namespace_id=? and service_name=?",
                    routeRule.getNamespaceId(), routeRule.getServiceName());
        }
        routeRule.setUpdateTime(new Date().getTime());
        if (StringUtils.isEmpty(routeRule.getRuleId())) {
            routeRule.setRuleId("rt-" + iidGeneratorService.nextHashId());
            routeRule.setCreateTime(new Date().getTime());
            return manager.update("insert into route_rule(rule_id,namespace_id,service_name,rule_name,route_rule.status,route_tag,create_time,update_time,route_rule.desc) " +
                            "values(?,?,?,?,?,?,?,?,?)",
                    routeRule.getRuleId(), routeRule.getNamespaceId(), routeRule.getServiceName(),
                    routeRule.getRuleName(), routeRule.getStatus(),
                    JSONSerializer.serializeStr(routeRule.getRouteTag()), routeRule.getCreateTime(),
                    routeRule.getUpdateTime(), routeRule.getDesc());
        }else {
            return manager.update("update route_rule set rule_name=?,route_rule.status=?,route_tag=?,update_time=?,route_rule.desc=? where rule_id=?",
                    routeRule.getRuleName(), routeRule.getStatus(),
                    JSONSerializer.serializeStr(routeRule.getRouteTag()), routeRule.getUpdateTime(),
                    routeRule.getDesc(), routeRule.getRuleId());
        }
    }

    /**
     * 新增操作日志
     *
     * @param record
     * @return
     */
    @Override
    public int configureRecord(Record record) {
        return manager.update("insert into record(log_id,user,status,detail,type,module,time) " +
                        "values(?,?,?,?,?,?,?)",
                record.getLogId(), record.getUser(),
                record.getStatus() ? 1 : 0, record.getDetail(),
                record.getType(), record.getModule(),
                new Date().getTime());
    }

    /**
     * 编辑路由容错开关
     *
     * @param tolerate
     * @return
     */
    @Override
    public int configureTolerant(Tolerate tolerate) {
        int res = manager.selectInteger("select count(1) from tolerant where namespace_id=? and service_name=?",
                tolerate.getNamespaceId(), tolerate.getServiceName());
        if(res == 0 ){
            return manager.update("insert into tolerant(namespace_id,service_name,is_tolerant) values(?,?,?)",
                    tolerate.getNamespaceId(), tolerate.getServiceName(), tolerate.getIsTolerant());
        }else{
            return manager.update("update tolerant set is_tolerant=? where namespace_id=? and service_name=?",
                    tolerate.getIsTolerant(), tolerate.getNamespaceId(), tolerate.getServiceName());
        }
    }

    /**
     * 查询路由容错开关
     *
     * @param tolerateModel
     */
    @Override
    public boolean fetchTolerant(TolerateModel tolerateModel) {
        String res = manager.selectString("select is_tolerant from tolerant where namespace_id=? and service_name=?",
                tolerateModel.getNamespaceId(), tolerateModel.getServiceName());
        if(StringUtils.isEmpty(res)){
            return false;
        }
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
        RowMapper<FemasAuthRule> mapper = RowMapperFactory.getMapper(AUTH);
        return manager.selectById(mapper, "auth_rule", "rule_id", ruleSearch.getRuleId());
    }

    /**
     * 查询单条路由规则
     *
     * @param ruleSearch
     */
    @Override
    public FemasRouteRule fetchRouteRuleById(RuleSearch ruleSearch) {
        RowMapper<FemasRouteRule> mapper = RowMapperFactory.getMapper(ROUTE);
        return manager.selectById(mapper,"route_rule", "rule_id", ruleSearch.getRuleId());
    }

    /**
     * 查询单条熔断规则
     *
     * @param ruleSearch
     * @return
     */
    @Override
    public FemasCircuitBreakerRule fetchBreakerRuleById(RuleSearch ruleSearch) {
        RowMapper<FemasCircuitBreakerRule> mapper = RowMapperFactory.getMapper(CIRCUIT_BREAKER);
        return manager.selectById(mapper ,"circuit_breaker_rule", "rule_id", ruleSearch.getRuleId());
    }

    /**
     * 查询单条限流规则
     *
     * @param ruleSearch
     * @return
     */
    @Override
    public FemasLimitRule fetchLimitRuleById(RuleSearch ruleSearch) {
        RowMapper<FemasLimitRule> mapper = RowMapperFactory.getMapper(RATE_LIMIT);
        return manager.selectById(mapper,"rate_limit_rule", "rule_id", ruleSearch.getRuleId());
    }

    /**
     * 查询操作日志
     *
     * @param logModel
     * @return
     */
    @Override
    public PageService<Record> fetchLogs(LogModel logModel) {
        String baseSql = "select * from record where record.time >= ? and record.time <= ? ";
        if(StringUtils.isEmpty(logModel.getModule())){
            return manager.selectByPagesOrdered(new BeanPropertyRowMapper<>(Record.class), baseSql,
                    logModel.getPageNo(),logModel.getPageSize(),
                    DESC, "record.time",
                    logModel.getStartTime(), logModel.getEndTime());
        }else{
            baseSql += "and module = ?";
            return manager.selectByPagesOrdered(new BeanPropertyRowMapper<>(Record.class), baseSql,
                    logModel.getPageNo(),logModel.getPageSize(),
                    DESC, "record.time",
                    logModel.getStartTime(), logModel.getEndTime(), LogModuleEnum.valueOf(logModel.getModule()).getName());
        }
    }

    /**
     * 查询命名空间下的路由规则
     *
     * @param namespaceId
     * @return
     */
    @Override
    public List<FemasRouteRule> fetchRouteRuleByNamespaceId(String namespaceId) {
        RowMapper<FemasRouteRule> rowMapper = RowMapperFactory.getMapper(ROUTE);
        return manager.selectListPojoByMapper(rowMapper,"select * from route_rule where namespace_id = ?",
                namespaceId);
    }

    /**
     * 拉取配置表的值
     *
     * @param key
     * @return
     */
    @Override
    public String fetchConfig(String key) {
        String res = manager.selectString("select config_value from config where config_key = ?", key);
        if(StringUtils.isEmpty(res)){
            return null;
        }
        return res;
    }

    /**
     * 修改配置表
     *
     * @param key
     * @return
     */
    @Override
    public int configureConfig(String key, String value) {
        int count = manager.selectInteger("select count(1) from config where config_key = ?", key);
        if(count == 0){
            return manager.update("insert into config(config_key,config_value) values(?,?)", key, value);
        }
        return manager.update("update config set config_value=? where config_key=?", value, key);
    }


    /** dcfg start **/

    @Override
    public int configureDcfg(Config config) {
        if(StringUtils.isEmpty(config.getConfigId())){
            config.setConfigId(DCFG_ID_PREFIX.concat(iidGeneratorService.nextHashId()));
            return manager.update("insert into dcfg_config(config_id, config_name, namespace_id, service_name, system_tag, config_desc, config_type, create_time) values (?,?,?,?,?,?,?,?)",
                    config.getConfigId(),
                    config.getConfigName(),
                    config.getNamespaceId(),
                    config.getServiceName(),
                    config.getSystemTag(),
                    config.getConfigDesc(),
                    config.getConfigType(),
                    config.getCreateTime()
            );
        }
       return manager.update("update dcfg_config set config_desc = ?, release_time = ?, last_release_version_id = ?, current_release_version_id = ? where config_id = ?",
               config.getConfigDesc(),
               config.getReleaseTime(),
               config.getLastReleaseVersionId(),
               config.getCurrentReleaseVersionId(),
               config.getConfigId()
       );
    }



    @Override
    public PageService<Config> fetchDcfgs(ConfigRequest request) {
        String sql = "select * from dcfg_config where namespace_id = ? ";
        List params = new ArrayList();
        params.add(request.getNamespaceId());
        if (StringUtils.isNotEmpty(request.getConfigId())) {
            sql += " and config_id = ?";
            params.add(request.getConfigId());
        }
        if (StringUtils.isNotEmpty(request.getSearchWord())) {
            sql += " and (config_id like ? or config_name like ?)";
            params.add('%' + request.getSearchWord() + '%');
            params.add('%' + request.getSearchWord() + '%');
        }
        return manager.selectByPagesOrdered(new BeanPropertyRowMapper(Config.class), sql, request.getPageNo(),request.getPageSize(), DESC, "create_time", params.toArray());
    }


    @Override
    public int deleteDcfg(String configId, String namespaceId) {
        return manager.deleteById("dcfg_config", "config_id", configId);
    }

    @Override
    public int deleteDcfgVersion(String configVersionId, String configId) {
        return manager.deleteById("dcfg_config_version", "config_version_id", configVersionId);
    }

    @Override
    public List<Config> fetchDcfgOther(List<String> configIdList) {
        if (CollectionUtil.isEmpty(configIdList)) {
            return Arrays.asList();
        }
        String join = configIdList.stream().map(str -> "'" + str + "'").collect(Collectors.joining(", "));
//        String sql = "select config_id configId, count(1) versionCount , max(log.release_time) releaseTime from dcfg_config_version version left join dcfg_config_release_log log on version.config_version_id = log.config_version_id "
//        +" where log.release_status = 'S' and version.config_id in( " + join + " )";
        String sql = "select config_id configId, count(1) versionCount from dcfg_config_version where config_id in( " + join + " ) group by config_id";
        return manager.selectListPojo(sql, Config.class);
    }



    @Override
    public int configureDcfgVersion(ConfigVersion configVersion) {
        if(StringUtils.isEmpty(configVersion.getConfigVersionId())) {
            configVersion.setConfigVersionId(DCFGV_ID_PREFIX.concat(iidGeneratorService.nextHashId()));
            return manager.update("insert into dcfg_config_version(config_version_id, config_id, config_version, config_value, create_time, release_time, release_status) values (?,?,?,?,?,?,?)",
                    configVersion.getConfigVersionId(),
                    configVersion.getConfigId(),
                    configVersion.getConfigVersion(),
                    configVersion.getConfigValue(),
                    configVersion.getCreateTime(),
                    configVersion.getReleaseTime(),
                    configVersion.getReleaseStatus()
            );
        }
        return manager.update("update dcfg_config_version set release_time = ?, release_status = ? where config_version_id = ?",
                configVersion.getReleaseTime(),
                configVersion.getReleaseStatus(),
                configVersion.getConfigVersionId()
        );
    }



    @Override
    public PageService<ConfigVersion> fetchDcfgVersions(ConfigRequest request) {
        String sql = "select * from dcfg_config_version where config_id = ? ";
        List params = new ArrayList();
        params.add(request.getConfigId());
        if (StringUtils.isNotEmpty(request.getConfigVersionId())) {
            sql += " and config_version_id = ?";
            params.add(request.getConfigVersionId());
        }
        if (StringUtils.isNotEmpty(request.getReleaseStatus())) {
            sql += " and release_status = ?";
            params.add(request.getReleaseStatus());
        }
        if (StringUtils.isNotEmpty(request.getSearchWord())) {
            sql += " and (config_version_id like ?)";
            params.add('%' + request.getSearchWord() + '%');
        }
        return manager.selectByPagesOrdered(new BeanPropertyRowMapper(ConfigVersion.class), sql,
                request.getPageNo(), request.getPageSize(),
                (request.getOrderType() == 0 ? DESC : ASC),
                (StringUtils.isEmpty(request.getOrderBy()) ? "create_time" : CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, request.getOrderBy())), // 小驼峰转下划线
                params.toArray());
    }

    @Override
    public int configureDcfgReleaseLog(ConfigReleaseLog configReleaseLog) {
        return manager.update("insert into dcfg_release_log(config_id, config_version_id, last_config_version_id, release_time) values(?,?,?,?)",
                configReleaseLog.getConfigId(),
                configReleaseLog.getConfigVersionId(),
                configReleaseLog.getLastConfigVersionId(),
                configReleaseLog.getReleaseTime()
        );
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
        String sql = "select * from auth_rule where namespace_id=? and service_name=?";
        if(!StringUtils.isEmpty(authRuleModel.getKeyword())){
           sql += " and rule_name like '%" + authRuleModel.getKeyword() + "%'";
        }
        sql += " order by available_time desc";
        PageService pageService = manager.selectByPages(RowMapperFactory.getMapper(AUTH), sql, authRuleModel.getPageNo(), authRuleModel.getPageSize(),
                authRuleModel.getNamespaceId(), authRuleModel.getServiceName());
        return pageService;
    }

    /**
     * 查询熔断规则
     *
     * @param circuitBreakerModel
     * @return
     */
    @Override
    public PageService<FemasCircuitBreakerRule> fetchBreakerRulePages(CircuitBreakerModel circuitBreakerModel) {
        RowMapper rowMapper = RowMapperFactory.getMapper(CIRCUIT_BREAKER);
        PageService pageService;
        String sql = "select * from circuit_breaker_rule where namespace_id=? and service_name=?";
        if(!StringUtils.isEmpty(circuitBreakerModel.getIsolationLevel())){
            sql += " and isolation_level='" + circuitBreakerModel.getIsolationLevel() + "'";
        }
        if(!StringUtils.isEmpty(circuitBreakerModel.getSearchWord())){
            sql += " and target_service_name like '%" + circuitBreakerModel.getSearchWord() + "%'";
        }
        sql += " order by update_time desc";
        pageService =  manager.selectByPages(rowMapper, sql,
                circuitBreakerModel.getPageNo(), circuitBreakerModel.getPageSize(),
                circuitBreakerModel.getNamespaceId(), circuitBreakerModel.getServiceName());
        return pageService;
    }

    /**
     * 查询限流规则
     *
     * @param serviceModel
     * @return
     */
    @Override
    public PageService<FemasLimitRule> fetchLimitRulePages(LimitModel serviceModel) {
        RowMapper rowMapper = RowMapperFactory.getMapper(RATE_LIMIT);
        PageService pageService;
        String sql = "select * from rate_limit_rule where namespace_id=? and service_name=?";
        if(!StringUtils.isEmpty(serviceModel.getType())){
            sql += " and rate_limit_rule.type='" + serviceModel.getType() + "'";
        }
        if(!StringUtils.isEmpty(serviceModel.getKeyword())){
            sql += " and rule_name like '%" + serviceModel.getKeyword() + "%'";
        }
        sql += " order by update_time desc";
        pageService = manager.selectByPages(rowMapper, sql,
                serviceModel.getPageNo(), serviceModel.getPageSize(),
                serviceModel.getNamespaceId(), serviceModel.getServiceName());
        return pageService;
    }

    /**
     * 查询路由规则
     *
     * @param serviceModel
     * @return
     */
    @Override
    public PageService<FemasRouteRule> fetchRouteRulePages(ServiceModel serviceModel) {
        RowMapper rowMapper = RowMapperFactory.getMapper(ROUTE);
        String sql = "select * from route_rule where namespace_id=? and service_name=?";
        sql += " order by update_time desc";
        return manager.selectByPages(rowMapper, sql,
                serviceModel.getPageNo(), serviceModel.getPageSize(),
                serviceModel.getNamespaceId(), serviceModel.getServiceName());
    }

    @Override
    public Integer configureLane(LaneInfo laneInfo) {
        if (StringUtils.isEmpty(laneInfo.getLaneId())) {
            laneInfo.setLaneId("lane-" + iidGeneratorService.nextHashId());
            long time = new Date().getTime();
            laneInfo.setCreateTime(time);
            laneInfo.setUpdateTime(time);
            return manager.update("insert into lane_info(lane_id,lane_name,remark,create_time,update_time,lane_service_list) " +
                            "values(?,?,?,?,?,?)",
                    laneInfo.getLaneId(), laneInfo.getLaneName(),
                    laneInfo.getRemark(), laneInfo.getCreateTime(),
                    laneInfo.getUpdateTime(), JSONSerializer.serializeStr(laneInfo.getLaneServiceList()));
        }else {
            laneInfo.setUpdateTime(new Date().getTime());
            return manager.update("update lane_info set lane_name=?, remark=?, create_time=?, update_time=?, lane_service_list=? where lane_id=?",
                    laneInfo.getLaneName(), laneInfo.getRemark(),
                    laneInfo.getCreateTime(), laneInfo.getUpdateTime(),
                    JSONSerializer.serializeStr(laneInfo.getLaneServiceList()), laneInfo.getLaneId());
        }
    }

    @Override
    public LaneInfo fetchLaneById(String laneId) {
        RowMapper<LaneInfo> mapper = RowMapperFactory.getMapper(LANE_INFO);
        return manager.selectById(mapper,"lane_info", "lane_id", laneId);
    }

    @Override
    public PageService<LaneInfo> fetchLaneInfoPages(LaneInfoModel laneInfoModel) {
        RowMapper rowMapper = RowMapperFactory.getMapper(LANE_INFO);
        PageService pageService;
        String sql = "select * from lane_info";
        if(!StringUtils.isEmpty(laneInfoModel.getLaneId())){
            sql += " and lane_info.lane_id like '%" + laneInfoModel.getLaneId() + "%'";
        }
        if(!StringUtils.isEmpty(laneInfoModel.getLaneName())){
            sql += " and lane_info.lane_name like '%" + laneInfoModel.getLaneName() + "%'";
        }
        if(!StringUtils.isEmpty(laneInfoModel.getRemark())){
            sql += " and lane_info.remark like '%" + laneInfoModel.getRemark() + "%'";
        }
        sql = sql.replaceFirst("and", "where");
        pageService = manager.selectByPagesOrdered(rowMapper, sql,
                laneInfoModel.getPageNo(), laneInfoModel.getPageSize(),
                DESC, "update_time");
        return pageService;
    }


    @Override
    public Integer deleteLane(String laneId) {
        return manager.deleteById("lane_info", "lane_id", laneId);
    }

    @Override
    public Integer configureLaneRule(LaneRule laneRule) {
        if (StringUtils.isEmpty(laneRule.getRuleId())) {
            laneRule.setRuleId("laneRule-" + iidGeneratorService.nextHashId());
            long time = new Date().getTime();
            laneRule.setCreateTime(time);
            laneRule.setUpdateTime(time);
            laneRule.setPriority(time);
            return manager.update("insert into lane_rule(rule_id,rule_name,remark,enable,create_time,update_time,relative_lane,rule_tag_list,rule_tag_relationship,gray_type,priority) " +
                            "values(?,?,?,?,?,?,?,?,?,?,?)",
                    laneRule.getRuleId(), laneRule.getRuleName(),
                    laneRule.getRemark(), laneRule.getEnable(),
                    laneRule.getCreateTime(), laneRule.getUpdateTime(), JSONSerializer.serializeStr(laneRule.getRelativeLane()),
                    JSONSerializer.serializeStr(laneRule.getRuleTagList()), laneRule.getRuleTagRelationship().toString(), laneRule.getGrayType().toString(), laneRule.getPriority());
        }else {
            laneRule.setUpdateTime(new Date().getTime());
            return manager.update("update lane_rule set rule_name=?, remark=?, enable=?, create_time=?, update_time=?, relative_lane=?, rule_tag_list=?, rule_tag_relationship=?, gray_type=?, priority=? where rule_id=?",
                    laneRule.getRuleName(), laneRule.getRemark(),
                    laneRule.getEnable(), laneRule.getCreateTime(),
                    laneRule.getUpdateTime(), JSONSerializer.serializeStr(laneRule.getRelativeLane()),
                    JSONSerializer.serializeStr(laneRule.getRuleTagList()),
                    laneRule.getRuleTagRelationship().toString(),laneRule.getGrayType().toString(), laneRule.getPriority(), laneRule.getRuleId());
        }
    }

    @Override
    public LaneRule fetchLaneRuleById(String laneRuleId) {
        RowMapper<LaneRule> mapper = RowMapperFactory.getMapper(LANE_RULE);
        return manager.selectById(mapper,"lane_rule", "rule_id", laneRuleId);
    }

    @Override
    public PageService<LaneRule> fetchLaneRulePages(LaneRuleModel laneRuleModel) {
        RowMapper rowMapper = RowMapperFactory.getMapper(LANE_RULE);
        PageService pageService;
        String sql = "select * from lane_rule";
        if(!StringUtils.isEmpty(laneRuleModel.getRuleId())){
            sql += " and lane_rule.rule_id like '%" + laneRuleModel.getRuleId() + "%'";
        }
        if(!StringUtils.isEmpty(laneRuleModel.getRemark())){
            sql += " and lane_rule.remark like '%" + laneRuleModel.getRemark() + "%'";
        }
        if(!StringUtils.isEmpty(laneRuleModel.getRuleName())){
            sql += " and lane_rule.rule_name like '%" + laneRuleModel.getRuleName() + "%'";
        }
        sql = sql.replaceFirst("and", "where");
        pageService = manager.selectByPagesOrdered(rowMapper, sql,
                laneRuleModel.getPageNo(), laneRuleModel.getPageSize(),
                DESC, "priority");
        return pageService;
    }

    @Override
    public Integer deleteLaneRule(String laneRuleId) {
        return manager.deleteById("lane_rule", "rule_id", laneRuleId);
    }

    @Override
    public List<LaneInfo> fetchLaneInfo() {
        RowMapper rowMapper = RowMapperFactory.getMapper(LANE_INFO);
        return manager.selectListPojoByMapper(rowMapper, "select * from lane_info");
    }

    @Override
    public List<LaneRule> fetchLaneRule() {
        RowMapper rowMapper = RowMapperFactory.getMapper(LANE_RULE);
        return manager.selectListPojoByMapper(rowMapper, "select * from lane_rule");
    }

}
