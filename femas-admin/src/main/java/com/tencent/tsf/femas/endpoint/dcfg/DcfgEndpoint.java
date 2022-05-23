package com.tencent.tsf.femas.endpoint.dcfg;

import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.endpoint.adaptor.AbstractBaseEndpoint;
import com.tencent.tsf.femas.entity.PageService;
import com.tencent.tsf.femas.entity.dcfg.Config;
import com.tencent.tsf.femas.entity.dcfg.ConfigRequest;
import com.tencent.tsf.femas.entity.dcfg.ConfigVersion;
import com.tencent.tsf.femas.service.dcfg.ConfigService;
import com.tencent.tsf.femas.service.dcfg.ConfigVersionService;
import com.tencent.tsf.femas.storage.DataOperation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 */
@RestController
@RequestMapping("atom/v1/dcfg")
@Api(tags = "配置模块")
public class DcfgEndpoint extends AbstractBaseEndpoint {

    private final DataOperation dataOperation;

    @Autowired
    ConfigService configService;

    public DcfgEndpoint(DataOperation dataOperation) {
        this.dataOperation = dataOperation;
    }

    @Autowired
    ConfigVersionService configVersionService;

    @PostMapping("configureConfig")
    @ApiOperation("创建或更新配置")
    public Result<Config> configureConfig(@RequestBody ConfigRequest configRequest) {
        return executor.process(()->configService.configureConfig(configRequest));
    }


    @PostMapping("deleteConfigs")
    @ApiOperation("删除配置")
    public Result<Config> deleteConfigs(@RequestBody ConfigRequest configRequest) {
        return executor.process(()->configService.deleteConfigs(configRequest));
    }


    @PostMapping("fetchConfigs")
    @ApiOperation("获取配置列表")
    public Result<PageService<Config>> fetchConfigs(@RequestBody ConfigRequest configRequest) {
        return executor.process(()->configService.fetchConfigs(configRequest));
    }

    @PostMapping("fetchConfigById")
    @ApiOperation("获取配置")
    public Result<Config> fetchConfigById(@RequestBody ConfigRequest configRequest) {
        return executor.process(()->configService.fetchConfigById(configRequest));
    }

    // version start
    @PostMapping("operateConfigVersion")
    @ApiOperation("操作配置版本")
    public Result<Config> operateConfigVersion(@RequestBody ConfigRequest configRequest) {
        return executor.process(()->configVersionService.operateConfigVersion(configRequest));
    }


    @PostMapping("fetchConfigVersions")
    @ApiOperation("查看配置版本")
    public Result<PageService<ConfigVersion>> fetchConfigVersions(@RequestBody ConfigRequest configRequest) {
        return executor.process(()->configVersionService.fetchConfigVersions(configRequest));
    }

    @PostMapping("deleteConfigVersions")
    @ApiOperation("删除版本")
    public Result<Config> deleteConfigVersions(@RequestBody ConfigRequest configRequest) {
        return executor.process(()->configVersionService.deleteConfigVersions(configRequest));
//        return executor.invoke(ServiceInvokeEnum.ApiInvokeEnum.DCFG_CONFIG_VERSION_DELETE, configRequest);
    }


}
