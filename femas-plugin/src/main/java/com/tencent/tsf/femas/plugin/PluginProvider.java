package com.tencent.tsf.femas.plugin;

import com.tencent.tsf.femas.common.annotation.SPI;
import com.tencent.tsf.femas.common.spi.SpiExtensionClass;

import java.util.List;

/**
 * @Author leoziltong
 * @Date: 2021/5/25 19:11
 */
@SPI
public interface PluginProvider extends SpiExtensionClass {

    /**
     * 实现充分的灵活性，实现层提供插件列表，femas不指定插件列表
     *
     * @return 插件列表
     */
    List<Class<? extends Plugin>> getPluginTypes();

    /**
     * 插件归属
     *
     * @return 插件归属
     */
    Attribute getAttr();

}
