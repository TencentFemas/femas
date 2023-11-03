package com.tencent.tsf.femas.common.spi;

public interface SpiExtensionClass {
    /**
     * 获取扩展类型
     *
     * @return 扩展类型
     */
    String getType();

    /**
     * 获取扩展类型名称
     *
     * @return 扩展类型名称
     */
    default String getName() {
        return this.getClass().getName();
    }

}
