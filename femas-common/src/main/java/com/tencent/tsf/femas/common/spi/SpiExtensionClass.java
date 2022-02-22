package com.tencent.tsf.femas.common.spi;

public interface SpiExtensionClass {

    String getType();

    default String getName() {
        return this.getClass().getName();
    }

}
