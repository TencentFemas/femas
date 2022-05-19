package com.tencent.tsf.femas.agent.tools;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author huyuanxin
 */
public class FileHandlerBuilder {

    protected static final String DEFAULT_AGENT_LOG = System.getProperty("user.home") + File.separator + "log" + File.separator + "femas" + File.separator + "agent.log";

    protected static final String AGENT_LOG_PATH_KEY = "femas_agent_log_path";

    public static DefaultBuilder defaultBuilder() {
        return new DefaultBuilder();
    }

    public static SizeSpiltFileHandler sizeSpiltLoggerFileHandlerBuilder() {
        return new SizeSpiltFileHandler();
    }

    private FileHandlerBuilder() {
        // static
    }

    public static TimeSplitFileHandlerBuilder timeSplitFileHandlerBuilder() {
        return new TimeSplitFileHandlerBuilder();
    }

    /**
     * 最基础的FileHandler
     */
    public static class DefaultBuilder {

        private DefaultBuilder() {
            // builder
        }

        public FileHandler build() throws IOException {
            String filePath = System.getProperty(AGENT_LOG_PATH_KEY);
            if (StringUtils.isBlank(filePath)) {
                filePath = DEFAULT_AGENT_LOG;
            }
            return new FileHandler(filePath);
        }
    }

    /**
     * 根据大小分割的FileHandler
     */
    public static class SizeSpiltFileHandler {

        private SizeSpiltFileHandler() {
            // builder
        }

        /**
         * 分割文件数目
         */
        private Integer splitNum;

        /**
         * 单个文件大小上线，单位为byte
         */
        private Integer byteNum;

        public SizeSpiltFileHandler setSplitNum(Integer splitNum) {
            if (splitNum == null || splitNum == 0) {
                this.splitNum = Integer.MAX_VALUE;
            } else {
                this.splitNum = splitNum;
            }
            return this;
        }

        public SizeSpiltFileHandler setByteNum(Integer byteNum) {
            if (byteNum == null || byteNum == 0) {
                this.byteNum = Integer.MAX_VALUE;
            } else {
                this.byteNum = byteNum;
            }
            return this;
        }

        public FileHandler build() throws IOException {
            if (byteNum == null || splitNum == null) {
                throw new NullPointerException();
            }
            String filePath = System.getProperty(AGENT_LOG_PATH_KEY);
            if (StringUtils.isBlank(filePath)) {
                filePath = DEFAULT_AGENT_LOG;
            }
            return new FileHandler(filePath, byteNum, splitNum);
        }

    }


    /**
     * 根据时间进行分割的FileHandler
     */
    public static class TimeSplitFileHandlerBuilder {
        private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

        private Long delay;

        private Logger logger;

        private FileHandler pre;

        private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

        private static final String LOG_FILE_FORMAT = System.getProperty("user.home") + File.separator + "log" + File.separator + "femas" + File.separator + "femas-admin-%s.log";

        private boolean init = true;

        private static final Formatter FORMATTER = new Formatter() {
            @Override
            public String format(LogRecord logRecord) {
                StringBuilder builder = new StringBuilder();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
                Date now = new Date();
                String dateStr = sdf.format(now);
                builder.append(dateStr).append(" - ");
                builder.append(logRecord.getLevel()).append(" - ");
                builder.append(logRecord.getMessage());
                builder.append("\r\n");
                return builder.toString();
            }
        };

        public TimeSplitFileHandlerBuilder() {
            this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1, r -> {
                Thread thread = new Thread(r);
                thread.setName("femas agent logger file relesh");
                thread.setDaemon(true);
                return thread;
            });
        }

        public TimeSplitFileHandlerBuilder setDelay(Long delay) {
            if (delay < 0) {
                throw new IllegalArgumentException();
            }
            this.delay = delay;
            return this;
        }

        public TimeSplitFileHandlerBuilder setLogger(Logger logger) {
            this.logger = logger;
            return this;
        }

        public TimeSplitFileHandlerBuilder quickSetSecond(long second) {
            return this.setDelay(second);
        }

        public TimeSplitFileHandlerBuilder quickSetDay(long day) {
            if (day < 1) {
                throw new IllegalArgumentException();
            }
            // 计算
            return this.quickSetHour(24);
        }

        public TimeSplitFileHandlerBuilder quickSetMonth(long month) {
            if (month < 1) {
                throw new IllegalArgumentException();
            }
            // 一个月按30天算
            return this.quickSetDay(30 * month);
        }

        public TimeSplitFileHandlerBuilder quickSetHour(long hour) {
            if (hour < 1) {
                throw new IllegalArgumentException();
            }
            return this.quickSetMinute(60 * hour);
        }

        public TimeSplitFileHandlerBuilder quickSetMinute(long minute) {
            if (minute < 1) {
                throw new IllegalArgumentException();
            }
            return this.quickSetSecond(60 * minute);
        }

        public void build() throws IOException {
            this.pre = newFileHandler();
            logger.addHandler(pre);
            scheduledThreadPoolExecutor.execute(new Fresh());
        }

        private FileHandler newFileHandler() throws IOException {
            FileHandler fileHandler = new FileHandler(String.format(LOG_FILE_FORMAT, simpleDateFormat.format(new Date(System.currentTimeMillis()))));
            fileHandler.setFormatter(FORMATTER);
            return fileHandler;
        }

        private class Fresh implements Runnable {

            @Override
            public synchronized void run() {
                try {
                    if (init) {
                        init = false;
                        return;
                    }
                    FileHandler next = newFileHandler();
                    logger.addHandler(next);
                    logger.removeHandler(pre);
                    pre.close();
                    pre = next;
                } catch (IOException e) {
                    logger.warning(e.getMessage());
                } finally {
                    scheduledThreadPoolExecutor.schedule(this, delay, TimeUnit.SECONDS);
                }
            }
        }
    }
}
