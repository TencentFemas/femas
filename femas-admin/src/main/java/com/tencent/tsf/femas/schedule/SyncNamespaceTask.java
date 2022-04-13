package com.tencent.tsf.femas.schedule;

import static com.tencent.tsf.femas.constant.AdminConstants.NAMESPACE_ID;

import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.entity.namespace.Namespace;
import com.tencent.tsf.femas.entity.param.RegistryInstanceParam;
import com.tencent.tsf.femas.entity.registry.RegistryConfig;
import com.tencent.tsf.femas.entity.registry.RegistryPageService;
import com.tencent.tsf.femas.entity.registry.RegistrySearch;
import com.tencent.tsf.femas.entity.registry.ServiceBriefInfo;
import com.tencent.tsf.femas.service.namespace.NamespaceMangerService;
import com.tencent.tsf.femas.service.registry.OpenApiFactory;
import com.tencent.tsf.femas.service.registry.RegistryOpenApiInterface;
import com.tencent.tsf.femas.storage.DataOperation;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Cody
 * @date 2021 2021/8/30 2:24 下午
 */
@Component
public class SyncNamespaceTask {


    @Autowired
    DataOperation dataOperation;

    @Autowired
    OpenApiFactory factory;

    @Autowired
    NamespaceMangerService namespaceMangerService;


    /**
     * 初始化创建的命名空间
     */
//    @Scheduled(fixedDelay = 3000)
    public void initNamespace() {
        List<RegistryConfig> registryConfigs = dataOperation.fetchRegistryConfigs(new RegistrySearch());
        if (!CollectionUtil.isEmpty(registryConfigs)) {
            for (RegistryConfig config : registryConfigs) {
                RegistryOpenApiInterface registryApi = factory.select(config.getRegistryType());
                RegistryInstanceParam param = new RegistryInstanceParam();
                param.setPageNo(1);
                param.setPageSize(Integer.MAX_VALUE);
                RegistryPageService registryPageService = registryApi.fetchServices(config, param);
                if (CollectionUtil.isEmpty(registryConfigs)) {
                    continue;
                }
                for (ServiceBriefInfo serviceBriefInfo : registryPageService.getServiceBriefInfos()) {
                    RegistryInstanceParam registryInstanceParam = new RegistryInstanceParam();
                    registryInstanceParam.setServiceName(serviceBriefInfo.getServiceName());
                    List<ServiceInstance> serviceInstances = registryApi
                            .fetchServiceInstances(config, registryInstanceParam);
                    if (CollectionUtil.isEmpty(serviceInstances)) {
                        continue;
                    }
                    for (ServiceInstance instance : serviceInstances) {
                        if (instance.getAllMetadata() != null && !StringUtils
                                .isEmpty(instance.getMetadata(NAMESPACE_ID))) {
                            Namespace oldNamespace = dataOperation
                                    .fetchNamespaceById(instance.getMetadata(NAMESPACE_ID));
                            if (oldNamespace != null) {
                                continue;
                            }
                            Namespace namespace = new Namespace();
                            namespace.setName("default-name-include-" + serviceBriefInfo.getServiceName());
                            namespace.setDesc("default");
                            namespace.setNamespaceId(instance.getMetadata(NAMESPACE_ID));
                            ArrayList<String> registryId = new ArrayList<>();
                            registryId.add(config.getRegistryId());
                            namespace.setRegistryId(registryId);
                            namespaceMangerService.createNamespace(namespace);
                        }
                    }
                }
            }
        }
    }
}
