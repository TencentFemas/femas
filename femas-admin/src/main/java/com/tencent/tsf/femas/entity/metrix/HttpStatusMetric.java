package com.tencent.tsf.femas.entity.metrix;

import java.util.List;

/**
 * @author Cody
 * @date 2021 2021/8/15 10:10 下午
 */
public class HttpStatusMetric {

    private List<TimeSeries> _2xx;

    private List<TimeSeries> _3xx;

    private List<TimeSeries> _4xx;

    private List<TimeSeries> _5xx;

    public List<TimeSeries> get_2xx() {
        return _2xx;
    }

    public void set_2xx(List<TimeSeries> _2xx) {
        this._2xx = _2xx;
    }

    public List<TimeSeries> get_3xx() {
        return _3xx;
    }

    public void set_3xx(List<TimeSeries> _3xx) {
        this._3xx = _3xx;
    }

    public List<TimeSeries> get_4xx() {
        return _4xx;
    }

    public void set_4xx(List<TimeSeries> _4xx) {
        this._4xx = _4xx;
    }

    public List<TimeSeries> get_5xx() {
        return _5xx;
    }

    public void set_5xx(List<TimeSeries> _5xx) {
        this._5xx = _5xx;
    }
}
