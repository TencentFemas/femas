package com.tencent.tsf.femas.entity;

import java.util.List;

public class PageService<T> {

    private List<T> data;
    private Integer count;

    public PageService(List<T> data, Integer count) {
        this.data = data;
        this.count = count;
    }

    public PageService() {
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
