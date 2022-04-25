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

package com.tencent.tsf.femas.storage.rocksdb;

import com.tencent.tsf.femas.common.util.BytesUtil;
import com.tencent.tsf.femas.storage.StorageResult;
import java.util.List;
import java.util.Map;

import com.tencent.tsf.femas.storage.config.RocksDbConditional;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/4/27 15:22
 */
@Component
@Conditional(RocksDbConditional.class)
public class StringRawKVStoreManager {


    private final RocksDbKvStore store;

    public StringRawKVStoreManager() {
        store = RocksDbKvStore.getInstance();
    }

    public StorageResult<String> get(String key) {
        return store.get(BytesUtil.writeUtf8(key));
    }

    public StorageResult<List<String>> scanPrefix(String prefix) {
        return store.scanPrefix(BytesUtil.writeUtf8(prefix));
    }

    public StorageResult<List<String>> scanPrefixValue(String prefix) {
        return store.scanPrefixValue(BytesUtil.writeUtf8(prefix));
    }

    public StorageResult<List<Map<String, String>>> scanAll() {
        return store.scanAll();
    }

    public StorageResult<List<Map<String, String>>> scanPrefixAll(String prefix) {
        return store.scanPrefixAll(BytesUtil.writeUtf8(prefix));
    }

    public StorageResult containsKey(String key) {
        return store.containsKey(BytesUtil.writeUtf8(key));
    }


    public StorageResult put(String key, String value) {
        return store.put(BytesUtil.writeUtf8(key), BytesUtil.writeUtf8(value));
    }


    public StorageResult putIfAbsent(String key, String value) {
        return store.putIfAbsent(BytesUtil.writeUtf8(key), BytesUtil.writeUtf8(value));
    }


    public StorageResult delete(String key) {
        return store.delete(BytesUtil.writeUtf8(key));
    }

    public StorageResult<List<String>> scanPrefixAllKey(String prefix) {
        return store.scanPrefixAllKey(BytesUtil.writeUtf8(prefix));
    }

    public StorageResult<List<String>> scanPrefixAll() {
        return store.scanAllKey();
    }
}
