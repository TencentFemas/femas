package com.tencent.tsf.femas.common.discovery;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author leoziltong
 * @Author netflix
 * @Description //TODO
 * @Date: 2021/3/23 18:02
 * @Version 1.0
 */
public class SchedulePollingServerListUpdater implements ServerUpdater {

    private static final Logger logger = LoggerFactory.getLogger(SchedulePollingServerListUpdater.class);

    /**
     * 默认更新延迟
     */
    private static final long LIST_OF_SERVERS_CACHE_UPDATE_DELAY = 1000L;

    /**
     * 默认更新间隔
     */
    private static final long LIST_OF_SERVERS_CACHE_REPEAT_INTERVAL = 30000L;

    private final AtomicBoolean isActive;

    private final long initialDelayMs;

    private final long refreshIntervalMs;

    private volatile long lastUpdated;

    public SchedulePollingServerListUpdater() {
        this(LIST_OF_SERVERS_CACHE_UPDATE_DELAY, LIST_OF_SERVERS_CACHE_REPEAT_INTERVAL);
    }

    public SchedulePollingServerListUpdater(long initialDelayMs, long refreshIntervalMs) {
        this.isActive = new AtomicBoolean(false);
        this.lastUpdated = System.currentTimeMillis();
        this.initialDelayMs = initialDelayMs;
        this.refreshIntervalMs = refreshIntervalMs;
    }

    /**
     * lazy init executor
     *
     * @return ScheduledThreadPoolExecutor
     */
    private static ScheduledThreadPoolExecutor getRefreshExecutor() {
        return SchedulePollingServerListUpdater.LazyHolder.serverListRefreshExecutor;
    }

    @Override
    public synchronized ScheduledFuture<?> start(final UpdateAction updateAction) {

        if (this.isActive.compareAndSet(false, true)) {
            Runnable wrapperRunnable = () -> {
                if (SchedulePollingServerListUpdater.this.isActive.get()) {
                    try {
                        updateAction.doUpdate();
                        SchedulePollingServerListUpdater.this.lastUpdated = System.currentTimeMillis();
                    } catch (Exception exception) {
                        logger.warn("Failed one update cycle", exception);
                    }
                }
            };
            return getRefreshExecutor()
                    .scheduleWithFixedDelay(wrapperRunnable, this.initialDelayMs, this.refreshIntervalMs,
                            TimeUnit.MILLISECONDS);
        } else {
            logger.info("SchedulePollingServerListUpdater already active");
        }
        return null;
    }

    @Override
    public synchronized void stop(ScheduledFuture<?> scheduledFuture) {
        if (this.isActive.compareAndSet(true, false)) {
            if (scheduledFuture != null) {
                scheduledFuture.cancel(true);
            }
        } else {
            logger.info("SchedulePollingServerListUpdater is not active");
        }

    }

    @Override
    public String getLastUpdate() {
        return (new Date(this.lastUpdated)).toString();
    }

    @Override
    public long getDurationSinceLastUpdateMs() {
        return System.currentTimeMillis() - this.lastUpdated;
    }

    @Override
    public int getCoreThreads() {
        return this.isActive.get() && getRefreshExecutor() != null ? getRefreshExecutor().getCorePoolSize() : 0;
    }

    private static class LazyHolder {

        static ScheduledThreadPoolExecutor serverListRefreshExecutor;

        private static final Thread SHUTDOWN_THREAD;

        static {
            int coreSize = Runtime.getRuntime().availableProcessors();
            ThreadFactory factory = (new ThreadFactoryBuilder()).setNameFormat("PollingServerListUpdater-%d")
                    .setDaemon(true).build();
            serverListRefreshExecutor = new ScheduledThreadPoolExecutor(coreSize, factory);
            SHUTDOWN_THREAD = new Thread(() -> {
                logger.info("Shutting down the Executor Pool for PollingServerListUpdater");
                LazyHolder.shutdownExecutorPool();
            });
            Runtime.getRuntime().addShutdownHook(SHUTDOWN_THREAD);
        }

        private LazyHolder() {
        }

        private static void shutdownExecutorPool() {
            if (serverListRefreshExecutor != null) {
                serverListRefreshExecutor.shutdown();
                if (SHUTDOWN_THREAD != null) {
                    try {
                        Runtime.getRuntime().removeShutdownHook(SHUTDOWN_THREAD);
                    } catch (IllegalStateException exception) {
                        logger.warn("shutdownExecutorPool error", exception);
                    }
                }
            }
        }
    }

}
