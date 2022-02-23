package com.tencent.tsf.femas.extension.springcloud.discovery.ribbon;

import com.netflix.client.config.CommonClientConfigKey;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ServerListUpdater;
import com.tencent.tsf.femas.common.util.NamedThreadFactory;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LongPollingServerListUpdater implements ServerListUpdater {

    private static final Logger logger = LoggerFactory.getLogger(LongPollingServerListUpdater.class);
    public static String SECOND_PREFIX = "polling-watch-server-list-updater";
    private static long LISTOFSERVERS_CACHE_UPDATE_DELAY = 0; // msecs;
    private static int LISTOFSERVERS_CACHE_REPEAT_INTERVAL = 1000; // msecs;
    private final AtomicBoolean isActive = new AtomicBoolean(false);
    private final long initialDelayMs;
    private final long refreshIntervalMs;
    private volatile long lastUpdated = System.currentTimeMillis();
    private volatile ScheduledFuture<?> scheduledFuture;

    public LongPollingServerListUpdater() {
        this(LISTOFSERVERS_CACHE_UPDATE_DELAY, LISTOFSERVERS_CACHE_REPEAT_INTERVAL);
    }

    public LongPollingServerListUpdater(IClientConfig clientConfig) {
        this(LISTOFSERVERS_CACHE_UPDATE_DELAY, getRefreshIntervalMs(clientConfig));
    }

    public LongPollingServerListUpdater(final long initialDelayMs, final long refreshIntervalMs) {
        this.initialDelayMs = initialDelayMs;
        this.refreshIntervalMs = refreshIntervalMs;
    }

    private static ScheduledThreadPoolExecutor getRefreshExecutor() {
        return LongPollingServerListUpdater.LazyHolder._serverListRefreshExecutor;
    }

    private static AtomicInteger getAtomicCoreSize() {
        return LazyHolder.coreSize;
    }

    private static long getRefreshIntervalMs(IClientConfig clientConfig) {
        return clientConfig.get(CommonClientConfigKey.ServerListRefreshInterval, LISTOFSERVERS_CACHE_REPEAT_INTERVAL);
    }

    @Override
    public synchronized void start(final ServerListUpdater.UpdateAction updateAction) {
        if (isActive.compareAndSet(false, true)) {
            final Runnable wrapperRunnable = new Runnable() {
                @Override
                public void run() {
                    if (!isActive.get()) {
                        if (scheduledFuture != null) {
                            scheduledFuture.cancel(true);
                        }
                        return;
                    }
                    try {
                        updateAction.doUpdate();
                        lastUpdated = System.currentTimeMillis();
                    } catch (Throwable e) {
                        logger.warn("Failed one update cycle", e);
                    }
                }
            };
            int newCoreSize = getAtomicCoreSize().incrementAndGet();
            ScheduledThreadPoolExecutor refreshExecutor = getRefreshExecutor();
            // 更新 core size
            refreshExecutor.setCorePoolSize(newCoreSize);

            scheduledFuture = refreshExecutor.scheduleWithFixedDelay(
                    wrapperRunnable,
                    initialDelayMs,
                    refreshIntervalMs,
                    TimeUnit.MILLISECONDS
            );
        } else {
            logger.info("Already active, no-op");
        }
    }

    @Override
    public synchronized void stop() {
        if (isActive.compareAndSet(true, false)) {
            if (scheduledFuture != null) {
                scheduledFuture.cancel(true);
                // 更新 core size
                int newCoreSize = getAtomicCoreSize().decrementAndGet();
                getRefreshExecutor().setCorePoolSize(newCoreSize);
            }
        } else {
            logger.info("Not active, no-op");
        }
    }

    @Override
    public String getLastUpdate() {
        return new Date(lastUpdated).toString();
    }

    @Override
    public long getDurationSinceLastUpdateMs() {
        return System.currentTimeMillis() - lastUpdated;
    }

    @Override
    public int getNumberMissedCycles() {
        if (!isActive.get()) {
            return 0;
        }
        return (int) ((int) (System.currentTimeMillis() - lastUpdated) / refreshIntervalMs);
    }

    @Override
    public int getCoreThreads() {
        if (isActive.get()) {
            if (getRefreshExecutor() != null) {
                return getRefreshExecutor().getCorePoolSize();
            }
        }
        return 0;
    }

    private static class LazyHolder {

        private final static String CORE_THREAD = "DynamicServerListLoadBalancer.ThreadPoolSize";
        static ScheduledThreadPoolExecutor _serverListRefreshExecutor = null;
        static AtomicInteger coreSize = new AtomicInteger(1);
        private static Thread _shutdownThread;

        static {
            _serverListRefreshExecutor = new ScheduledThreadPoolExecutor(coreSize.get(),
                    new NamedThreadFactory(SECOND_PREFIX, true));
            _shutdownThread = new Thread(new Runnable() {
                public void run() {
                    logger.info("Shutting down the Executor Pool for PollingServerListUpdater");
                    shutdownExecutorPool();
                }
            });
            Runtime.getRuntime().addShutdownHook(_shutdownThread);
        }

        private static void shutdownExecutorPool() {
            if (_serverListRefreshExecutor != null) {
                _serverListRefreshExecutor.shutdown();

                if (_shutdownThread != null) {
                    try {
                        Runtime.getRuntime().removeShutdownHook(_shutdownThread);
                    } catch (IllegalStateException ise) { // NOPMD
                        // this can happen if we're in the middle of a real
                        // shutdown,
                        // and that's 'ok'
                    }
                }

            }
        }
    }
}
