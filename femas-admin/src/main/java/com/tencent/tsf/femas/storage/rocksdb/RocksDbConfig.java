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

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/4/21 14:36
 */
public class RocksDbConfig {

    private boolean fastSnapshot = false;

    private boolean asyncSnapshot = false;
    private boolean sync = false;

    private boolean disableWAL = true;
    private String dbPath;

    public static RocksDbConfig.RocksDbConfigBuilder builder() {
        return new RocksDbConfig.RocksDbConfigBuilder();
    }

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    public boolean isDisableWAL() {
        return disableWAL;
    }

    public void setDisableWAL(boolean disableWAL) {
        this.disableWAL = disableWAL;
    }

    public boolean isFastSnapshot() {
        return fastSnapshot;
    }

    public void setFastSnapshot(boolean fastSnapshot) {
        this.fastSnapshot = fastSnapshot;
    }

    public boolean isAsyncSnapshot() {
        return asyncSnapshot;
    }

    public void setAsyncSnapshot(boolean asyncSnapshot) {
        this.asyncSnapshot = asyncSnapshot;
    }

    public String getDbPath() {
        return dbPath;
    }

    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

    @Override
    public String toString() {
        return "RocksDbConfig{" + "fastSnapshot=" + fastSnapshot
                + ", asyncSnapshot=" + asyncSnapshot + ", dbPath='" + dbPath
                + '\'' + '}';
    }

    public static final class RocksDbConfigBuilder {

        private String path;

        private RocksDbConfigBuilder() {
        }

        public RocksDbConfig.RocksDbConfigBuilder path(String path) {
            this.path = path;
            return this;
        }

        public RocksDbConfig build() {
            RocksDbConfig config = new RocksDbConfig();
            config.setDbPath(path);
            return config;
        }
    }

}
