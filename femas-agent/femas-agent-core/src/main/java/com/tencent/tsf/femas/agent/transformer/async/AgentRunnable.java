package com.tencent.tsf.femas.agent.transformer.async;


/**
 * @Author leoziltong@tencent.com
 */
public class AgentRunnable implements Runnable {
    private String tag;
    private Runnable wrapRunnable;

    public AgentRunnable(String tag, Runnable runnable) {
//        if(runnable == null){
//            AgentLogger.getLogger().info("runnable is null");
//        }
        this.tag = tag;
        this.wrapRunnable = runnable;
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
