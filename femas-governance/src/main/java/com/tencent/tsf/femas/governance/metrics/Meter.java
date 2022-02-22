package com.tencent.tsf.femas.governance.metrics;

/**
 * @Author p_mtluo
 * @Date 2021-08-17 17:06
 * @Description TODO
 **/
public interface Meter<T> {

    T getMeter();

    MeterEnum getMeterType();

    String getMeterName();
}
