package com.tencent.tsf.femas.service.registry;

import static com.tencent.tsf.femas.constant.AdminConstants.FEMAS_CLIENT_SDK_VERSION;
import static com.tencent.tsf.femas.constant.AdminConstants.FEMAS_META_APPLICATION_VERSION_KEY;

import com.tencent.tsf.femas.common.entity.EndpointStatus;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.entity.PageService;
import com.tencent.tsf.femas.entity.ServiceVersion;
import com.tencent.tsf.femas.entity.param.InstanceVersionParam;
import com.tencent.tsf.femas.entity.param.RegistryInstanceParam;
import com.tencent.tsf.femas.entity.registry.*;
import com.tencent.tsf.femas.entity.rule.FemasEventData;
import com.tencent.tsf.femas.entity.service.ServiceEventModel;
import com.tencent.tsf.femas.entity.service.ServiceEventView;
import com.tencent.tsf.femas.service.EventService;
import com.tencent.tsf.femas.service.IIDGeneratorService;
import com.tencent.tsf.femas.service.namespace.NamespaceMangerService;
import com.tencent.tsf.femas.storage.DataOperation;
import com.tencent.tsf.femas.util.PageUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Cody
 * @date 2021 2021/7/30 10:58 上午
 */
@Service
public class ServiceManagerService {

    private final RegistryManagerService registryService;

    private final NamespaceMangerService namespaceMangerService;


    private final IIDGeneratorService iidGeneratorService;

    private final DataOperation dataOperation;

    public ServiceManagerService(RegistryManagerService registryService, NamespaceMangerService namespaceMangerService,
            IIDGeneratorService iidGeneratorService, DataOperation dataOperation) {
        this.registryService = registryService;
        this.namespaceMangerService = namespaceMangerService;
        this.iidGeneratorService = iidGeneratorService;
        this.dataOperation = dataOperation;
    }

    public Result<PageService<ServiceInstance>> describeServiceInstance(InstanceVersionParam param) {
        List<ServiceInstance> serviceInstances = registryService
                .describeServiceInstance(param.getNamespaceId(), param.getServiceName());
        ArrayList<ServiceInstance> versionInstances = new ArrayList<>();
        if (serviceInstances != null) {
            serviceInstances.stream().forEach(serviceInstance -> {
                if (serviceInstance.getAllMetadata() != null) {
                    serviceInstance.setServiceVersion(serviceInstance.getMetadata(FEMAS_META_APPLICATION_VERSION_KEY));
                    serviceInstance.setClientVersion(serviceInstance.getMetadata(FEMAS_CLIENT_SDK_VERSION));
                } else {
                    serviceInstance.setServiceVersion("1.0");
                }
                boolean flag = true;
                if (!StringUtils.isEmpty(param.getStatus()) && !param.getStatus()
                        .equalsIgnoreCase(serviceInstance.getStatus().name())) {
                    flag = false;
                }
                if (!StringUtils.isEmpty(param.getServiceVersion()) && !param.getServiceVersion()
                        .equalsIgnoreCase(serviceInstance.getServiceVersion())) {
                    flag = false;
                }
                if (!StringUtils.isEmpty(param.getKeyword()) && !StringUtils.isEmpty(serviceInstance.getId())
                        && !serviceInstance.getId().contains(param.getKeyword())) {
                    flag = false;
                }
                if (flag) {
                    versionInstances.add(serviceInstance);
                }
            });
        }
        List<ServiceInstance> data = PageUtil.pageList(versionInstances, param.getPageNo(), param.getPageSize());
        return Result.successData(new PageService<ServiceInstance>(data, versionInstances.size()));
    }

    public Result<ServiceOverview> describeServiceOverview(RegistryInstanceParam param) {
        List<ServiceInstance> serviceInstances = registryService
                .describeServiceInstance(param.getNamespaceId(), param.getServiceName());
        if (serviceInstances == null || serviceInstances.size() == 0) {
            return Result.successData(null);
        }
        ServiceOverview serviceOverview = new ServiceOverview();
        Integer liveInstanceCount = 0;
        HashSet<String> versions = new HashSet<>();
        for (ServiceInstance serviceInstance : serviceInstances) {
            if (serviceInstance.getAllMetadata() != null) {
                String version = serviceInstance.getMetadata(FEMAS_META_APPLICATION_VERSION_KEY);
                if(version!=null && StringUtils.isBlank(version)){
                    version = null;
                }
                versions.add(version);
            }
            if (serviceInstance.getStatus() == EndpointStatus.UP) {
                liveInstanceCount++;
            }
        }
        // 将下线对应的 api 删除
        dataOperation.deleteOfflinceServiceApi(param.getNamespaceId(), param.getServiceName(), versions);

        serviceOverview.setNamespaceId(param.getNamespaceId());
        serviceOverview.setNamespaceName(
                namespaceMangerService.fetchNamespaceById(param.getNamespaceId()).getData().getName());
        serviceOverview.setStatus(EndpointStatus.DOWN.name());
        if (liveInstanceCount != 0) {
            serviceOverview.setStatus(EndpointStatus.UP.name());
        }
        serviceOverview.setServiceName(param.getServiceName());
        serviceOverview.setVersions(versions);
        serviceOverview.setLiveInstanceCount(liveInstanceCount);
        serviceOverview.setInstanceNum(serviceInstances.size());
        serviceOverview.setVersionNum(versions.size());
        return Result.successData(serviceOverview);
    }

    public Result<PageService<ServiceApi>> describeServiceApi(ApiModel apiModel) {
        PageService pageService = dataOperation.fetchServiceApiData(apiModel);
        return Result.successData(pageService);
    }

    public Result<PageService<ServiceEventView>> describeServiceEvent(
            @RequestBody ServiceEventModel serviceEventModel) {
        PageService<FemasEventData> res = dataOperation.fetchEventData(serviceEventModel);

        ArrayList<ServiceEventView> serviceEventViews = new ArrayList<>();
        if (!CollectionUtil.isEmpty(res.getData())) {
            for (FemasEventData eventData : res.getData()) {
                ServiceEventView serviceEventView = EventService.parse(eventData);
                serviceEventViews.add(serviceEventView);
            }
        }
        return Result.successData(new PageService<>(serviceEventViews, (res.getCount() == null) ? 0 : res.getCount()));
    }

    public void reportServiceApi(String namespaceId, String serviceName, String groupId, String data) {
        String id = "api-" + iidGeneratorService.nextHashId();
        dataOperation.reportServiceApi(namespaceId, serviceName, groupId, id, data);
    }

    public void reportServiceEvent(String namespaceId, String serviceName, String eventId, String data) {
        dataOperation.reportServiceEvent(namespaceId, serviceName, eventId, data);
    }

    public Result<List<ServiceVersion>> describeServiceInstance(String namespaceId){
        Result<RegistryPageService> registryPageServiceResult = registryService.describeRegisterService(namespaceId, null, 1, Integer.MAX_VALUE, null);
        ArrayList<ServiceVersion> res = new ArrayList<>();
        if(registryPageServiceResult.getData() == null || CollectionUtil.isEmpty(registryPageServiceResult.getData().getServiceBriefInfos())){
            return Result.successData(res);
        }
        for(ServiceBriefInfo serviceBriefInfo : registryPageServiceResult.getData().getServiceBriefInfos()){
            InstanceVersionParam instanceVersionParam = new InstanceVersionParam();
            instanceVersionParam.setNamespaceId(namespaceId);
            instanceVersionParam.setServiceName(serviceBriefInfo.getServiceName());
            Result<PageService<ServiceInstance>> pageServiceResult = describeServiceInstance(instanceVersionParam);
            if(pageServiceResult.getData() == null || CollectionUtil.isEmpty(pageServiceResult.getData().getData())){
                continue;
            }
            HashSet<String> versions = new HashSet<>();
            for(ServiceInstance serviceInstance: pageServiceResult.getData().getData()){
                String version = serviceInstance.getAllMetadata().get(FEMAS_META_APPLICATION_VERSION_KEY);
                boolean notExist = versions.add(version);
                if(notExist){
                    ServiceVersion serviceVersion = new ServiceVersion(serviceBriefInfo.getServiceName(), version, namespaceId);
                    res.add(serviceVersion);
                }
            }
        }
        return Result.successData(res);
    }


}