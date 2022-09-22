package com.tencent.tsf.femas.plugin;

import com.tencent.tsf.femas.common.annotation.SPI;
import com.tencent.tsf.femas.common.spi.SpiExtensionClass;
import com.tencent.tsf.femas.plugin.context.ConfigContext;

/**
 * @Author leoziltong
 * @Date: 2021/5/25 19:11
 */
@SPI
public interface ConfigProvider extends SpiExtensionClass {
    /**
     * 获取配置上下文
     *
     * @return 配置上下文
     */
    ConfigContext getPluginConfigs();

    /**
     * 插件归属
     *
     * @return 插件归属
     */
    Attribute getAttr();

}
