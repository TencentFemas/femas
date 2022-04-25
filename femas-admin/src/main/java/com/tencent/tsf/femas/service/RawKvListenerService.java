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

package com.tencent.tsf.femas.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tsf.femas.common.util.BytesUtil;
import com.tencent.tsf.femas.storage.StorageResult;
import com.tencent.tsf.femas.storage.rocksdb.RocksDbKvStore;
import java.lang.reflect.Type;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/4/22 16:15
 */
public abstract class RawKvListenerService {

    final static ObjectMapper mapper = new ObjectMapper();
    private final RocksDbKvStore rocksDbKvStore = RocksDbKvStore.getInstance();

    protected <T> T getKvFromRocksDb(final String key, Type type) {
        StorageResult storageResult = rocksDbKvStore.get(BytesUtil.writeUtf8(key));
        if (storageResult.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)) {
            String data = (String) storageResult.getData();
            return (T) mapper.convertValue(data, type.getClass());
        }
        return null;
    }

    protected boolean putKv2RocksDb(final String key, final String value) {
        StorageResult storageResult = rocksDbKvStore.put(BytesUtil.writeUtf8(key), BytesUtil.writeUtf8(value));
        if (storageResult.getStatus().equalsIgnoreCase(StorageResult.SUCCESS)) {
            return (boolean) storageResult.getData();
        }
        return false;
    }

}
