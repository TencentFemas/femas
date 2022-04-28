package com.tencent.tsf.femas.service.dcfg;

import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.config.FemasConfigManagerFactory;
import com.tencent.tsf.femas.constant.DcfgConstants;
import com.tencent.tsf.femas.entity.PageService;
import com.tencent.tsf.femas.entity.dcfg.Config;
import com.tencent.tsf.femas.entity.dcfg.ConfigRequest;
import com.tencent.tsf.femas.entity.dcfg.ConfigVersion;
import com.tencent.tsf.femas.entity.namespace.Namespace;
import com.tencent.tsf.femas.entity.registry.RegistryConfig;
import com.tencent.tsf.femas.service.ServiceExecutor;
import com.tencent.tsf.femas.service.registry.RegistryManagerService;
import com.tencent.tsf.femas.storage.DataOperation;
import com.tencent.tsf.femas.util.ResultCheck;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.scanner.ScannerException;


/**
 * @author jianzhi
 * @date 2021/8/16 19:10
 */
@Service
public class ConfigVersionService implements ServiceExecutor {

    private static final Logger log = LoggerFactory.getLogger(ConfigVersionService.class);

    private final DataOperation dataOperation;

    @Autowired
    private ConfigService configService;

    @Autowired
    private RegistryManagerService registryManagerService;

    public ConfigVersionService(DataOperation dataOperation) {
        this.dataOperation = dataOperation;
    }

    public Result<String> configureConfigVersion(ConfigRequest configRequest) {
        ConfigVersion configVersion = new ConfigVersion();
        BeanUtils.copyProperties(configRequest, configVersion);

        // 校验内容
        Config config = configService.fetchConfigById(configRequest).getData();
        Result valid = validConfigValue(config.getConfigType(), configVersion.getConfigValue());
        if (!Result.SUCCESS.equals(valid.getCode())) {
            return valid;
        }

        configRequest.setPageSize(Integer.MAX_VALUE);
        Result<PageService<ConfigVersion>> page = fetchConfigVersions(configRequest);
        int version = 1;
        if (!CollectionUtil.isEmpty(page.getData().getData())) {
            version = page.getData().getData().stream().map(ConfigVersion::getConfigVersion).max(Integer::compare).get()
                    + 1;
        }
        configVersion.setConfigVersion(version);
        configVersion.setReleaseStatus(DcfgConstants.RELEASE_STATUS.UN_RELEASE);
        if (ResultCheck.checkCount(dataOperation.configureDcfgVersion(configVersion))) {
            return Result.successData(configVersion.getConfigVersionId());
        }
        return Result.errorMessage("操作失败");
    }

    public Result<PageService<ConfigVersion>> fetchConfigVersions(ConfigRequest configRequest) {
        return Result.successData(dataOperation.fetchDcfgVersions(configRequest));
    }


    public Result deleteConfigVersions(ConfigRequest configRequest) {
        if (!CollectionUtils.isEmpty(configRequest.getConfigVersionIdList())) {
            configRequest.getConfigVersionIdList().forEach(configVersionId -> {
                dataOperation.deleteDcfgVersion(configVersionId, configRequest.getConfigId());
            });
        }
        if (StringUtils.isNotEmpty(configRequest.getConfigVersionId())) {
            dataOperation.deleteDcfgVersion(configRequest.getConfigVersionId(), configRequest.getConfigId());
        }
        return Result.successMessage("操作成功");
    }


    public Result<ConfigVersion> fetchConfigVersionById(String configId, String configVersionId) {
        if (StringUtils.isEmpty(configId) || StringUtils.isEmpty(configVersionId)) {
            return Result.successData(null);
        }
        ConfigRequest configRequest = new ConfigRequest();
        configRequest.setConfigId(configId);
        configRequest.setConfigVersionId(configVersionId);
        PageService<ConfigVersion> page = dataOperation.fetchDcfgVersions(configRequest);
        List<ConfigVersion> data = page.getData();
        if (CollectionUtil.isEmpty(data)) {
            return Result.successData(null);
        }
        return Result.successData(data.get(0));
    }

    public Result operateConfigVersion(ConfigRequest configRequest) {
        String command = configRequest.getCommand();
        if ("create".equals(command)) {
            configRequest.setConfigVersionId(null);
            return configureConfigVersion(configRequest);
        }
        if ("release".equals(command)) {
            return releaseConfigVersion(configRequest);
        }
        if ("rollback".equals(command)) {
            return rollbackConfigVersion(configRequest);
        }
        return Result.errorMessage("操作失败");
    }

    public Result releaseConfigVersion(ConfigRequest configRequest) {
        Config config = configService.fetchConfigById(configRequest).getData();
        ConfigVersion releaseVersion = fetchConfigVersionById(configRequest.getConfigId(),
                configRequest.getConfigVersionId()).getData();
        if (releaseVersion == null) {
            return Result.errorMessage("操作失败，不存在该版本");
        }
        ConfigVersion lastVersion = fetchConfigVersionById(configRequest.getConfigId(),
                config.getCurrentReleaseVersionId()).getData();
        if (releaseFemasConfig(config, lastVersion, releaseVersion, false)) {
            return Result.successMessage("操作成功");
        }
        return Result.errorMessage("操作失败");
    }


    public Result rollbackConfigVersion(ConfigRequest configRequest) {
        Config config = configService.fetchConfigById(configRequest).getData();
        // 当前版本改为上一版本
        ConfigVersion lastVersion = fetchConfigVersionById(configRequest.getConfigId(),
                config.getCurrentReleaseVersionId()).getData();
        // 上一版本改为当前版本
        ConfigVersion releaseVersion = fetchConfigVersionById(configRequest.getConfigId(),
                config.getLastReleaseVersionId()).getData();
        // 重新生成个releaseVersion
        ConfigRequest versionReq = new ConfigRequest();
        versionReq.setConfigId(releaseVersion.getConfigId());
        versionReq.setConfigValue(releaseVersion.getConfigValue());
        versionReq.setNamespaceId(configRequest.getNamespaceId());
        String configVersionId = configureConfigVersion(versionReq).getData();
        releaseVersion = fetchConfigVersionById(releaseVersion.getConfigId(), configVersionId).getData();

        if (releaseFemasConfig(config, lastVersion, releaseVersion, true)) {
            return Result.successMessage("操作成功");
        }
        return Result.errorMessage("操作失败");
    }


    public boolean releaseFemasConfig(Config config, ConfigVersion lastVersion, ConfigVersion releaseVersion,
            boolean rollback) {
        Object[] params = null;
        try {
            Namespace namespace = dataOperation.fetchNamespaceById(config.getNamespaceId());
            if (CollectionUtils.isEmpty(namespace.getRegistryId())) {
                return false;
            }
            String namespaceId = namespace.getRegistryId().get(0);
            if (StringUtils.isBlank(namespaceId)) {
                return false;
            }
            RegistryConfig registryConfig = registryManagerService.getConfigById(namespaceId);
            params = new Object[]{config.getNamespaceId(), config.getConfigId(), config.getSystemTag(),
                    releaseVersion.getConfigValue(), config.getConfigType(), config.getServiceName(),
                    registryConfig.getRegistryCluster(),registryConfig.getUsername(),registryConfig.getPassword()};

            String paramStr = Arrays.toString(params);
            log.info("release femasConfig requestParam: {}", paramStr);

            com.tencent.tsf.femas.config.Config<Object> atomConfig = FemasConfigManagerFactory
                    .getConfigManagerInstance().getConfig();
            if (!atomConfig.publishConfig(params)) {
                log.warn("release femasConfig return false, requestParam:{}", paramStr);
                return false;
            }

            // update last releaseVersion
            if (lastVersion != null) {
                lastVersion.setReleaseStatus(DcfgConstants.RELEASE_STATUS.RELEASE_SUCCESS);
                dataOperation.configureDcfgVersion(lastVersion);
            }

            // update releaseVersion
            long releaseTime = System.currentTimeMillis();
            releaseVersion.setReleaseTime(releaseTime);
            releaseVersion.setReleaseStatus(DcfgConstants.RELEASE_STATUS.RELEASE_VALID);
            dataOperation.configureDcfgVersion(releaseVersion);

            // update config
            config.setLastReleaseVersionId((rollback || lastVersion == null) ? null : lastVersion.getConfigVersionId());
            config.setCurrentReleaseVersionId(releaseVersion.getConfigVersionId());
            config.setReleaseTime(releaseTime);
            dataOperation.configureDcfg(config);

            return true;
        } catch (Exception e) {
            log.error("releaseNacos error, requestParam:{}", params, e);
            return false;
        }
    }


    public Result validConfigValue(String type, String configValue) {
        if ("properties".equals(type)) {
            try {
                Properties props = new Properties();
                props.load(new ByteArrayInputStream(configValue.getBytes()));
            } catch (Exception e) {
                return Result.errorData("格式校验失败", "formatError");
            }
        } else if ("yaml".equals(type)) {
            try {
                Yaml yaml = new Yaml();
                yaml.loadAs(configValue, LinkedHashMap.class);
            } catch (ScannerException e) {
                return Result.errorData("格式校验失败:" + e.getMessage(), "formatError");
            } catch (Exception e) {
                return Result.errorData("格式校验失败", "formatError");
            }
        }
        return Result.success();
    }
}
