package com.tencent.tsf.femas.common.struct;

import com.tencent.tsf.femas.common.util.NamedThreadFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduledService {

    /**
     * 固定频率执行，按执行开始时间计算间隔
     */
    public static final int MODE_FIXEDRATE = 0;
    /**
     * 固定间隔执行，执行完成后才计算间隔
     */
    public static final int MODE_FIXEDDELAY = 1;
    /**
     * 缓存了目前全部的定时任务， 用于重建
     */
    protected final static Map<ScheduledService, Long> SCHEDULED_SERVICE_MAP = new ConcurrentHashMap<ScheduledService,
            Long>();
    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ScheduledService.class);
    /**
     * 正在重置标识
     */
    protected static volatile boolean resetting;
    /**
     * The Runnable.
     */
    private final Runnable runnable;

    /**
     * The Initial delay.
     */
    private final long initialDelay;

    /**
     * The Period.
     */
    private final long period;

    /**
     * The Unit.
     */
    private final TimeUnit unit;

    /**
     * 0:scheduleAtFixedRate
     * 1:scheduleWithFixedDelay
     */
    private final int mode;
    /**
     * The Scheduled executor service.
     */
    private volatile ScheduledExecutorService scheduledExecutorService;
    /**
     * The Thread name
     */
    private String threadName;
    /**
     * The Future.
     */
    private volatile ScheduledFuture future;
    /**
     * The Started.
     */
    private volatile boolean started;

    /**
     * Instantiates a new Scheduled service.
     *
     * @param threadName the thread name
     * @param mode the mode
     * @param runnable the runnable
     * @param initialDelay the initial delay
     * @param period the period
     * @param unit the unit
     */
    public ScheduledService(String threadName,
            int mode,
            Runnable runnable,
            long initialDelay,
            long period,
            TimeUnit unit) {
        this.threadName = threadName;
        this.runnable = runnable;
        this.initialDelay = initialDelay;
        this.period = period;
        this.unit = unit;
        this.mode = mode;
    }

    /**
     * 重建定时任务，用于特殊情况
     */
    public static synchronized void reset() {
        resetting = true;
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("Start resetting all {} schedule executor service.", SCHEDULED_SERVICE_MAP.size());
        }
        for (Map.Entry<ScheduledService, Long> entry : SCHEDULED_SERVICE_MAP.entrySet()) {
            try {
                ScheduledService service = entry.getKey();
                if (service.isStarted()) {
                    service.stop();
                    service.start();
                }
            } catch (Exception e) {
                LOGGER.error("Reset femas scheduled service failed.", e);
            }
        }
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("Already reset all {} schedule executor service.", SCHEDULED_SERVICE_MAP.size());
        }
        resetting = false;
    }

    /**
     * 是否正在重置
     *
     * @return the boolean
     */
    public static boolean isResetting() {
        return resetting;
    }

    /**
     * 开始执行定时任务
     *
     * @return the boolean
     * @see ScheduledExecutorService#scheduleAtFixedRate(Runnable, long, long, TimeUnit)
     * @see ScheduledExecutorService#scheduleWithFixedDelay(Runnable, long, long, TimeUnit)
     */
    public synchronized ScheduledService start() {
        if (started) {
            return this;
        }
        if (scheduledExecutorService == null) {
            scheduledExecutorService = new ScheduledThreadPoolExecutor(1,
                    new NamedThreadFactory(threadName, true));
        }
        ScheduledFuture future = null;
        switch (mode) {
            case MODE_FIXEDRATE:
                future = scheduledExecutorService.scheduleAtFixedRate(runnable, initialDelay,
                        period,
                        unit);
                break;
            case MODE_FIXEDDELAY:
                future = scheduledExecutorService.scheduleWithFixedDelay(runnable, initialDelay, period,
                        unit);
                break;
            default:
                break;
        }
        if (future != null) {
            this.future = future;
            // 缓存一下
            SCHEDULED_SERVICE_MAP.put(this, System.currentTimeMillis());
            started = true;
        } else {
            started = false;
        }
        return this;
    }

    /**
     * 停止执行定时任务，还可以重新start
     */
    public synchronized void stop() {
        if (!started) {
            return;
        }
        try {
            if (future != null) {
                future.cancel(true);
                future = null;
            }
            if (scheduledExecutorService != null) {
                scheduledExecutorService.shutdownNow();
                scheduledExecutorService = null;
            }
        } catch (Throwable t) {
            LOGGER.warn(t.getMessage(), t);
        } finally {
            SCHEDULED_SERVICE_MAP.remove(this);
            started = false;
        }
    }

    /**
     * 停止执行定时任务，还可以重新start
     */
    public void shutdown() {
        stop();
    }

    /**
     * 是否已经启动
     *
     * @return the boolean
     */
    public boolean isStarted() {
        return started;
    }
}
