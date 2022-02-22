package com.tencent.tsf.femas.governance.metrics;

/**
 * @Author p_mtluo
 * @Date 2021-08-17 20:09
 * @Description TODO
 **/
public interface Counter<T> extends Meter<T> {

    void increment(double amount);

    void increment();
}
