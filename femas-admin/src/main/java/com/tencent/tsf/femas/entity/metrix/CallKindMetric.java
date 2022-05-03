package com.tencent.tsf.femas.entity.metrix;

import java.util.List;

/**
 * @author Cody
 * @date 2021 2021/8/15 10:13 下午
 */
public class CallKindMetric {

    private List<TimeSeries> sql;

    private List<TimeSeries> mq;

    private List<TimeSeries> unKnow;

    public List<TimeSeries> getSql() {
        return sql;
    }

    public void setSql(List<TimeSeries> sql) {
        this.sql = sql;
    }

    public List<TimeSeries> getMq() {
        return mq;
    }

    public void setMq(List<TimeSeries> mq) {
        this.mq = mq;
    }

    public List<TimeSeries> getUnKnow() {
        return unKnow;
    }

    public void setUnKnow(List<TimeSeries> unKnow) {
        this.unKnow = unKnow;
    }
}
