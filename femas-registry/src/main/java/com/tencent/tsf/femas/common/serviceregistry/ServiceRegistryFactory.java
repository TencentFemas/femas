package com.tencent.tsf.femas.common.serviceregistry;

import com.tencent.tsf.femas.common.annotation.SPI;
import com.tencent.tsf.femas.common.spi.SpiExtensionClass;
import java.util.Map;

/**
 * 通过SPI注册的注册中心生成
 *
 * @author zhixinzxliu
 */
@SPI
public interface ServiceRegistryFactory extends SpiExtensionClass {

    ServiceRegistry getServiceRegistry(Map<String, String> configMap);
}
