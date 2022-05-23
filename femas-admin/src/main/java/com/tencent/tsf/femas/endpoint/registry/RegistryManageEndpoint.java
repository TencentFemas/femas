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

package com.tencent.tsf.femas.endpoint.registry;

import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.endpoint.adaptor.AbstractBaseEndpoint;
import com.tencent.tsf.femas.entity.registry.RegistryConfig;
import com.tencent.tsf.femas.entity.registry.RegistryIdModel;
import com.tencent.tsf.femas.entity.registry.RegistryInfo;
import com.tencent.tsf.femas.entity.registry.RegistryModel;
import com.tencent.tsf.femas.entity.registry.RegistrySearch;
import com.tencent.tsf.femas.service.registry.RegistryManagerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/4/27 17:24
 */
@RestController
@RequestMapping("/atom/v1/registry")
@Api(value = "/atom/v1/registry", tags = "注册中心操作")
public class RegistryManageEndpoint extends AbstractBaseEndpoint {

    @Resource
    RegistryManagerService registryManageService;

    @ApiOperation(value = "配置注册中心")
    @PostMapping("/configureRegistry")
    public Result<Boolean> configureRegistry(@RequestBody RegistryModel registryModel) {
        return executor.process(()->registryManageService.configureRegistry(registryModel));
    }

    @ApiOperation(value = "验证k8s认证配置")
    @PostMapping("/checkCertificateConf")
    public Result<Boolean> checkCertificateConf(@RequestBody RegistryModel registryModel) {
        return executor.process(()->registryManageService.checkCertificateConf(registryModel));
    }

    @ApiOperation(value = "获取注册中心列表")
    @PostMapping("/describeRegistryClusters")
    public Result<List<RegistryConfig>> describeRegistryClusters(@RequestBody RegistrySearch registrySearch) {
        return executor.process(()->registryManageService.describeRegistryClusters(registrySearch));
    }

    @ApiOperation(value = "删除注册中心")
    @PostMapping("/deleteRegistryCluster")
    public Result deleteRegistryCluster(@RequestBody RegistryIdModel idModel) {
        return executor.process(()->registryManageService.deleteRegistryCluster(idModel.getRegistryId()));
    }

    @ApiOperation(value = "获取注册中心集群信息")
    @PostMapping("/describeRegistryCluster")
    public Result<RegistryInfo> describeRegistryCluster(@RequestBody RegistryIdModel idModel) {
        return executor.process(()->registryManageService.describeRegistryCluster(idModel.getRegistryId()));
    }

}
