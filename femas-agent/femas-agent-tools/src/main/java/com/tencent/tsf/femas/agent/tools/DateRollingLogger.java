package com.tencent.tsf.femas.agent.tools;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * @author huyuanxin
 */
public class DateRollingLogger {

    /**
     * 需要定时刷新文件的的logger
     */
    private Logger logger;

    /**
     * 先前的handler
     */
    private FileHandler pre;

    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1, r -> {
        Thread thread = new Thread(r);
        thread.setName("Logger file update thread");
        thread.setDaemon(true);
        return thread;
    });

    public static long getTodayLeft() {
        LocalDateTime midnight = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return ChronoUnit.SECONDS.between(LocalDateTime.now(), midnight);
    }

    public DateRollingLogger() {

    }

    private FileHandler newFileHandler() {
        return null;
    }

    public void start() {
        scheduledThreadPoolExecutor.execute();
    }


    class Fresh implements Runnable {
        @Override
        public void run() {
            try {
                updateFileHandler(newFileHandler());
            } finally {
                scheduledThreadPoolExecutor.schedule(this, getTodayLeft(), TimeUnit.SECONDS);
            }
        }

        private void updateFileHandler(FileHandler fileHandler) {
            this.logger.addHandler(fileHandler);
            this.logger.removeHandler(pre);
            pre.close();
            pre = fileHandler;
        }
    }
}
