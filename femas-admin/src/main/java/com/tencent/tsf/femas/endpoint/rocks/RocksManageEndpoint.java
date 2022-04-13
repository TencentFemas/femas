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
package com.tencent.tsf.femas.endpoint.rocks;

import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.endpoint.adaptor.AbstractBaseEndpoint;
import com.tencent.tsf.femas.entity.rocks.RocksModel;
import com.tencent.tsf.femas.storage.StorageResult;
import com.tencent.tsf.femas.storage.config.RocksDbConditional;
import com.tencent.tsf.femas.storage.rocksdb.StringRawKVStoreManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Conditional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * @Author jolyonzheng
 * @Description
 * @Date 2021/12/28 下午3:11
 **/
@RestController
@RequestMapping("/atom/v1/rocks")
@Conditional(RocksDbConditional.class)
@Api(tags = "RocksDB内容管理模块")
public class RocksManageEndpoint extends AbstractBaseEndpoint {

    private final StringRawKVStoreManager kvStoreManager;

    public RocksManageEndpoint(StringRawKVStoreManager kvStoreManager) {
        this.kvStoreManager = kvStoreManager;
    }

    @GetMapping("fetchAllKey")
    @ApiOperation("查询RocksDB全部key")
    public Result<StorageResult<List<String>>> fetchAllKey(@RequestParam(required = false) String prefix){
        return StringUtils.isNotBlank(prefix)
                ? Result.successData(kvStoreManager.scanPrefixAllKey(prefix)) : Result.successData(kvStoreManager.scanPrefixAll());
    }

    @GetMapping("fetchAll")
    @ApiOperation("查询RocksDB全部数据")
    public Result<StorageResult<List<Map<String, String>>>> fetchAll(@RequestParam(required = false) String prefix){
        return StringUtils.isNotBlank(prefix)
                ? Result.successData(kvStoreManager.scanPrefixAll(prefix)) : Result.successData(kvStoreManager.scanAll());
    }

    @DeleteMapping("delete")
    @ApiOperation("通过key删除RocksDB数据")
    public Result delete(@RequestBody RocksModel rocksModel){
        if (StringUtils.isNotBlank(rocksModel.getKey())) {
            return Result.successData(kvStoreManager.delete(rocksModel.getKey()));
        }
        return Result.errorMessage("删除失败");
    }

    @PutMapping("put")
    @ApiOperation("创建/修改RocksDB数据")
    public Result modify(@RequestBody RocksModel rocksModel){
        if (StringUtils.isBlank(rocksModel.getKey()) || StringUtils.isBlank(rocksModel.getValue())) {
            return Result.errorMessage("编辑失败");
        }
        return Result.successData(kvStoreManager.put(rocksModel.getKey(), rocksModel.getValue()));
    }
}
