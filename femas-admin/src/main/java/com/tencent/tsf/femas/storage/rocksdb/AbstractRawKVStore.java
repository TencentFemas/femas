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

import com.tencent.tsf.femas.storage.StorageResult;
import java.util.List;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/4/21 14:24
 */
public abstract class AbstractRawKVStore<T, C> implements RawKVStore, Lifecycle<C> {

    @Override
    public void init(C conf) throws Exception {

    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public StorageResult get(byte[] key) {
        return null;
    }

    @Override
    public StorageResult multiGet(List keys) {
        return null;
    }

    @Override
    public StorageResult containsKey(byte[] key) {
        return null;
    }

    @Override
    public StorageResult scan(byte[] startKey, byte[] endKey) {
        return null;
    }

    @Override
    public StorageResult getSequence(byte[] seqKey, int step) {
        return null;
    }

    @Override
    public StorageResult put(byte[] key, byte[] value) {
        return null;
    }

    @Override
    public StorageResult getAndPut(byte[] key, byte[] value) {
        return null;
    }

    @Override
    public StorageResult compareAndPut(byte[] key, byte[] expect, byte[] update) {
        return null;
    }

    @Override
    public StorageResult putIfAbsent(byte[] key, byte[] value) {
        return null;
    }

    @Override
    public StorageResult delete(byte[] key) {
        return null;
    }

    @Override
    public StorageResult deleteRange(byte[] startKey, byte[] endKey) {
        return null;
    }

    @Override
    public StorageResult deleteBatch(List keys) {
        return null;
    }

    abstract public StorageResult scanPrefix(final byte[] prefix);
}
