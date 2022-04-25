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
 * @Date: 2021/4/19 17:28
 */
public interface RawKVStore<T> {

    StorageResult<T> get(final byte[] key);

    StorageResult<T> multiGet(final List<byte[]> keys);

    StorageResult containsKey(final byte[] key);

    StorageResult<T> scan(final byte[] startKey, final byte[] endKey);

    StorageResult<T> getSequence(final byte[] seqKey, final int step);

    StorageResult<T> put(final byte[] key, final byte[] value);

    StorageResult<T> getAndPut(final byte[] key, final byte[] value);

    StorageResult<T> compareAndPut(final byte[] key, final byte[] expect, final byte[] update);

    StorageResult<T> putIfAbsent(final byte[] key, final byte[] value);

    StorageResult<T> delete(final byte[] key);

    StorageResult<T> deleteRange(final byte[] startKey, final byte[] endKey);

    StorageResult<T> deleteBatch(final List<byte[]> keys);


}
