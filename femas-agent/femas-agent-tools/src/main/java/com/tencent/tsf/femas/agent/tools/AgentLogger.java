package com.tencent.tsf.femas.agent.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author HuYua
 */
public class AgentLogger extends AbstractAgentLogger {

    private final Logger logger;

    public static AgentLogger getLogger(Class<?> clazz) {
        return new AgentLogger(clazz);
    }

    private AgentLogger(Class<?> clazz) {
        logger = LoggerFactory.getLogger(clazz.getName());
    }

    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    @Override
    public void warn(String msg) {
        logger.warn(msg);
    }

    @Override
    public void warn(String msg, Throwable throwable) {
        logger.warn(msg, throwable);
    }

    @Override
    public void error(String msg, Throwable throwable) {
        logger.error(msg, throwable);
    }

}
