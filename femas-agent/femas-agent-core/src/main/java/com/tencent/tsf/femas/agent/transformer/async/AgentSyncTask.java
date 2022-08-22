package com.tencent.tsf.femas.agent.transformer.async;

import com.tencent.tsf.femas.agent.tools.ReflectionUtils;

import java.util.concurrent.Callable;


/**
 * @Author leoziltong@tencent.com
 */
public class AgentSyncTask implements Runnable, Callable {
    private Object rpcContext;
    private Runnable wrapRunnable;
    private Callable wrapCallable;

    public AgentSyncTask(Object rpcContext, Object target) {
        this.rpcContext = rpcContext;
        if (target instanceof Runnable) {
            this.wrapRunnable = (Runnable) target;
        } else if (target instanceof Callable) {
            this.wrapCallable = (Callable) target;
        }
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
    public Object call() throws Exception {
        try {
            ReflectionUtils.invokeStaticMethod(RunnableWrapper.FEMAS_CONTEXT_CLASS_NAME,RunnableWrapper.CONTEXT_SET_VALUE_METHOD_NAME,rpcContext);
            if (wrapCallable != null) {
                return this.wrapCallable.call();
            } else {
                return null;
            }
        } finally {
            ReflectionUtils.invokeStaticMethod(RunnableWrapper.FEMAS_CONTEXT_CLASS_NAME,RunnableWrapper.CONTEXT_CLEAN_VALUE_METHOD_NAME);
        }
    }
}
