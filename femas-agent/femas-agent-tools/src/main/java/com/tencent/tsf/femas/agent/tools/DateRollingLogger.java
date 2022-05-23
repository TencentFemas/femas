package com.tencent.tsf.femas.agent.tools;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
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
     * 发生异常时打印异常的logger
     */
    private static final Logger LOGGER_FOR_EXCEPTION = Logger.getLogger("DateRollingLogger");

    /**
     * 先前的handler,用于更新FileHandler时进行移除
     */
    private FileHandler pre;

    /**
     * 保证第一次不需要更换fileHandler
     */
    private final AtomicBoolean startUp = new AtomicBoolean(false);

    /**
     * 生成日志的路径
     */
    private final String logFileLocationFormat = System.getProperty("user.home") + File.separator + "log" + File.separator + "femas" + File.separator + "agent-%s.log";

    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1, r -> {
        Thread thread = new Thread(r);
        thread.setName("Logger file update thread");
        thread.setDaemon(true);
        return thread;
    });

    /**
     * 获得今天剩余时间
     *
     * @return 今天剩余的时间
     */
    public static long getTodayLeft() {
        LocalDateTime midnight = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return ChronoUnit.SECONDS.between(LocalDateTime.now(), midnight);
    }

    @SuppressWarnings("unused")
    private DateRollingLogger() {
        // private
    }

    public DateRollingLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * 创建新的FileHandler
     *
     * @return 新的FileHandler
     * @throws IOException 创建异常
     */
    private FileHandler newFileHandler() throws IOException {
        // 每次new一次,避免线程安全
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        FileHandler fileHandler = new FileHandler(String.format(logFileLocationFormat, dateFormat.format(new Date())), true);

        // 每次new一次,避免使用的DateFormat出现线程安全的问题
        fileHandler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord logRecord) {
                StringBuilder builder = new StringBuilder();
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
                Date now = new Date();
                String dateStr = dateFormat.format(now);
                builder.append(dateStr).append(" - ");
                builder.append(logRecord.getLevel()).append(" - ");
                builder.append(logRecord.getMessage());
                builder.append("\r\n");
                return builder.toString();
            }
        });
        return fileHandler;
    }

    /**
     * 开始定时更新
     */
    public void start() {
        if (this.logger == null) {
            throw new NullPointerException("logger can't be null");
        }
        try {
            pre = newFileHandler();
            logger.addHandler(pre);
            scheduledThreadPoolExecutor.execute(new RefreshFileHandler());
        } catch (Exception e) {
            LOGGER_FOR_EXCEPTION.warning(e.getMessage());
        }
    }

    /**
     * 刷新的FileHandler的方法
     */
    class RefreshFileHandler implements Runnable {
        @Override
        public void run() {
            try {
                if (Boolean.TRUE.equals(startUp.get())) {
                    updateFileHandler(newFileHandler());
                    return;
                }
                startUp.compareAndSet(false, true);
            } catch (Exception e) {
                LOGGER_FOR_EXCEPTION.warning(e.getMessage());
            } finally {
                scheduledThreadPoolExecutor.schedule(this, getTodayLeft(), TimeUnit.SECONDS);
            }
        }

        /**
         * 更新FileHandler
         *
         * @param fileHandler 新的FileHandler
         */
        private void updateFileHandler(FileHandler fileHandler) {
            logger.addHandler(fileHandler);
            logger.removeHandler(pre);
            pre.close();
            pre = fileHandler;
        }
    }
}
