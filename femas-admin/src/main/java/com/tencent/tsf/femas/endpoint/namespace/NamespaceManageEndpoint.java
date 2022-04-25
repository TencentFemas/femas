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
package com.tencent.tsf.femas.endpoint.namespace;

import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.endpoint.adaptor.AbstractBaseEndpoint;
import com.tencent.tsf.femas.entity.PageService;
import com.tencent.tsf.femas.entity.namespace.*;
import com.tencent.tsf.femas.enums.ServiceInvokeEnum;
import io.swagger.annotations.Api;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;



/**
 * @Auther: yrz
 * @Date: 2021/05/08/15:57
 * @Descriptioin
 */
@RestController
@RequestMapping("/atom/v1/namespace")
@Api(tags = "命名空间模块")
public class NamespaceManageEndpoint extends AbstractBaseEndpoint {

    @PostMapping("fetchNamespaces")
    @ApiOperation("查询命名空间列表")
    public Result<PageService<NamespaceVo>> fetchNamespaces(@RequestBody NamespacePageModel pageModel){
        return executor.invoke(ServiceInvokeEnum.ApiInvokeEnum.NAMESPACE_MANGER_FETCH,pageModel);
    }

    @PostMapping("deleteNamespace")
    @ApiOperation("通过命名空间的id删除命名空间")
    public Result deleteNamespace(@RequestBody NamespaceIdModel id){
        return executor.invoke(ServiceInvokeEnum.ApiInvokeEnum.NAMESPACE_MANGER_DELETE,id.getNamespaceId());
    }

    @PostMapping("fetchNamespaceById")
    @ApiOperation("通过命名空间id查询命名空间列表")
    public Result<Namespace> fetchNamespaceById(@RequestBody NamespaceIdModel idModel){
        return executor.invoke(ServiceInvokeEnum.ApiInvokeEnum.NAMESPACE_MANGER_FETCH_BY_ID,idModel.getNamespaceId());
    }

    @PostMapping("modifyNamespace")
    @ApiOperation("修改命名空间 id必传")
    public Result modifyNamespace(@RequestBody Namespace namespace){
        return executor.invoke(ServiceInvokeEnum.ApiInvokeEnum.NAMESPACE_MANGER_MODIFY,namespace);
    }

    @PostMapping("createNamespace")
    @ApiOperation("创建命名空间")
    public Result createNamespace(@RequestBody Namespace namespace){
        return executor.invoke(ServiceInvokeEnum.ApiInvokeEnum.NAMESPACE_MANGER_CREATE,namespace);
    }
}
