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

package com.tencent.tsf.femas.service.registry;

import static com.tencent.tsf.femas.constant.AdminConstants.HTTP_PREFIX;
import static com.tencent.tsf.femas.entity.registry.RegistryConfig.REGISTRY_HOST;
import static com.tencent.tsf.femas.entity.registry.RegistryConfig.REGISTRY_PORT;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.httpclient.ApacheHttpClientHolder;
import com.tencent.tsf.femas.common.httpclient.client.AbstractHttpClient;
import com.tencent.tsf.femas.common.util.PositiveAtomicCounter;
import com.tencent.tsf.femas.entity.namespace.Namespace;
import com.tencent.tsf.femas.entity.param.RegistryInstanceParam;
import com.tencent.tsf.femas.entity.registry.ClusterServer;
import com.tencent.tsf.femas.entity.registry.RegistryConfig;
import com.tencent.tsf.femas.entity.registry.RegistryPageService;
import java.io.Closeable;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

/**
 * @Author leoziltong
 *         各个注册中心实现差异比较大，服务列表和服务实例单独api获取的暂不做缓存列表
 *         Eureka做缓存列表
 * @Date: 2021/4/29 21:58
 * @Version 1.0
 */
@Slf4j
public abstract class RegistryOpenApiAdaptor implements RegistryOpenApiInterface, RoundRobbin, ServiceCacheListener,
        Closeable {

    private static long LISTOFSERVERS_CACHE_UPDATE_DELAY = 1000L;
    private static long LISTOFSERVERS_CACHE_REPEAT_INTERVAL = 10000L;
    protected final AbstractHttpClient httpClient;
    //eureka
    protected final Map<String, List<ServiceInstance>> serviceMapCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, PositiveAtomicCounter> indexes = new ConcurrentHashMap();
    private final Lock lock = new ReentrantLock();

    private final AtomicBoolean isStop = new AtomicBoolean(false);


    public RegistryOpenApiAdaptor() {
        this.httpClient = ApacheHttpClientHolder.getHttpClient();
    }

    private static ScheduledThreadPoolExecutor getListenerExecutor() {
        return LazyHolder._serverListListenerExecutor;
    }

    @Override
    public List<ClusterServer> clusterServers(RegistryConfig config) {
        return null;
    }

    @Override
    public ServerMetrics fetchServerMetrics(RegistryConfig config) {
        return null;
    }

    @Override
    public RegistryPageService fetchServices(RegistryConfig config, RegistryInstanceParam registryInstanceParam) {
        return null;
    }

    public abstract RegistryPageService fetchNamespaceServices(RegistryConfig config, String namespaceId, int pageNo,
            int pageSize);

    @Override
    public List<ServiceInstance> fetchServiceInstances(RegistryConfig config,
            RegistryInstanceParam registryInstanceParam) {
        return null;
    }

    public abstract void freshServiceMapCache(RegistryConfig config);

    public abstract boolean healthCheck(String url);

    @Override
    public String selectOne(RegistryConfig config) {
        return reTrySelectOne(config, 1);
    }

    private String reTrySelectOne(RegistryConfig config, int times) {
        List<String> urlList = config.convertClusterList();
        String key = config.getRegistryType();
        int length = urlList.size();
        PositiveAtomicCounter index = indexes.get(key);
        if (index == null) {
            indexes.putIfAbsent(key, new PositiveAtomicCounter());
            index = indexes.get(key);
        }
        String url = urlList.get(index.getAndIncrement() % length);
        if (healthCheck(url)) {
            return url;
        } else {
            if (times <= urlList.size()) {
                times++;
                reTrySelectOne(config, times);
            }
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
        LazyHolder.shutdownExecutorPool();
    }

    @Override
    public void listen(final RegistryConfig config) {
        ScheduledThreadPoolExecutor executor = getListenerExecutor();
        ScheduledFuture future = executor.scheduleWithFixedDelay(() -> {
            freshServiceMapCache(config);
        }, LISTOFSERVERS_CACHE_UPDATE_DELAY, LISTOFSERVERS_CACHE_REPEAT_INTERVAL, TimeUnit.MILLISECONDS);
    }

    protected <T> List<T> pageList(List<T> list, Integer pageNum, Integer pageSize) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        list.sort(new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return o1.hashCode() - o2.hashCode();
            }
        });
        Integer count = list.size();
        Integer pageCount = 0;
        if (count % pageSize == 0) {
            pageCount = count / pageSize;
        } else {
            pageCount = count / pageSize + 1;
        }
        int fromIndex = 0;
        int toIndex = 0;
        if (pageNum > pageCount) {
            pageNum = pageCount;
        }
        if (!pageNum.equals(pageCount)) {
            fromIndex = (pageNum - 1) * pageSize;
            toIndex = fromIndex + pageSize;
        } else {
            fromIndex = (pageNum - 1) * pageSize;
            toIndex = count;
        }
        List<T> pageList = list.subList(fromIndex, toIndex);
        return pageList;
    }

    @Override
    public void run() {

    }

    @Override
    public boolean createNamespace(RegistryConfig config, Namespace namespace) {
        return true;
    }

    @Override
    public boolean deleteNamespace(RegistryConfig config, Namespace namespace) {
        return true;
    }

    private static class LazyHolder {

        static ScheduledThreadPoolExecutor _serverListListenerExecutor = null;
        private static Thread _shutdownThread;

        static {
            int coreSize = Runtime.getRuntime().availableProcessors();
            ThreadFactory factory = (new ThreadFactoryBuilder()).setNameFormat("serverListListenerExecutor-%d")
                    .setDaemon(true).build();
            _serverListListenerExecutor = new ScheduledThreadPoolExecutor(coreSize, factory);
            _shutdownThread = new Thread(new Runnable() {
                public void run() {
                    RegistryOpenApiAdaptor.LazyHolder.shutdownExecutorPool();
                }
            });
            Runtime.getRuntime().addShutdownHook(_shutdownThread);
        }

        private LazyHolder() {
        }

        private static void shutdownExecutorPool() {
            if (_serverListListenerExecutor != null) {
                _serverListListenerExecutor.shutdown();
                if (_shutdownThread != null) {
                    try {
                        Runtime.getRuntime().removeShutdownHook(_shutdownThread);
                    } catch (IllegalStateException var1) {
                    }
                }
            }
        }
    }
}
