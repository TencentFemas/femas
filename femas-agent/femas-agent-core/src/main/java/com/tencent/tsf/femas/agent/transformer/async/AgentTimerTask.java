package com.tencent.tsf.femas.agent.transformer.async;

import java.util.TimerTask;


/**
 * @Author leoziltong@tencent.com
 */
public class AgentTimerTask extends TimerTask {

    private String tag;
    private TimerTask timerTask;

    public AgentTimerTask(String tag, TimerTask timerTask) {
        this.tag = tag;
        this.timerTask = timerTask;
    }

    @Override
    public void run() {
        try {
            AgentContext.setAgentHead(tag);
            if (timerTask != null) {
                this.timerTask.run();
            }
        } finally {
            AgentContext.clearContext();
        }

    }
}
