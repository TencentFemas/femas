package com.tencent.tsf.femas.governance.metrics;

/**
 * @Author p_mtluo
 * @Date 2021-08-17 20:12
 * @Description TODO
 **/
public interface LongTaskTimer<T, N> extends Meter<T> {

    N start();

    long stop(long task);
}
