package com.tencent.tsf.femas.config;

import com.tencent.tsf.femas.common.annotation.SPI;
import com.tencent.tsf.femas.common.spi.SpiExtensionClass;
import java.util.Map;

@SPI
public interface ConfigFactory extends SpiExtensionClass {

    Config create(Map<String, String> configMap);
}
