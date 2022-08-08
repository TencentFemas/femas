package com.tencent.tsf.femas.agent.transformer.async;


import com.tencent.tsf.femas.agent.tools.ReflectionUtils;

/**
 * @Author leoziltong@tencent.com
 */
public class AgentRunnable implements Runnable {
    private Object rpcContext;
    private Runnable wrapRunnable;

    public AgentRunnable(Object rpcContext, Runnable runnable) {
        this.rpcContext = rpcContext;
        this.wrapRunnable = runnable;
    }

    @Override
    public void run() {
        try {
            ReflectionUtils.invokeStaticMethod(RunnableWrapper.FEMAS_CONTEXT_CLASS_NAME,RunnableWrapper.CONTEXT_SET_VALUE_METHOD_NAME,rpcContext);
            if (wrapRunnable != null) {
                this.wrapRunnable.run();
            }
        } finally {
            ReflectionUtils.invokeStaticMethod(RunnableWrapper.FEMAS_CONTEXT_CLASS_NAME,RunnableWrapper.CONTEXT_CLEAN_VALUE_METHOD_NAME);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        if (o instanceof AgentRunnable) {
            AgentRunnable that = (AgentRunnable) o;
            return wrapRunnable.equals(that.wrapRunnable);
        } else if (o instanceof Runnable) {
            Runnable that = (Runnable) o;
            return wrapRunnable.equals(that);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return wrapRunnable.hashCode();
    }
}
