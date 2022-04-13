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

import static com.tencent.tsf.femas.constant.AdminConstants.ROCKSDB_COLUMN_FAMILY;
import static com.tencent.tsf.femas.constant.AdminConstants.ROCKSDB_DATA_PATH;
import static com.tencent.tsf.femas.storage.StorageResult.ERROR;
import static com.tencent.tsf.femas.storage.StorageResult.SUCCESS;

import com.tencent.tsf.femas.common.util.BytesUtil;
import com.tencent.tsf.femas.common.util.ErrorStackTraceUtil;
import com.tencent.tsf.femas.exception.RocksDbStorageException;
import com.tencent.tsf.femas.storage.StorageResult;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.DBOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksIterator;
import org.rocksdb.WriteOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 打开一个带有列族的RocksDB数据库
 * TODO 一个列族持有一个DB,集群间的数据同步
 *
 * @Author leoziltong@tencent.com
 * @Date: 2021/4/21 14:32
 */
//@Component
public class RocksDbKvStore extends AbstractRawKVStore<String, RocksDbConfig> {

    private static final Logger log = LoggerFactory.getLogger(RocksDbKvStore.class);
    private static volatile RocksDbKvStore rocksDbKvStore = null;

    static {
        RocksDB.loadLibrary();
    }

    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock definitionLock = this.readWriteLock.writeLock();
    private final Lock operateLock = this.readWriteLock.readLock();
    private final AtomicLong databaseVersion = new AtomicLong(0);
    private final RocksDbConfig config;
    private ColumnFamilyOptions cfOpts;
    private List<ColumnFamilyDescriptor> cfDescriptors;
    /**
     * 此处不能直接用List接收CF，必须明确每个CF
     */
    private ColumnFamilyHandle defaultHandle;
    private ColumnFamilyHandle atomConfigHandle;
    private RocksDB db;
    private String dbPath;
    private DBOptions options;
    private WriteOptions writeOptions;

    public RocksDbKvStore() throws Exception {
        this(RocksDbConfig.builder().path(ROCKSDB_DATA_PATH).build());
    }

    public RocksDbKvStore(RocksDbConfig config) throws Exception {
        this.config = config;
        init(config);
    }

    public static RocksDbKvStore getInstance() {
        if (rocksDbKvStore == null) {
            synchronized (RocksDbKvStore.class) {
                if (rocksDbKvStore == null) {
                    try {
                        rocksDbKvStore = new RocksDbKvStore(RocksDbConfig.builder().path(ROCKSDB_DATA_PATH).build());
                    } catch (Exception e) {

                    }
                }
            }
        }
        return rocksDbKvStore;
    }

    @Override
    public void init(final RocksDbConfig conf) throws Exception {
        definitionLock.lock();
        try {
            if (this.db != null) {
                log.info("[RocksDbKvStore] already started.");
                return;
            }
            cfOpts = new ColumnFamilyOptions().optimizeUniversalStyleCompaction();
            // list of column family descriptors, first entry must always be default column family
            cfDescriptors = Arrays.asList(
                    new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, cfOpts),
                    new ColumnFamilyDescriptor(ROCKSDB_COLUMN_FAMILY.getBytes(), cfOpts)
            );
            this.dbPath = conf.getDbPath();
            // a list which will hold the handles for the column families once the db is opened
            final List<ColumnFamilyHandle> columnFamilyHandleList =
                    new ArrayList<>();
            options = new DBOptions()
                    .setCreateIfMissing(true)
                    .setCreateMissingColumnFamilies(true);
            try {
                if (!Files.isSymbolicLink(Paths.get(dbPath))) {
                    Files.createDirectories(Paths.get(dbPath));
                }
            } catch (Exception e) {
                setCriticalError("create file dir failed,check your access right to that dir".concat(dbPath), e);
            }
            this.writeOptions = new WriteOptions();
            this.writeOptions.setSync(conf.isSync());
            // If `sync` is true, `disableWAL` must be set false.
            this.writeOptions.setDisableWAL(!conf.isSync() && conf.isDisableWAL());
            db = RocksDB.open(options,
                    dbPath, cfDescriptors,
                    columnFamilyHandleList);
            //org.rocksdb.RocksDBException: While lock file:XXX/rocksdb/femas/data//LOCK: Resource temporarily unavailable
            //This means that you ran two benchmarks at the same time on the same rocksdb directory. Only one RocksDB process can access a single database.
            this.defaultHandle = columnFamilyHandleList.get(0);
            this.atomConfigHandle = columnFamilyHandleList.get(1);
        } catch (Exception e) {
            log.error("Fail to open rocksDB at path {}, {}.", conf.getDbPath(), ErrorStackTraceUtil.getStackTrace(e));
        } finally {
            JVMShutdownHook jvmShutdownHook = new JVMShutdownHook();
            Runtime.getRuntime().addShutdownHook(jvmShutdownHook);
            definitionLock.unlock();
        }
    }


    @Override
    public StorageResult get(final byte[] key) {
        operateLock.lock();
        try {
            final byte[] value = this.db.get(key);
            return setSuccess(BytesUtil.readUtf8(value));
        } catch (final Exception e) {
            log.error("Fail to [GET], key: [{}], {}.", BytesUtil.readUtf8(key), ErrorStackTraceUtil.getStackTrace(e));
            return setFailure("Fail to [GET]");
        } finally {
            operateLock.unlock();
        }
    }

    @Override
    public StorageResult scanPrefix(final byte[] prefix) {
        operateLock.lock();
        try {
            List<String> result = new ArrayList<>();
            RocksIterator iterator = db.newIterator();
            for (iterator.seek(prefix); iterator.isValid(); iterator
                    .next()) {
                String key = BytesUtil.readUtf8(iterator.key());
                if (!key.startsWith(BytesUtil.readUtf8(prefix))) {
                    break;
                }
                result.add(String.format("%s", BytesUtil.readUtf8(iterator.key())));
            }
            return setSuccess(result);
        } catch (final Exception e) {
            log.error("Fail to [GET], key: [{}], {}.", BytesUtil.readUtf8(prefix),
                    ErrorStackTraceUtil.getStackTrace(e));
            return setFailure("Fail to [GET]");
        } finally {
            operateLock.unlock();
        }
    }

    public StorageResult scanPrefixValue(final byte[] prefix) {
        operateLock.lock();
        try {
            List<String> result = new ArrayList<>();
            RocksIterator iterator = db.newIterator();
            for (iterator.seek(prefix); iterator.isValid(); iterator
                    .next()) {
                String key = BytesUtil.readUtf8(iterator.key());
                if (!key.startsWith(BytesUtil.readUtf8(prefix))) {
                    break;
                }
                result.add(String.format("%s", BytesUtil.readUtf8(db.get(iterator.key()))));
            }
            return setSuccess(result);
        } catch (final Exception e) {
            log.error("Fail to [GET], key: [{}], {}.", BytesUtil.readUtf8(prefix),
                    ErrorStackTraceUtil.getStackTrace(e));
            return setFailure("Fail to [GET]");
        } finally {
            operateLock.unlock();
        }
    }

    public StorageResult scanAll() {
        operateLock.lock();
        try {
            List<Map<String, String>> result = new ArrayList<>();
            RocksIterator iterator = db.newIterator();
            for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                Map<String, String> map = new HashMap<>();
                String key = BytesUtil.readUtf8(iterator.key());
                map.put(key, String.format("%s", BytesUtil.readUtf8(db.get(iterator.key()))));
                result.add(map);
            }
            return setSuccess(result);
        } catch (final Exception e) {
            log.error("Fail to [GET], {}.", ErrorStackTraceUtil.getStackTrace(e));
            return setFailure("Fail to [GET]");
        } finally {
            operateLock.unlock();
        }
    }

    public StorageResult scanPrefixAll(final byte[] prefix) {
        operateLock.lock();
        try {
            List<Map<String, String>> result = new ArrayList<>();
            RocksIterator iterator = db.newIterator();
            for (iterator.seek(prefix); iterator.isValid(); iterator.next()) {
                Map<String, String> map = new HashMap<>();
                String key = BytesUtil.readUtf8(iterator.key());
                if (!key.startsWith(BytesUtil.readUtf8(prefix))) {
                    break;
                }
                map.put(key, String.format("%s", BytesUtil.readUtf8(db.get(iterator.key()))));
                result.add(map);
            }
            return setSuccess(result);
        } catch (final Exception e) {
            log.error("Fail to [GET], {}.", ErrorStackTraceUtil.getStackTrace(e));
            return setFailure("Fail to [GET]");
        } finally {
            operateLock.unlock();
        }
    }

    public StorageResult scanAllKey() {
        operateLock.lock();
        try {
            List<String> result = new ArrayList<>();
            RocksIterator iterator = db.newIterator();
            for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                String key = BytesUtil.readUtf8(iterator.key());
                result.add(key);
            }
            return setSuccess(result);
        } catch (final Exception e) {
            log.error("Fail to [GET], {}.", ErrorStackTraceUtil.getStackTrace(e));
            return setFailure("Fail to [GET]");
        } finally {
            operateLock.unlock();
        }
    }

    public StorageResult scanPrefixAllKey(final byte[] prefix) {
        operateLock.lock();
        try {
            List<String> result = new ArrayList<>();
            RocksIterator iterator = db.newIterator();
            for (iterator.seek(prefix); iterator.isValid(); iterator.next()) {
                String key = BytesUtil.readUtf8(iterator.key());
                if (!key.startsWith(BytesUtil.readUtf8(prefix))) {
                    break;
                }
                result.add(key);
            }
            return setSuccess(result);
        } catch (final Exception e) {
            log.error("Fail to [GET], {}.", ErrorStackTraceUtil.getStackTrace(e));
            return setFailure("Fail to [GET]");
        } finally {
            operateLock.unlock();
        }
    }

    @Override
    public StorageResult multiGet(List keys) {
        return super.multiGet(keys);
    }

    @Override
    public StorageResult containsKey(byte[] key) {
        this.operateLock.lock();
        try {
            boolean exists = false;
            if (this.db.keyMayExist(key, new StringBuilder(0))) {
                exists = this.db.get(key) != null;
            }
            return setSuccess(exists);
        } catch (final Exception e) {
            log.error("Fail to [CONTAINS_KEY], key: [{}], {}.", BytesUtil.readUtf8(key),
                    ErrorStackTraceUtil.getStackTrace(e));
            return setFailure("Fail to [CONTAINS_KEY]");
        } finally {
            operateLock.unlock();
        }
    }

    @Override
    public StorageResult scan(byte[] startKey, byte[] endKey) {
        return super.scan(startKey, endKey);
    }

    @Override
    public StorageResult put(byte[] key, byte[] value) {
        operateLock.lock();
        try {
            this.db.put(this.writeOptions, key, value);
            return setSuccess(Boolean.TRUE);
        } catch (final Exception e) {
            log.error("Fail to [put], key: [{}], {}.", BytesUtil.readUtf8(key), ErrorStackTraceUtil.getStackTrace(e));
            return setFailure("Fail to [PUT]");
        } finally {
            operateLock.unlock();
        }
    }

    @Override
    public StorageResult putIfAbsent(byte[] key, byte[] value) {
        operateLock.lock();
        try {
            final byte[] prevVal = this.db.get(key);
            if (prevVal == null) {
                this.db.put(this.writeOptions, key, value);
            }
            return setSuccess(BytesUtil.readUtf8(prevVal));
        } catch (final Exception e) {
            log.error("Fail to [PUT_IF_ABSENT], [{}, {}], {}.", BytesUtil.readUtf8(key), BytesUtil.readUtf8(value),
                    ErrorStackTraceUtil.getStackTrace(e));
            return setFailure("Fail to [PUT_IF_ABSENT]");
        } finally {
            operateLock.unlock();
        }
    }

    @Override
    public StorageResult delete(byte[] key) {
        operateLock.lock();
        try {
            this.db.delete(this.writeOptions, key);
            return setSuccess(Boolean.TRUE);
        } catch (final Exception e) {
            log.error("Fail to [DELETE], [{}], {}.", BytesUtil.readUtf8(key), ErrorStackTraceUtil.getStackTrace(e));
            return setFailure("Fail to [DELETE]");
        } finally {
            operateLock.unlock();
        }
    }

    private StorageResult setSuccess(Object data) {
        return StorageResult.builder().data(data).status(SUCCESS).build();
    }

    private StorageResult setFailure(String msg) {
        return StorageResult.builder().error(msg).status(ERROR).build();
    }

    private void setCriticalError(final String message, final Throwable error) {
        if (error != null) {
            throw new RocksDbStorageException(message, error);
        }
    }


    /**
     * release resource and help gc
     *
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        definitionLock.lock();
        try {
            if (this.db == null) {
                return;
            }
            closeRocksDB();
            this.isShutdown.compareAndSet(false, true);
            if (this.defaultHandle != null) {
                this.defaultHandle.close();
                this.defaultHandle = null;
            }
            if (this.atomConfigHandle != null) {
                this.atomConfigHandle.close();
                this.atomConfigHandle = null;
            }
            if (cfOpts != null) {
                cfOpts.close();
                this.cfOpts = null;
            }
//            this.cfDescriptors.clear();
            if (this.options != null) {
                this.options.close();
                this.options = null;
            }
            if (this.writeOptions != null) {
                this.writeOptions.close();
                this.writeOptions = null;
            }
        } finally {
            definitionLock.unlock();
            log.info("[RocksDbKvStore] shutdown successfully.");
        }
    }

    private void closeRocksDB() {
        if (this.db != null) {
            this.db.close();
            this.db = null;
        }
    }

    private class JVMShutdownHook extends Thread {

        public void run() {
            try {
                close();
                log.info("JVMShutdownHook close resource success...");
            } catch (Exception e) {
                log.error("JVMShutdownHook close resource failed", e);
            }
        }
    }

}
