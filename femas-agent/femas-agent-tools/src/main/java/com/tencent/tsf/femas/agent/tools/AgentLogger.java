package com.tencent.tsf.femas.agent.tools;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class AgentLogger {
    static Logger logger = Logger.getLogger("AgentLogger");
    private static final String AGENT_LOG_LINUX = "/tmp/agent.log";
    private static final String AGENT_LOG_WINDOWS = "C:/agent_plugin.log";

    static {
        FileHandler fh;
        try {
            String filePath = isOSWindows() ? AGENT_LOG_WINDOWS : AGENT_LOG_LINUX;
            fh = new FileHandler(filePath);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static Logger getLogger() {
        return logger;
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
            getLogger().severe("getStackTraceString error:" + stEx.getMessage());
            if (ex != null) {
                result = ex.getMessage();
            }
        }
        return result;
    }

    public static boolean isOSWindows() {
        String osName = System.getProperty("os.name");
        if (StringUtils.isEmpty(osName)) {
            return false;
        }
        return osName.startsWith("Windows");
    }

}
