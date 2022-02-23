package com.tencent.tsf.femas.common.spi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpiService {

    private static final Logger logger = LoggerFactory.getLogger(SpiService.class);

    public static <T extends SpiExtensionClass> Map<String, T> init(Class<T> spiExtensionClass) {
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
