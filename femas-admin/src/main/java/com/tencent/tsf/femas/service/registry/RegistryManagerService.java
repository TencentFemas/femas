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
package com.tencent.tsf.femas.service.registry;

import com.tencent.tsf.femas.common.discovery.DiscoveryService;
import com.tencent.tsf.femas.common.discovery.ServiceDiscoveryClient;
import com.tencent.tsf.femas.common.entity.EndpointStatus;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.entity.namespace.Namespace;
import com.tencent.tsf.femas.entity.param.RegistryInstanceParam;
import com.tencent.tsf.femas.entity.registry.*;
import com.tencent.tsf.femas.service.IIDGeneratorService;
import com.tencent.tsf.femas.service.ServiceExecutor;
import com.tencent.tsf.femas.storage.DataOperation;
import com.tencent.tsf.femas.util.ResultCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tencent.tsf.femas.common.RegistryConstants.REGISTRY_HOST;
import static com.tencent.tsf.femas.common.RegistryConstants.REGISTRY_PORT;
import static com.tencent.tsf.femas.constant.AdminConstants.FEMAS_META_APPLICATION_VERSION_KEY;
import static com.tencent.tsf.femas.constant.AdminConstants.FEMAS_META_NAMESPACE_ID_KEY;

/**
 * @Author leoziltong@tencent.com
 * @Author cody
 * @Date: 2021/4/27 19:49
 */
@Service
public class RegistryManagerService implements ServiceExecutor {

    private final static Logger log = LoggerFactory.getLogger(RegistryManagerService.class);

    private final OpenApiFactory factory;

    private final DataOperation dataOperation;

    private final Map<String, RegistryConfig> registryConfigMapCache = new ConcurrentHashMap<>();

    private Map<String, ServiceDiscoveryClient> serviceDiscoveryClientMap = new ConcurrentHashMap<>();

    private final ExecutorService executorService;

    private final IIDGeneratorService iidGeneratorService;

    private final static Long FUTURE_TASK_TIMEOUT_SEC = 5L;

    public RegistryManagerService(DataOperation dataOperation,
                                  OpenApiFactory factory,
                                  IIDGeneratorService iidGeneratorService,
                                  ExecutorService executorService) {
        this.dataOperation = dataOperation;
        this.factory = factory;
        this.iidGeneratorService = iidGeneratorService;
        this.executorService = executorService;
    }

    public void addDiscoveryClient(RegistryConfig config) {
        String conf = config.getRegistryCluster();
        String[] strings = conf.split(":");
        Map<String, String> confMap = new HashMap<>();
        confMap.put(REGISTRY_HOST, strings[0]);
        confMap.put(REGISTRY_PORT, strings[1]);
        ServiceDiscoveryClient client = DiscoveryService.createDiscoveryClient(config.getRegistryType(), confMap);
        serviceDiscoveryClientMap.put(config.getRegistryType(), client);
    }

    public Result configureRegistry(RegistryModel registryModel) {
        RegistryConfig config = registryModel.toRegistryConfig();
        if (StringUtils.isNotEmpty(registryModel.getRegistryCluster()) && !checkUrls(registryModel.getRegistryCluster())) {
            return Result.errorMessage("URL格式错误 请输入正确的URL格式 ip:端口、域名(多个地址使用,隔开)");
        }
        int res = dataOperation.configureRegistry(config);
        if (ResultCheck.checkCount(res)) {
            registryConfigMapCache.put(config.getRegistryId(), config);
            return Result.successData("配置成功", true);
        } else {
            return Result.errorData("配置失败", false);
        }
    }

    public Result checkCertificateConf(RegistryModel registryModel) {
        RegistryOpenApiInterface registryOpenApiInterface = factory.select(registryModel.getRegistryType());
        boolean healthy = registryOpenApiInterface.healthCheck(registryModel.toRegistryConfig());
        if (healthy) {
            return Result.successData("验证成功", true);
        }
        return Result.errorData("验证失败", false);
    }

    public Result describeRegistryClusters(RegistrySearch registrySearch) {
        List<RegistryConfig> registryConfigs = dataOperation.fetchRegistryConfigs(registrySearch);
        final List<FutureTask<Void>> tasks = new ArrayList<>();
        ArrayList<RegistryConfig> res = new ArrayList<>();
        if (!CollectionUtil.isEmpty(registryConfigs)) {
            registryConfigs.stream().forEach(config -> {
                FutureTask<Void> task = new FutureTask<Void>(() -> {
                    RegistryInfo registryInfo = (RegistryInfo) describeRegistryCluster(config.getRegistryId()).getData();
                    config.setStatus(2);
                    registryInfo.getClusterServers().forEach(server -> {
                        if ("UP".equalsIgnoreCase(server.getState())) {
                            config.setStatus(1);
                        }
                    });
                    if (StringUtils.isEmpty(registrySearch.getStatus()) || registrySearch.getStatus().equals(config.getStatus() + "")) {
                        config.setInstanceCount(registryInfo.getClusterServers().size());
                        res.add(config);
                    }
                    return null;
                });
                executorService.submit(task);
                tasks.add(task);
            });
            tasks.stream().forEach(t -> {
                try {
                    t.get(FUTURE_TASK_TIMEOUT_SEC, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    log.error("queryService  failed  ", e);
                } catch (ExecutionException e) {
                    log.error("queryService  failed  ", e);
                } catch (TimeoutException e) {
                    log.error("queryService  failed  ", e);
                }
            });
        }
        return Result.successData(res);
    }

    public Result deleteRegistryCluster(String registryId) {
        int res = dataOperation.deleteRegistry(registryId);
        if (ResultCheck.checkCount(res)) {
            registryConfigMapCache.remove(registryId);
            return Result.successMessage("注册中心删除成功");
        }
        return Result.errorMessage("注册中心删除失败");
    }

    public Result<RegistryInfo> describeRegistryCluster(String registryId) {
        RegistryInfo registryInfo = new RegistryInfo();
        RegistryConfig config = getSafetyConfigById(registryId);
        if (config == null) {
            return Result.success();
        }
        registryInfo.setConfig(config);
        RegistryOpenApiInterface registryOpenApiInterface = factory.select(config.getRegistryType());
        List<ClusterServer> clusterServers = registryOpenApiInterface.clusterServers(config);
        config.setStatus(2);
        clusterServers.forEach(server -> {
            if ("UP".equalsIgnoreCase(server.getState())) {
                config.setStatus(1);
            }
        });
        config.setInstanceCount(clusterServers.size());
        registryInfo.setClusterServers(clusterServers);
        int namespaceCount = dataOperation.getNamespacesCountByRegistry(registryId);
        registryInfo.setNamespaceCount(namespaceCount);
        return Result.successData(registryInfo);
    }


    public Result<RegistryPageService> describeRegisterService(String namespaceId, String status, Integer pageNo, Integer pageSize, String keyword) {
        Namespace namespace = dataOperation.fetchNamespaceById(namespaceId);
        if (namespace == null) {
            return Result.errorMessage("命名空间不存在");
        }
        List<String> clusterIds = namespace.getRegistryId();
        if (CollectionUtil.isEmpty(clusterIds)) {
            RegistryPageService registryPageService = new RegistryPageService();
            registryPageService.setCount(0);
            registryPageService.setPageNo(pageNo);
            registryPageService.setPageSize(pageSize);
            registryPageService.setServiceBriefInfos(new ArrayList<>());
            return Result.successData(registryPageService);
        }
        RegistryConfig config = getConfigById(clusterIds.get(0));
        if (config == null) {
            return Result.errorMessage("注册中心不存在");
        }
        RegistryOpenApiInterface registryOpenApiInterface = factory.select(config.getRegistryType());
        if (registryOpenApiInterface == null) {
            return Result.errorMessage("请传入支持的注册中心");
        }
        RegistryInstanceParam registryInstanceParam = new RegistryInstanceParam();
        registryInstanceParam.setPageNo(pageNo);
        registryInstanceParam.setPageSize(pageSize);
        registryInstanceParam.setNamespaceId(namespaceId);
        final List<ServiceBriefInfo> serviceBriefInfos = Collections.synchronizedList(new ArrayList<ServiceBriefInfo>());
        RegistryPageService registryPageService = registryOpenApiInterface.fetchServices(config, registryInstanceParam);

        //过滤并补齐service brief信息
        if (registryPageService.getCount() != null && registryPageService.getCount() > 0) {
            final List<FutureTask<Void>> tasks = new ArrayList<>();
            for (final ServiceBriefInfo serviceBriefInfo : registryPageService.getServiceBriefInfos()) {
                FutureTask<Void> task = new FutureTask<Void>(() -> {
                    // 服务版本，服务状态设定
                    serviceBriefInfo.setStatus(EndpointStatus.DOWN.name());
                    List<ServiceInstance> serviceInstances = describeServiceInstance(namespaceId, serviceBriefInfo.getServiceName());
                    String firstNamespace = "";
                    if (!CollectionUtil.isEmpty(serviceInstances)) {
                        // 取第一个有非空命名空间的实例的namespace 而不是直接取第一个实例的命名空间 防止用户起服务时忘记打tag而无法显示服务
                        for (ServiceInstance serviceInstance : serviceInstances) {
                            if (serviceInstance.getAllMetadata() != null && !StringUtils.isEmpty(serviceInstance.getAllMetadata().get(FEMAS_META_NAMESPACE_ID_KEY))) {
                                firstNamespace = serviceInstance.getAllMetadata().get(FEMAS_META_NAMESPACE_ID_KEY);
                                break;
                            }
                        }
                        if (!StringUtils.isEmpty(firstNamespace) && firstNamespace.equals(namespaceId)) {
                            // 实例版本
                            HashSet<String> versions = new HashSet<>();
                            for (ServiceInstance serviceInstance : serviceInstances) {
                                if (serviceInstance.getAllMetadata() != null && firstNamespace.equals(serviceInstance.getAllMetadata().get(FEMAS_META_NAMESPACE_ID_KEY))) {
                                    versions.add(serviceInstance.getAllMetadata().get(FEMAS_META_APPLICATION_VERSION_KEY));
                                    if (serviceInstance.getStatus() == EndpointStatus.UP) {
                                        serviceBriefInfo.setStatus(EndpointStatus.UP.name());
                                    }
                                }
                            }
                            serviceBriefInfo.setVersionNum(versions.size());
                            // 状态过滤
                            if (!StringUtils.isEmpty(status) && !status.equalsIgnoreCase(serviceBriefInfo.getStatus())) {
                                return null;
                            }
                            // 名称过滤
                            if (!StringUtils.isEmpty(keyword) && !serviceBriefInfo.getServiceName().contains(keyword)) {
                                return null;
                            }
                            serviceBriefInfos.add(serviceBriefInfo);
                        }
                    }
                    return null;
                });
                executorService.submit(task);
                tasks.add(task);
            }
            tasks.stream().forEach(t -> {
                try {
                    t.get();
                } catch (InterruptedException e) {
                    log.error("describe RegisterService brief info failed  ", e);
                } catch (ExecutionException e) {
                    log.error("describe RegisterService brief info failed  ", e);
                }
            });
        }
        registryPageService.setCount(serviceBriefInfos.size());
        registryPageService.setServiceBriefInfos(serviceBriefInfos);
        registryPageService.setRegistryId(namespace.getRegistryId().get(0));
        return Result.successData(registryPageService);
    }

    public List<ServiceInstance> describeServiceInstance(String namespaceId, String serviceName) {
        Namespace namespace = dataOperation.fetchNamespaceById(namespaceId);
        if (namespace == null || CollectionUtil.isEmpty(namespace.getRegistryId())) {
            return null;
        }
        RegistryConfig config = getConfigById(namespace.getRegistryId().get(0));
        if (config == null) {
            return null;
        }
        RegistryOpenApiInterface registryOpenApiInterface = factory.select(config.getRegistryType());
        RegistryInstanceParam registryInstanceParam = new RegistryInstanceParam();
        registryInstanceParam.setServiceName(serviceName);
        registryInstanceParam.setNamespaceId(namespaceId);

        List<ServiceInstance> instances = registryOpenApiInterface.fetchServiceInstances(config, registryInstanceParam);
        return instances;
    }

    //查询注册中心参数
    public RegistryConfig getConfigById(String registryId) {
        RegistryConfig config = registryConfigMapCache.get(registryId);
        try {
            if (config == null) {
                config = dataOperation.fetchRegistryById(registryId);
            }
        } catch (Exception e) {
            log.error("registry manager get config by id failed ", e);
        }
        return config;
    }

    //查询去掉nacos用户名和密码的注册中心参数
    public RegistryConfig getSafetyConfigById(String registryId) {
        RegistryConfig config = getConfigById(registryId);
        if (config != null) {
            config.setUsername("");
            config.setPassword("");
        }
        return config;
    }

    public boolean checkUrls(String urlsString) {
        if (StringUtils.isEmpty(urlsString)) {
            return false;
        }

        String[] urls = urlsString.split(",");
        String urlRegex = "((http://|https://|))(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?";
        for (String url : urls) {
            if (!Pattern.compile(urlRegex).matcher(url).matches()) {
                return false;
            }
        }
        return true;
    }


}
