package com.tencent.tsf.femas.entity.metrix.prom;

import java.util.List;

/**
 * @author Cody
 * @date 2021 2021/8/12 11:04 上午
 */
public class PromDataInfo<T> {

    /**
     * "matrix" | "vector" | "scalar" | "string"
     */
    private String resultType;

    private List<T> result;

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }
}
