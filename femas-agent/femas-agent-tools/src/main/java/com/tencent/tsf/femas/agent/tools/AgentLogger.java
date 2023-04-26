package com.tencent.tsf.femas.agent.tools;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;


public class AgentLogger {

    private static final AgentLogger LOG = AgentLogger.getLogger(AgentLogger.class);

    static Logger logger = Logger.getLogger("AgentLogger");

    private static final PrintStream printStream = System.out;

    private final String messagePattern;

    static {
        new DateRollingLogger(logger).start();
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

    public void error(String msg) {
        error(msg, null);
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

        /**
         * 警告
         */
        WARN,

        /**
         * 信息
         */
        INFO,

        /**
         * 异常
         */
        ERROR

    }

}
