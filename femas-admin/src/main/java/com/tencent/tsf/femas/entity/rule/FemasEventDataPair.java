package com.tencent.tsf.femas.entity.rule;

public class FemasEventDataPair {

    private String key;
    private String value;

    public FemasEventDataPair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public FemasEventDataPair() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
