package com.tencent.tsf.femas.agent.tools;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;


public class AgentLogger {

    private static final AgentLogger LOG = AgentLogger.getLogger(AgentLogger.class);
    static Logger logger = Logger.getLogger("AgentLogger");
    private static final String DEFAULT_AGENT_LOG = System.getProperty("user.home")+ File.separator+"log"+ File.separator+"femas"+ File.separator+"agent.log";
    private static final String AGENT_LOG_PATH_KEY = "femas_agent_log_path";

    private static PrintStream printStream;
    private final String messagePattern;

    static {
        FileHandler fh;
        try {
            printStream = System.out;
            String filePath = System.getProperty(AGENT_LOG_PATH_KEY);
            if(StringUtils.isBlank(filePath)){
                filePath = DEFAULT_AGENT_LOG;
            }
            fh = new FileHandler(filePath);
            logger.addHandler(fh);
            fh.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    StringBuilder builder = new StringBuilder();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
                    Date now = new Date();
                    String dateStr = sdf.format(now);
                    builder.append(dateStr).append(" - ");
                    builder.append(record.getLevel()).append(" - ");
                    builder.append(record.getMessage());
                    builder.append("\r\n");
                    return builder.toString();
                }
            });
        } catch (SecurityException e) {
            LOG.error("Initialize logger error:", e);
        } catch (IOException e) {
            LOG.error("Initialize logger error:", e);
        }
    }

    public static AgentLogger getLogger(Class<?> clazz) {
        return new AgentLogger(clazz.getName());
    }

    public AgentLogger(String loggerName) {
        if (loggerName == null) {
            throw new NullPointerException("loggerName must not be null");
        }
        this.messagePattern = "{0,date,yyyy-MM-dd HH:mm:ss} [{1}] (" + loggerName + ") {2}{3}";
    }

    public void info(String msg) {
        String formatMessage = format(LogLevel.INFO.name(), msg, "");
        printStream.println(formatMessage);
        logger.log(Level.INFO, formatMessage);
    }

    public void warn(String msg) {
        warn(msg, null);
    }

    public void warn(String msg, Throwable throwable) {
        String exceptionMessage = toString(throwable);
        String formatMessage = format(LogLevel.WARN.name(), msg, exceptionMessage);
        printStream.println(formatMessage);
        logger.log(Level.WARNING, formatMessage);
    }

    public void error(String msg, Throwable throwable) {
        String exceptionMessage = toString(throwable);
        String stackTrace = getStackTraceString(throwable);
        String formatMessage = format(LogLevel.INFO.name(), msg, exceptionMessage);
        printStream.println(formatMessage);
        logger.log(Level.SEVERE, formatMessage);
    }

    private String toString(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println();
        throwable.printStackTrace(pw);
        pw.close();

        return sw.toString();
    }

    private String defaultString(String exceptionMessage, String defaultValue) {
        if (exceptionMessage == null) {
            return defaultValue;
        }

        return exceptionMessage;
    }

    public static String getStackTraceString(Throwable ex) {
        String result = "";
        try {
            StackTraceElement[] traceElements = ex.getStackTrace();
            StringBuilder traceBuilder = new StringBuilder();

            if (traceElements != null && traceElements.length > 0) {
                for (StackTraceElement traceElement : traceElements) {
                    traceBuilder.append(traceElement.toString());
                    traceBuilder.append("\n");
                }
            }
            String stackTrace = traceBuilder.toString();
            String exceptionType = ex.toString();
            String exceptionMessage = ex.getMessage();

            result = String.format("%s : %s \r\n %s", exceptionType, exceptionMessage, stackTrace);
        } catch (Exception stEx) {
            LOG.error("getStackTraceString error:", stEx);
            if (ex != null) {
                result = ex.getMessage();
            }
        }
        return result;
    }

    private String format(String logLevel, String msg, String exceptionMessage) {
        exceptionMessage = defaultString(exceptionMessage, "");
        MessageFormat messageFormat = new MessageFormat(messagePattern);
        final long date = System.currentTimeMillis();
        Object[] parameter = {date, logLevel, msg, exceptionMessage};
        return messageFormat.format(parameter);
    }

    enum LogLevel {
        WARN, INFO, ERROR;
    }

}
