package com.tencent.tsf.femas.agent.transformer.async;


import java.util.concurrent.Callable;


/**
 * @Author leoziltong@tencent.com
 */
public class AgentCallable implements Callable {
    private String tag;
    private Callable wrapCallable;

    public AgentCallable(String tag, Callable wrapCallable) {
        this.tag = tag;
        this.wrapCallable = wrapCallable;
    }

    @Override
    public Object call() throws Exception {
        try {
            AgentContext.setAgentHead(tag);
            if (wrapCallable != null) {
                return this.wrapCallable.call();
            } else {
                return null;
            }
        } finally {
            AgentContext.clearContext();
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
