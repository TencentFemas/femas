package com.tencent.tsf.femas.agent.transformer.async;

import com.tencent.tsf.femas.agent.tools.ReflectionUtils;

import java.util.TimerTask;


/**
 * @Author leoziltong@tencent.com
 */
public class AgentTimerTask extends TimerTask {

    private Object rpcContext;
    private TimerTask timerTask;

    public AgentTimerTask(Object rpcContext, TimerTask timerTask) {
        this.rpcContext = rpcContext;
        this.timerTask = timerTask;
    }

    @Override
    public void run() {
        try {
            ReflectionUtils.invokeStaticMethod(RunnableWrapper.FEMAS_CONTEXT_CLASS_NAME,RunnableWrapper.CONTEXT_SET_VALUE_METHOD_NAME,rpcContext);
            if (timerTask != null) {
                this.timerTask.run();
            }
        } finally {
            ReflectionUtils.invokeStaticMethod(RunnableWrapper.FEMAS_CONTEXT_CLASS_NAME,RunnableWrapper.CONTEXT_CLEAN_VALUE_METHOD_NAME);
        }

    }
}
