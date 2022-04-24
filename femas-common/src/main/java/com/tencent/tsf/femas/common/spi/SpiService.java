package com.tencent.tsf.femas.common.spi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import com.tencent.tsf.femas.agent.classloader.AgentClassLoader;
import com.tencent.tsf.femas.agent.classloader.InterceptorClassLoaderCache;
import com.tencent.tsf.femas.common.context.AgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.tencent.tsf.femas.common.context.ContextConstant.START_AGENT_FEMAS;

public class SpiService {

    private static final Logger logger = LoggerFactory.getLogger(SpiService.class);

    public static <T extends SpiExtensionClass> Map<String, T> init(Class<T> spiExtensionClass) {
        if (AgentConfig.doGetProperty(START_AGENT_FEMAS) != null && (Boolean) AgentConfig.doGetProperty(START_AGENT_FEMAS)) {
            AgentClassLoader agentClassLoader = InterceptorClassLoaderCache.getAgentClassLoader(Thread.currentThread().getContextClassLoader());
            Thread.currentThread().setContextClassLoader(agentClassLoader);
        }
        ServiceLoader<T> registryFactoryServiceLoader = ServiceLoader.load(spiExtensionClass);
        Iterator<T> it = registryFactoryServiceLoader.iterator();

        Map<String, T> typeMap = new HashMap<>();
        while (it.hasNext()) {
            T klassInstance = it.next();
            typeMap.put(klassInstance.getType(), klassInstance);
            logger.info("Load SPI Service, Type :" + klassInstance.getType() + ", Class :" + klassInstance.getClass()
                    .getCanonicalName());
        }
        return typeMap;
    }
}
