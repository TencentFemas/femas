package com.tencent.tsf.femas.agent.transformer.async;


import java.util.TimerTask;
import java.util.concurrent.Callable;


public class RunnableWrapper {

    public static Runnable wrapRunnable(Runnable runnable) {
//        AgentLogger.getLogger().info("AgentRunnable.wrapRunnable");
        if (runnable instanceof AgentRunnable) {
            return runnable;
        } else {
            return new AgentRunnable(AgentContext.getAgentHead(), runnable);
        }
    }

    public static Callable wrapCallable(Callable callable) {
//        AgentLogger.getLogger().info("AgentRunnable.wrapCallable");
        if (callable instanceof AgentCallable) {
            return callable;
        } else {
            return new AgentCallable(AgentContext.getAgentHead(), callable);
        }
    }

    public static TimerTask wrapTimerTask(TimerTask timerTask) {
        if (timerTask instanceof AgentTimerTask) {
            return timerTask;
        } else {
            return new AgentTimerTask(AgentContext.getAgentHead(), timerTask);
        }
    }

    public static AgentSyncTask wrapAsync(Object target) {
        return new AgentSyncTask(AgentContext.getAgentHead(), target);
    }
}
