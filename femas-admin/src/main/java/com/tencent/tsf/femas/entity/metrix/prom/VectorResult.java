package com.tencent.tsf.femas.entity.metrix.prom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cody
 * @date 2021 2021/8/15 4:46 下午
 */
public class VectorResult {

    private Map<String, String> metric;

    /**
     * "value": [ <unix_time>, "<sample_value>" ]
     */
    private List<Object> value;

    public VectorResult() {
    }

    public VectorResult(HashMap<String, String> metric, List<Object> value) {
        this.metric = metric;
        this.value = value;
    }


    public Map<String, String> getMetric() {
        return metric;
    }

    public void setMetric(Map<String, String> metric) {
        this.metric = metric;
    }

    public List<Object> getValue() {
        return value;
    }

    public void setValue(List<Object> value) {
        this.value = value;
    }
}
