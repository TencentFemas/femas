package com.tencent.tsf.femas.agent.transformer.async;


import com.tencent.tsf.femas.agent.tools.ReflectionUtils;

import java.util.concurrent.Callable;


/**
 * @Author leoziltong@tencent.com
 */
public class AgentCallable implements Callable {
    private Object rpcContext;
    private Callable wrapCallable;

    public AgentCallable(Object rpcContext, Callable wrapCallable) {
        this.rpcContext = rpcContext;
        this.wrapCallable = wrapCallable;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        if (o instanceof AgentCallable) {
            AgentCallable that = (AgentCallable) o;
            return wrapCallable.equals(that.wrapCallable);
        } else if (o instanceof Callable) {
            Callable that = (Callable) o;
            return wrapCallable.equals(that);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return wrapCallable.hashCode();
    }
}
