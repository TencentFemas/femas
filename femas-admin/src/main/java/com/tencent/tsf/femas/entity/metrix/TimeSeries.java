package com.tencent.tsf.femas.entity.metrix;

/**
 * @author Cody
 * @date 2021 2021/8/15 9:58 下午
 */
public class TimeSeries {

    private Long time;

    private String value;

    public TimeSeries() {
    }

    public TimeSeries(Long time, String value) {
        this.time = time;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
