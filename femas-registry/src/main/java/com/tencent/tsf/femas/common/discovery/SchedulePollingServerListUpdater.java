package com.tencent.tsf.femas.common.discovery;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static long LISTOFSERVERS_CACHE_UPDATE_DELAY = 1000L;
    /**
     * 默认更新间隔
     */
    private static long LISTOFSERVERS_CACHE_REPEAT_INTERVAL = 30000L;
    private final AtomicBoolean isActive;
    private final long initialDelayMs;
    private final long refreshIntervalMs;
    private volatile long lastUpdated;

    public SchedulePollingServerListUpdater() {
        this(LISTOFSERVERS_CACHE_UPDATE_DELAY, LISTOFSERVERS_CACHE_REPEAT_INTERVAL);
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
     * @return
     */
    private static ScheduledThreadPoolExecutor getRefreshExecutor() {
        return SchedulePollingServerListUpdater.LazyHolder._serverListRefreshExecutor;
    }

    @Override
    public synchronized ScheduledFuture<?> start(final UpdateAction updateAction) {

        if (this.isActive.compareAndSet(false, true)) {
            Runnable wrapperRunnable = new Runnable() {
                public void run() {
                    if (SchedulePollingServerListUpdater.this.isActive.get()) {
                        try {
                            updateAction.doUpdate();
                            SchedulePollingServerListUpdater.this.lastUpdated = System.currentTimeMillis();
                        } catch (Exception var) {
                            logger.warn("Failed one update cycle", var);
                        }
                    }
                }
            };
            return getRefreshExecutor()
                    .scheduleWithFixedDelay(wrapperRunnable, this.initialDelayMs, this.refreshIntervalMs,
                            TimeUnit.MILLISECONDS);
        } else {
            logger.info("Already active");
        }
        return null;
    }

    @Override
    public synchronized void stop(ScheduledFuture scheduledFuture) {
        if (this.isActive.compareAndSet(true, false)) {
            if (scheduledFuture != null) {
                scheduledFuture.cancel(true);
            }
        } else {
            logger.info("Not active");
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

        static ScheduledThreadPoolExecutor _serverListRefreshExecutor = null;
        private static Thread _shutdownThread;

        static {
            int coreSize = Runtime.getRuntime().availableProcessors();
            ThreadFactory factory = (new ThreadFactoryBuilder()).setNameFormat("PollingServerListUpdater-%d")
                    .setDaemon(true).build();
            _serverListRefreshExecutor = new ScheduledThreadPoolExecutor(coreSize, factory);
            _shutdownThread = new Thread(new Runnable() {
                public void run() {
                    logger.info("Shutting down the Executor Pool for PollingServerListUpdater");
                    SchedulePollingServerListUpdater.LazyHolder.shutdownExecutorPool();
                }
            });
            Runtime.getRuntime().addShutdownHook(_shutdownThread);
        }

        private LazyHolder() {
        }

        private static void shutdownExecutorPool() {
            if (_serverListRefreshExecutor != null) {
                _serverListRefreshExecutor.shutdown();
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
