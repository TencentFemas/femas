package com.tencent.tsf.femas.entity.metrix;

import java.util.List;

/**
 * @author Cody
 * @date 2021 2021/8/15 10:06 下午
 */
public class ResponseMetric {

    private List<TimeSeries> p50;

    private List<TimeSeries> p75;

    private List<TimeSeries> p90;

    private List<TimeSeries> p99;

    private List<TimeSeries> average;

    public List<TimeSeries> getP50() {
        return p50;
    }

    public void setP50(List<TimeSeries> p50) {
        this.p50 = p50;
    }

    public List<TimeSeries> getP75() {
        return p75;
    }

    public void setP75(List<TimeSeries> p75) {
        this.p75 = p75;
    }

    public List<TimeSeries> getP90() {
        return p90;
    }

    public void setP90(List<TimeSeries> p90) {
        this.p90 = p90;
    }

    public List<TimeSeries> getP99() {
        return p99;
    }

    public void setP99(List<TimeSeries> p99) {
        this.p99 = p99;
    }

    public List<TimeSeries> getAverage() {
        return average;
    }

    public void setAverage(List<TimeSeries> average) {
        this.average = average;
    }
}
