package com.tencent.tsf.femas.agent.transformer.async;



/**
 * @Author leoziltong@tencent.com
 */
public class AgentContext {
    private static final ThreadLocal<String> current_agent_Head = new ThreadLocal<>();
    private static final ThreadLocal<String> current_agent_trace_id = new ThreadLocal<>();

    public static final String getAgentHead() {
        return current_agent_Head.get();
    }

    public static final String getTraceId() {
        return current_agent_trace_id.get();
    }

    public static final void setAgentHead(String agent) {
        current_agent_Head.set(agent);
    }

    public static final void setTraceId(String traceId) {
        current_agent_trace_id.set(traceId);
    }

    public static void clearContext() {
        current_agent_Head.remove();
        current_agent_trace_id.remove();
    }
}
