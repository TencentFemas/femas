package com.tencent.tsf.femas.agent.transformer.async;

import java.util.concurrent.Callable;


/**
 * @Author leoziltong@tencent.com
 */
public class AgentSyncTask implements Runnable, Callable {
    private String tag;
    private Runnable wrapRunnable;
    private Callable wrapCallable;

    public AgentSyncTask(String tag, Object target) {
        this.tag = tag;
        if (target instanceof Runnable) {
            this.wrapRunnable = (Runnable) target;
        } else if (target instanceof Callable) {
            this.wrapCallable = (Callable) target;
        }
    }


    @Override
    public void run() {
        try {
            AgentContext.setAgentHead(tag);
            if (wrapRunnable != null) {
                this.wrapRunnable.run();
            }
        } finally {
            AgentContext.clearContext();
        }
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
}
