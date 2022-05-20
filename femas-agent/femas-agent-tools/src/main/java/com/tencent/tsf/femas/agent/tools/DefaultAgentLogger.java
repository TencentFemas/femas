package com.tencent.tsf.femas.agent.tools;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DefaultAgentLogger extends AbstractAgentLogger {

    private static final DefaultAgentLogger LOG = DefaultAgentLogger.getLogger(DefaultAgentLogger.class);
    static Logger logger = Logger.getLogger("AgentLogger");

    private static PrintStream printStream = System.out;

    private final String messagePattern;

    static {
        try {
            loggerStrategy();
        } catch (SecurityException e) {
            LOG.error("Initialize logger error:", e);
        } catch (IOException e) {
            LOG.error("Initialize logger error:", e);
        }
    }

    public static DefaultAgentLogger getLogger(Class<?> clazz) {
        return new DefaultAgentLogger(clazz.getName());
    }

    public DefaultAgentLogger(String loggerName) {
        if (loggerName == null) {
            throw new NullPointerException("loggerName must not be null");
        }
        this.messagePattern = "{0,date,yyyy-MM-dd HH:mm:ss} [{1}] (" + loggerName + ") {2}{3}";
    }

    private static void loggerStrategy() throws IOException {
        // time rolling
        //FileHandlerBuilder.timeSplitFileHandlerBuilder().setLogger(logger).setDelay(10L).build();

        // default
        //FileHandler fileHandler=FileHandlerBuilder.defaultBuilder().build();
        //logger.addHandler(fileHandler);

        // size rolling
        FileHandler fileHandler = FileHandlerBuilder.sizeSpiltLoggerFileHandlerBuilder().setByteNum(0).setByteNum(1).build();
        logger.addHandler(fileHandler);
    }

    @Override
    public void info(String msg) {
        String formatMessage = format(LogLevel.INFO.name(), msg, "");
        printStream.println(formatMessage);
        logger.log(Level.INFO, formatMessage);
    }

    @Override
    public void warn(String msg) {
        warn(msg, null);
    }

    @Override
    public void warn(String msg, Throwable throwable) {
        String exceptionMessage = toString(throwable);
        String formatMessage = format(LogLevel.WARN.name(), msg, exceptionMessage);
        printStream.println(formatMessage);
        logger.log(Level.WARNING, formatMessage);
    }

    @Override
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
