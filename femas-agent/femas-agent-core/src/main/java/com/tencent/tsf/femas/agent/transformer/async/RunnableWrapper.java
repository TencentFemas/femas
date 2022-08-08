package com.tencent.tsf.femas.agent.transformer.async;


import com.tencent.tsf.femas.agent.tools.ReflectionUtils;

import java.util.TimerTask;
import java.util.concurrent.Callable;


/**
 * @Author leoziltong@tencent.com
 */
public class RunnableWrapper {
    public static final String FEMAS_CONTEXT_CLASS_NAME = "com.tencent.tsf.femas.common.context.FemasContext";
    public static final String CONTEXT_COPY_VALUE_METHOD_NAME = "getCopyRpcContext";
    public static final String CONTEXT_SET_VALUE_METHOD_NAME = "restoreRpcContext";
    public static final String CONTEXT_CLEAN_VALUE_METHOD_NAME = "reset";

    public static Runnable wrapRunnable(Runnable runnable) {
        if (runnable instanceof AgentRunnable) {
            return runnable;
        } else {
            Object rpcContext = ReflectionUtils.invokeStaticMethod(FEMAS_CONTEXT_CLASS_NAME, CONTEXT_COPY_VALUE_METHOD_NAME);
            return new AgentRunnable(rpcContext, runnable);
        }
    }

    public static Callable wrapCallable(Callable callable) {
        if (callable instanceof AgentCallable) {
            return callable;
        } else {
            Object rpcContext = ReflectionUtils.invokeStaticMethod(FEMAS_CONTEXT_CLASS_NAME, CONTEXT_COPY_VALUE_METHOD_NAME);
            return new AgentCallable(rpcContext, callable);
        }
    }

    public static TimerTask wrapTimerTask(TimerTask timerTask) {
        if (timerTask instanceof AgentTimerTask) {
            return timerTask;
        } else {
            Object rpcContext = ReflectionUtils.invokeStaticMethod(FEMAS_CONTEXT_CLASS_NAME, CONTEXT_COPY_VALUE_METHOD_NAME);
            return new AgentTimerTask(rpcContext, timerTask);
        }
    }

    public static AgentSyncTask wrapAsync(Object target) {
        Object rpcContext = ReflectionUtils.invokeStaticMethod(FEMAS_CONTEXT_CLASS_NAME, CONTEXT_COPY_VALUE_METHOD_NAME);
        return new AgentSyncTask(rpcContext, target);
    }
}
