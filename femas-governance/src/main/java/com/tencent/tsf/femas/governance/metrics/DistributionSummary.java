package com.tencent.tsf.femas.governance.metrics;

/**
 * @Author p_mtluo
 * @Date 2021-08-17 20:05
 * @Description TODO
 **/
public interface DistributionSummary<T> extends Meter<T> {

    void record(double amount);
}
