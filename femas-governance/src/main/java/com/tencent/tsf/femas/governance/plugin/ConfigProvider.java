package com.tencent.tsf.femas.governance.plugin;

import com.tencent.tsf.femas.common.annotation.SPI;
import com.tencent.tsf.femas.common.spi.SpiExtensionClass;
import com.tencent.tsf.femas.governance.plugin.context.ConfigContext;

/**
 * @Author leoziltong
 * @Date: 2021/5/25 19:11
 */
@SPI
public interface ConfigProvider extends SpiExtensionClass {

    ConfigContext getPluginConfigs();

    /**
     * 插件归属
     *
     * @return
     */
    Attribute getAttr();

}
