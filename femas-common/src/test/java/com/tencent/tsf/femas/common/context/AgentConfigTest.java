package com.tencent.tsf.femas.common.context;

import com.tencent.tsf.femas.agent.classloader.AgentClassLoader;
import org.junit.Assert;
import org.junit.Test;

public class AgentConfigTest {

    @Test
    public void testGetAgentClassLoader() {
        AgentClassLoader agentClassLoader = AgentConfig.getAgentClassLoader(AgentConfigTest.class, Thread.currentThread());
        Assert.assertNotNull(agentClassLoader);
    }
}
