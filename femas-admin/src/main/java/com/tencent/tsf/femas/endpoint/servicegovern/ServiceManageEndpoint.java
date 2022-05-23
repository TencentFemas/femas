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

package com.tencent.tsf.femas.endpoint.servicegovern;

import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.endpoint.adaptor.AbstractBaseEndpoint;
import com.tencent.tsf.femas.entity.KVEntity;
import com.tencent.tsf.femas.entity.PageService;
import com.tencent.tsf.femas.entity.param.InstanceVersionParam;
import com.tencent.tsf.femas.entity.param.RegistryInstanceParam;
import com.tencent.tsf.femas.entity.param.RegistryServiceParam;
import com.tencent.tsf.femas.entity.registry.ApiModel;
import com.tencent.tsf.femas.entity.registry.RegistryPageService;
import com.tencent.tsf.femas.entity.registry.ServiceApi;
import com.tencent.tsf.femas.entity.registry.ServiceOverview;
import com.tencent.tsf.femas.entity.service.EventTypeEnum;
import com.tencent.tsf.femas.entity.service.ServiceEventModel;
import com.tencent.tsf.femas.entity.service.ServiceEventView;
import com.tencent.tsf.femas.service.registry.RegistryManagerService;
import com.tencent.tsf.femas.service.registry.ServiceManagerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/5/7 15:02
 */
@Slf4j
@RestController
@RequestMapping("/atom/v1/service")
@Api(value = "/atom/v1/service", tags = "服务信息模块")
public class ServiceManageEndpoint extends AbstractBaseEndpoint {

    private final ServiceManagerService serviceManagerService;

    @Autowired
    RegistryManagerService registryManagerService;


    public ServiceManageEndpoint(ServiceManagerService serviceManagerService) {
        this.serviceManagerService = serviceManagerService;
    }

    @ApiOperation(value = "获取服务列表", notes = "根据命名空间获取服务列表")
    @PostMapping("/describeRegisterService")
    public Result<RegistryPageService> describeRegisterService(@RequestBody RegistryServiceParam param) {
        return executor.process(()->registryManagerService.describeRegisterService(param.getNamespaceId(),
                param.getStatus(), param.getPageNo() == null ? 1 : param.getPageNo(),
                param.getPageSize() == null ? 10 : param.getPageSize(), param.getKeyword()));
    }

    @ApiOperation(value = "获取服务实例列表", notes = "根据命名空间和服务名获取服务实例列表")
    @PostMapping("/describeServiceInstance")
    public Result<PageService<ServiceInstance>> describeServiceInstance(@RequestBody InstanceVersionParam param) {
        return executor.process(() -> serviceManagerService.describeServiceInstance(param));
    }

    @ApiOperation(value = "获取服务概览", notes = "通过命名空间和服务名查询服务概览")
    @PostMapping("/describeServiceOverview")
    public Result<ServiceOverview> describeServiceOverview(@RequestBody RegistryInstanceParam param) {
        return executor.process(() -> serviceManagerService.describeServiceOverview(param));
    }

    @ApiOperation(value = "获取服务接口", notes = "获取服务接口")
    @PostMapping("/describeServiceApi")
    public Result<PageService<ServiceApi>> describeServiceApi(@RequestBody ApiModel apiModel) {
        return executor.process(() -> serviceManagerService.describeServiceApi(apiModel));
    }

    @ApiOperation(value = "获取服务事件", notes = "获取服务事件")
    @PostMapping("/describeServiceEvent")
    public Result<PageService<ServiceEventView>> describeServiceEvent(
            @RequestBody ServiceEventModel serviceEventModel) {
        return executor.process(() -> serviceManagerService.describeServiceEvent(serviceEventModel));
    }

    @RequestMapping("fetchEventType")
    public Result<List<KVEntity>> fetchEventType() {
        List<KVEntity> modules = new ArrayList<>();
        for (EventTypeEnum value : EventTypeEnum.values()) {
            modules.add(new KVEntity(value.getName(), value.name()));
        }
        return Result.successData(modules);
    }
}
