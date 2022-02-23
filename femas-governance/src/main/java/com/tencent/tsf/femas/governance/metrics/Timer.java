package com.tencent.tsf.femas.governance.metrics;

import java.util.concurrent.TimeUnit;

/**
 * @Author p_mtluo
 * @Date 2021-08-17 17:55
 * @Description TODO
 **/
public interface Timer<T> extends Meter<T> {

    void record(long amount, TimeUnit unit);
}
