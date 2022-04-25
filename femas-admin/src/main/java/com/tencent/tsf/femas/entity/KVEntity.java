package com.tencent.tsf.femas.entity;

/**
 * @author Cody
 * @date 2021 2021/8/6 3:20 下午
 */
public class KVEntity {

    private String key;

    private String value;

    public KVEntity(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public KVEntity() {
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
