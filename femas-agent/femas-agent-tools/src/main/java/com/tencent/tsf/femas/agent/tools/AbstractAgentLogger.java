package com.tencent.tsf.femas.agent.tools;

/**
 * @author huyuanxin
 */
public abstract class AbstractAgentLogger {

    protected AbstractAgentLogger() {
        // abstract
    }

    /**
     * 打印信息
     *
     * @param msg 信息
     */
    public abstract void info(String msg);

    /**
     * 打印警告信息
     *
     * @param msg 警告信息
     */
    public abstract void warn(String msg);

    /**
     * 警告信息
     *
     * @param msg       警告信息
     * @param throwable 异常
     */
    public abstract void warn(String msg, Throwable throwable);

    /**
     * 打印错误信息
     *
     * @param msg       错误
     * @param throwable 异常
     */
    public abstract void error(String msg, Throwable throwable);

}
