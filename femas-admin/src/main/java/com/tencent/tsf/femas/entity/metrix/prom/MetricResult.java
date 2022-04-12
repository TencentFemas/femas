package com.tencent.tsf.femas.entity.metrix.prom;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cody
 * @date 2021 2021/8/15 4:47 下午
 */
public class MetricResult implements Serializable {

    private Map<String, String> metric;

    /**
     * "values": [ [ <unix_time>, "<sample_value>" ], ... ]
     */
    private List<List<Object>> values;

    public MetricResult() {
    }

    public MetricResult(HashMap<String, String> metric, List<List<Object>> values) {
        this.metric = metric;
        this.values = values;
    }

    public Map<String, String> getMetric() {
        return metric;
    }

    public void setMetric(Map<String, String> metric) {
        this.metric = metric;
    }

    public List<List<Object>> getValues() {
        return values;
    }

    public void setValues(List<List<Object>> values) {
        this.values = values;
    }
}
