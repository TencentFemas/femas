package com.tencent.tsf.femas.service.dcfg;

import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.entity.PageService;
import com.tencent.tsf.femas.entity.dcfg.Config;
import com.tencent.tsf.femas.entity.dcfg.ConfigRequest;
import com.tencent.tsf.femas.entity.namespace.Namespace;
import com.tencent.tsf.femas.service.ServiceExecutor;
import com.tencent.tsf.femas.service.namespace.NamespaceMangerService;
import com.tencent.tsf.femas.storage.DataOperation;
import com.tencent.tsf.femas.util.ResultCheck;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author jianzhi
 * @date 2021/8/16 19:10
 */
@Service
public class ConfigService implements ServiceExecutor {

    private final DataOperation dataOperation;
    @Autowired
    private ConfigVersionService configVersionService;
    @Autowired
    private NamespaceMangerService namespaceMangerService;

    public ConfigService(DataOperation dataOperation) {
        this.dataOperation = dataOperation;
    }

    public Result configureConfig(ConfigRequest configRequest) {
        Result valid = configVersionService
                .validConfigValue(configRequest.getConfigType(), configRequest.getConfigValue());
        if (!Result.SUCCESS.equals(valid.getCode())) {
            return valid;
        }

        Config config = new Config();
        BeanUtils.copyProperties(configRequest, config);
        if (StringUtils.isNotEmpty(configRequest.getConfigId())) { // 这是前端来修改desc的
            config = fetchConfigById(configRequest).getData();
            config.setConfigDesc(configRequest.getConfigDesc());
        }
        int success = dataOperation.configureDcfg(config);

        if (ResultCheck.checkCount(success)) {
            if (StringUtils.isNotEmpty(configRequest.getConfigValue())) { // 修改config
                configRequest.setConfigId(config.getConfigId());
                configVersionService.configureConfigVersion(configRequest);
            }
            return Result.successMessage(config.getConfigId());
        }
        return Result.errorMessage("操作失败");
    }

    public Result<PageService<Config>> fetchConfigs(ConfigRequest configRequest) {
        PageService<Config> pageConfig = dataOperation.fetchDcfgs(configRequest);
        List<String> configIdList = pageConfig.getData().stream().map(Config::getConfigId).collect(Collectors.toList());
        List<Config> dcfgOther = dataOperation.fetchDcfgOther(configIdList);

        Result<Namespace> namespaceResult = namespaceMangerService.fetchNamespaceById(configRequest.getNamespaceId());
        String namespaceName = namespaceResult.getData() == null ? null : namespaceResult.getData().getName();

        // fullfill
        pageConfig.getData().stream().forEach(config -> {
            config.setNamespaceName(namespaceName);
            config.setLastReleaseVersion(
                    configVersionService.fetchConfigVersionById(config.getConfigId(), config.getLastReleaseVersionId())
                            .getData());
            config.setCurrentReleaseVersion(configVersionService
                    .fetchConfigVersionById(config.getConfigId(), config.getCurrentReleaseVersionId()).getData());
            dcfgOther.forEach(o -> {
                if (config.getConfigId().equals(o.getConfigId())) {
                    config.setVersionCount(o.getVersionCount());
                }
            });
        });

        return Result.successData(pageConfig);
    }

    public Result<Config> fetchConfigById(ConfigRequest configRequest) {
        Result<PageService<Config>> page = fetchConfigs(configRequest);
        if (CollectionUtil.isEmpty(page.getData().getData())) {
            return Result.errorMessage("无相关数据");
        }
        return Result.successData(page.getData().getData().get(0));
    }


    public Result deleteConfigs(ConfigRequest configRequest) {
        if (!CollectionUtils.isEmpty(configRequest.getConfigIdList())) {
            configRequest.getConfigIdList().forEach(configId -> {
                dataOperation.deleteDcfg(configId, configRequest.getNamespaceId());
            });
        }
        if (StringUtils.isNotEmpty(configRequest.getConfigId())) {
            dataOperation.deleteDcfg(configRequest.getConfigId(), configRequest.getNamespaceId());
        }
        return Result.successMessage("操作成功");
    }
}
